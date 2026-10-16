package com.pulsepass.controller;

import com.pulsepass.domain.Role;
import com.pulsepass.domain.TicketStatus;
import com.pulsepass.domain.TicketType;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.RestAccessDeniedHandler;
import com.pulsepass.security.RestAuthenticationEntryPoint;
import com.pulsepass.security.SecurityConfig;
import com.pulsepass.security.SecurityPrincipal;
import com.pulsepass.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.pulsepass.support.TestPrincipals.andrea;
import static com.pulsepass.support.TestPrincipals.carlos;
import static com.pulsepass.support.TestPrincipals.laura;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class TicketControllerTest {

    private static final String COMPRA_VALIDA_JSON = """
            {
              "userEmail": "andrea@email.com",
              "eventCode": "CMF-2026",
              "type": "VIP"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    // Dependencias del filtro JWT de SecurityConfig. En estas pruebas la identidad
    // llega con .with(user(...)), asi que el filtro solo actua cuando hay un encabezado Bearer.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    // ---------- comprar ----------

    @Test
    @DisplayName("TEST-CTRL-TKT-001: compra valida retorna 201 y TicketResponse")
    void compraValidaRetorna201() throws Exception {
        // ARRANGE
        when(ticketService.purchase(any(PurchaseTicketRequest.class))).thenReturn(ticket(TicketStatus.PAID));

        // ACT + ASSERT
        mockMvc.perform(post("/api/tickets").with(user(andrea()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPRA_VALIDA_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TCK-0001"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.userEmail").value("andrea@email.com"))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        verify(ticketService).purchase(new PurchaseTicketRequest("andrea@email.com", "CMF-2026", TicketType.VIP));
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-002: request invalido retorna 400 y no llama al Service")
    void requestInvalidoRetorna400() throws Exception {
        // ARRANGE: email sin formato, eventCode vacio y sin tipo de ticket
        String json = """
                {
                  "userEmail": "andrea-sin-arroba",
                  "eventCode": ""
                }
                """;

        // ACT + ASSERT
        mockMvc.perform(post("/api/tickets").with(user(andrea()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.userEmail").value("User email must be valid"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.type").value("Ticket type is required"));

        verify(ticketService, never()).purchase(any());
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-003: usuario inexistente retorna 404")
    void usuarioInexistenteRetorna404() throws Exception {
        // ARRANGE
        when(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found: andrea@email.com"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/tickets").with(user(andrea()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPRA_VALIDA_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User not found: andrea@email.com"));
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-004: regla de negocio incumplida retorna 409")
    void reglaDeNegocioRetorna409() throws Exception {
        // ARRANGE: el Service rechaza la compra (Carlos no cumple una regla de negocio)
        when(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .thenThrow(new BusinessRuleException("User does not meet minimum age."));

        // ACT + ASSERT
        mockMvc.perform(post("/api/tickets").with(user(carlos()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPRA_VALIDA_JSON.replace("andrea@email.com", "carlos@email.com")))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User does not meet minimum age."));
    }

    @Test
    @DisplayName("JSON mal formado retorna 400 y no llama al Service")
    void jsonMalFormadoRetorna400() throws Exception {
        // ACT + ASSERT: falta la llave de cierre
        mockMvc.perform(post("/api/tickets").with(user(andrea()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"userEmail\": \"andrea@email.com\""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));

        verify(ticketService, never()).purchase(any());
    }

    // ---------- consultar ----------

    @Test
    @DisplayName("TEST-CTRL-TKT-005: consultar ticket retorna 200")
    void consultarTicketRetorna200() throws Exception {
        // ARRANGE
        when(ticketService.findByCode("TCK-0001")).thenReturn(ticket(TicketStatus.PAID));

        // ACT + ASSERT
        mockMvc.perform(get("/api/tickets/{ticketCode}", "TCK-0001").with(user(andrea())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TCK-0001"))
                .andExpect(jsonPath("$.type").value("VIP"));

        verify(ticketService).findByCode("TCK-0001");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-006: tickets por usuario retorna 200")
    void ticketsPorUsuarioRetorna200() throws Exception {
        // ARRANGE
        when(ticketService.findByUserEmail("andrea@email.com")).thenReturn(List.of(ticket(TicketStatus.PAID)));

        // ACT + ASSERT
        mockMvc.perform(get("/api/tickets/by-user").with(user(andrea())).param("email", "andrea@email.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userEmail").value("andrea@email.com"));

        verify(ticketService).findByUserEmail("andrea@email.com");
    }

    // ---------- cancelar y usar ----------

    @Test
    @DisplayName("TEST-CTRL-TKT-008 / T-SEC-011: USER cancela su ticket PAID y recibe 200")
    void cancelarValidoRetorna200() throws Exception {
        // ARRANGE
        when(ticketService.cancel("TCK-0001")).thenReturn(ticket(TicketStatus.CANCELLED));

        // ACT + ASSERT
        mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TCK-0001").with(user(andrea())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(ticketService).cancel("TCK-0001");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-009: cancelar ticket USED retorna 409")
    void cancelarInvalidoRetorna409() throws Exception {
        // ARRANGE
        when(ticketService.cancel("TCK-0001")).thenThrow(new BusinessRuleException(
                "Only PAID tickets can be cancelled. Current status: USED"));

        // ACT + ASSERT
        mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TCK-0001").with(user(andrea())))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Only PAID tickets can be cancelled. Current status: USED"));
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-010 / T-SEC-013: STAFF marca un ticket PAID como usado y recibe 200")
    void usarValidoRetorna200() throws Exception {
        // ARRANGE
        when(ticketService.markAsUsed("TCK-0001")).thenReturn(ticket(TicketStatus.USED));

        // ACT + ASSERT
        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TCK-0001").with(user(laura())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("USED"));

        verify(ticketService).markAsUsed("TCK-0001");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-011: usar ticket CANCELLED retorna 409")
    void usarInvalidoRetorna409() throws Exception {
        // ARRANGE
        when(ticketService.markAsUsed("TCK-0001")).thenThrow(new BusinessRuleException(
                "Only PAID tickets can be marked as used. Current status: CANCELLED"));

        // ACT + ASSERT
        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TCK-0001").with(user(laura())))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(
                        "Only PAID tickets can be marked as used. Current status: CANCELLED"));
    }

    // ---------- reglas de acceso y JWT (Fase 4) ----------

    @Test
    @DisplayName("Un anonimo no puede comprar (401)")
    void anonimoNoPuedeComprar() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPRA_VALIDA_JSON))
                .andExpect(status().isUnauthorized());

        verify(ticketService, never()).purchase(any());
    }

    @Test
    @DisplayName("FR-SEC-TKT-01: STAFF sin rol USER no puede comprar (403)")
    void staffNoPuedeComprar() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/tickets").with(user(laura()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPRA_VALIDA_JSON))
                .andExpect(status().isForbidden());

        verify(ticketService, never()).purchase(any());
    }

    @Test
    @DisplayName("T-SEC-012: un USER no puede marcar tickets como usados (403)")
    void userNoPuedeUsarTickets() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TCK-0001").with(user(andrea())))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("No tiene permisos para realizar esta operación"));

        verify(ticketService, never()).markAsUsed(anyString());
    }

    @Test
    @DisplayName("T-SEC-007: un token vencido responde 401 con ErrorResponse")
    void tokenVencidoRetorna401() throws Exception {
        // ARRANGE: JwtService no reconoce el token (vencido)
        when(jwtService.extractUserId("token-vencido")).thenReturn(Optional.empty());

        // ACT + ASSERT
        mockMvc.perform(get("/api/tickets/{ticketCode}", "TCK-0001")
                        .header("Authorization", "Bearer token-vencido"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"))
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    @DisplayName("T-SEC-008: un token con la firma alterada responde 401 y no invoca el Service")
    void tokenAlteradoRetorna401SinInvocarService() throws Exception {
        // ARRANGE
        when(jwtService.extractUserId("token-alterado")).thenReturn(Optional.empty());

        // ACT + ASSERT: aunque la ruta sea de un USER, el Bearer invalido nunca pasa como anonimo
        mockMvc.perform(get("/api/tickets/{ticketCode}", "TCK-0001")
                        .header("Authorization", "Bearer token-alterado"))
                .andExpect(status().isUnauthorized());

        verify(ticketService, never()).findByCode(anyString());
        verify(userDetailsService, never()).loadUserById(any());
    }

    @Test
    @DisplayName("T-SEC-016: el token valido de un usuario desactivado responde 401")
    void tokenDeUsuarioInactivoRetorna401() throws Exception {
        // ARRANGE: el token es valido, pero en la base de datos el usuario ya esta inactivo
        when(jwtService.extractUserId("token-valido")).thenReturn(Optional.of(1L));
        when(userDetailsService.loadUserById(1L)).thenReturn(new SecurityPrincipal(
                1L, "andrea", "andrea@email.com", "$2a$10$hashFicticio", false, Set.of(Role.USER)));

        // ACT + ASSERT
        mockMvc.perform(get("/api/tickets/{ticketCode}", "TCK-0001")
                        .header("Authorization", "Bearer token-valido"))
                .andExpect(status().isUnauthorized());

        verify(ticketService, never()).findByCode(anyString());
    }

    @Test
    @DisplayName("Un token valido de un usuario activo permite consultar su ticket")
    void tokenValidoPermiteConsultar() throws Exception {
        // ARRANGE
        when(jwtService.extractUserId("token-valido")).thenReturn(Optional.of(1L));
        when(userDetailsService.loadUserById(1L)).thenReturn(andrea());
        when(ticketService.findByCode("TCK-0001")).thenReturn(ticket(TicketStatus.PAID));

        // ACT + ASSERT
        mockMvc.perform(get("/api/tickets/{ticketCode}", "TCK-0001")
                        .header("Authorization", "Bearer token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketCode").value("TCK-0001"));
    }

    private TicketResponse ticket(TicketStatus status) {
        return new TicketResponse(
                1L, "TCK-0001", TicketType.VIP, new BigDecimal("200000.00"), status,
                LocalDateTime.of(2030, 5, 2, 10, 0), "andrea@email.com", "CMF-2026", "Caribbean Music Fest 2026"
        );
    }
}
