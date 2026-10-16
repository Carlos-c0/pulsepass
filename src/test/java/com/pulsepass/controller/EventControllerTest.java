package com.pulsepass.controller;

import com.pulsepass.domain.EventCategory;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.TicketStatus;
import com.pulsepass.domain.TicketType;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.RestAccessDeniedHandler;
import com.pulsepass.security.RestAuthenticationEntryPoint;
import com.pulsepass.security.SecurityConfig;
import com.pulsepass.service.EventService;
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

import static com.pulsepass.support.TestPrincipals.admin;
import static com.pulsepass.support.TestPrincipals.andrea;
import static com.pulsepass.support.TestPrincipals.laura;
import static org.mockito.ArgumentMatchers.any;
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

@WebMvcTest(EventController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class EventControllerTest {

    private static final LocalDateTime FECHA_EVENTO = LocalDateTime.of(2030, 6, 1, 20, 0);

    private static final String EVENTO_VALIDO_JSON = """
            {
              "eventCode": "CMF-2026",
              "name": "Caribbean Music Fest 2026",
              "description": "Festival de musica del Caribe",
              "category": "MUSIC",
              "eventDate": "2030-06-01T20:00:00",
              "minimumAge": 18,
              "venueCode": "VEN-SMR-01"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private TicketService ticketService;

    // Dependencias del filtro JWT de SecurityConfig. En estas pruebas la identidad
    // llega con .with(user(...)), asi que el filtro solo actua cuando hay un encabezado Bearer.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("TEST-CTRL-EVT-001 / T-SEC-004: ADMIN crea un evento valido y recibe 201")
    void crearEventoValidoRetorna201() throws Exception {
        // ARRANGE
        when(eventService.create(any(CreateEventRequest.class))).thenReturn(evento(EventStatus.DRAFT));

        // ACT + ASSERT
        mockMvc.perform(post("/api/events").with(user(admin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_VALIDO_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.venueCode").value("VEN-SMR-01"));

        // El JSON llego al Service convertido exactamente en este record
        verify(eventService).create(new CreateEventRequest(
                "CMF-2026", "Caribbean Music Fest 2026", "Festival de musica del Caribe",
                EventCategory.MUSIC, FECHA_EVENTO, 18, "VEN-SMR-01"
        ));
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-002 / T-SEC-018: request invalido de un ADMIN retorna 400 y no llama al Service")
    void requestInvalidoRetorna400() throws Exception {
        // ARRANGE: sin eventCode, sin venueCode y con edad minima negativa
        String json = """
                {
                  "name": "Caribbean Music Fest 2026",
                  "category": "MUSIC",
                  "eventDate": "2030-06-01T20:00:00",
                  "minimumAge": -1
                }
                """;

        // ACT + ASSERT
        mockMvc.perform(post("/api/events").with(user(admin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.venueCode").value("Venue code is required"))
                .andExpect(jsonPath("$.details.minimumAge").value("Minimum age cannot be negative"));

        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-003: consultar evento existente retorna 200")
    void consultarEventoExistenteRetorna200() throws Exception {
        // ARRANGE
        when(eventService.findByCode("CMF-2026")).thenReturn(evento(EventStatus.PUBLISHED));

        // ACT + ASSERT
        mockMvc.perform(get("/api/events/{eventCode}", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Caribbean Music Fest 2026"))
                .andExpect(jsonPath("$.venueName").value("Marina Convention Center"))
                .andExpect(jsonPath("$.artists[0].stageName").value("Solar Beat"));

        verify(eventService).findByCode("CMF-2026");
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-004: evento inexistente retorna 404")
    void eventoInexistenteRetorna404() throws Exception {
        // ARRANGE
        when(eventService.findByCode("NO-EXISTE"))
                .thenThrow(new ResourceNotFoundException("Event not found: NO-EXISTE"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/events/{eventCode}", "NO-EXISTE"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Event not found: NO-EXISTE"));
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-005 / T-SEC-001: un anonimo lista los eventos publicados sin token")
    void listarPublicadosRetorna200() throws Exception {
        // ARRANGE
        when(eventService.findPublishedEvents()).thenReturn(List.of(resumen()));

        // ACT + ASSERT
        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"));

        verify(eventService).findPublishedEvents();
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-006 / T-SEC-014: ADMIN publica un evento valido y recibe 200")
    void publicarValidoRetorna200() throws Exception {
        // ARRANGE
        when(eventService.publish("CMF-2026")).thenReturn(evento(EventStatus.PUBLISHED));

        // ACT + ASSERT
        mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026").with(user(admin())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(eventService).publish("CMF-2026");
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-007: publicar evento invalido retorna 409")
    void publicarInvalidoRetorna409() throws Exception {
        // ARRANGE
        when(eventService.publish("CMF-2026")).thenThrow(new BusinessRuleException(
                "Only DRAFT events can be published. Current status: CANCELLED"));

        // ACT + ASSERT
        mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026").with(user(admin())))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "Only DRAFT events can be published. Current status: CANCELLED"));
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-008: asociar artista retorna 200")
    void asociarArtistaRetorna200() throws Exception {
        // ARRANGE
        when(eventService.addArtist("CMF-2026", 1L)).thenReturn(evento(EventStatus.PUBLISHED));

        // ACT + ASSERT
        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 1L).with(user(admin())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.artists[0].id").value(1));

        verify(eventService).addArtist("CMF-2026", 1L);
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-009: buscar eventos por artista retorna 200")
    void buscarPorArtistaRetorna200() throws Exception {
        // ARRANGE
        when(eventService.findByArtist("Solar Beat")).thenReturn(List.of(resumen()));

        // ACT + ASSERT
        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].venueName").value("Marina Convention Center"));

        verify(eventService).findByArtist("Solar Beat");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-007: STAFF consulta los tickets PAID de un evento y recibe 200")
    void ticketsPagadosDeEventoRetorna200() throws Exception {
        // ARRANGE
        TicketResponse ticket = new TicketResponse(
                1L, "TCK-0001", TicketType.VIP, new BigDecimal("200000.00"), TicketStatus.PAID,
                FECHA_EVENTO.minusDays(30), "andrea@email.com", "CMF-2026", "Caribbean Music Fest 2026"
        );
        when(ticketService.findPaidTicketsByEvent("CMF-2026")).thenReturn(List.of(ticket));

        // ACT + ASSERT
        mockMvc.perform(get("/api/events/{eventCode}/tickets/paid", "CMF-2026").with(user(laura())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticketCode").value("TCK-0001"))
                .andExpect(jsonPath("$[0].status").value("PAID"));

        verify(ticketService).findPaidTicketsByEvent("CMF-2026");
    }

    // ---------- reglas de acceso (Fase 4) ----------

    @Test
    @DisplayName("T-SEC-002: un anonimo no puede crear eventos (401)")
    void anonimoNoPuedeCrearEventos() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_VALIDO_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("T-SEC-003: un USER no puede crear eventos (403) y el Service no se invoca")
    void userNoPuedeCrearEventos() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/events").with(user(andrea()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_VALIDO_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("No tiene permisos para realizar esta operación"));

        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("UC-SEC-08: un USER no puede publicar eventos (403)")
    void userNoPuedePublicar() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026").with(user(andrea())))
                .andExpect(status().isForbidden());

        verify(eventService, never()).publish(any());
    }

    @Test
    @DisplayName("FR-SEC-TKT-04: un USER no puede ver los tickets pagados de un evento (403)")
    void userNoPuedeVerTicketsPagados() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(get("/api/events/{eventCode}/tickets/paid", "CMF-2026").with(user(andrea())))
                .andExpect(status().isForbidden());

        verify(ticketService, never()).findPaidTicketsByEvent(any());
    }

    // ---------- datos de apoyo ----------

    private EventResponse evento(EventStatus status) {
        return new EventResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", "Festival de musica del Caribe",
                EventCategory.MUSIC, status, FECHA_EVENTO, 18, "VEN-SMR-01", "Marina Convention Center",
                List.of(new ArtistResponse(1L, "Solar Beat", "Colombia", "Electronic", true))
        );
    }

    private EventSummaryResponse resumen() {
        return new EventSummaryResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", EventCategory.MUSIC,
                EventStatus.PUBLISHED, FECHA_EVENTO, "Marina Convention Center", "Santa Marta"
        );
    }
}
