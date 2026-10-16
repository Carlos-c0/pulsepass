package com.pulsepass.controller;

import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.RestAccessDeniedHandler;
import com.pulsepass.security.RestAuthenticationEntryPoint;
import com.pulsepass.security.SecurityConfig;
import com.pulsepass.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.pulsepass.support.TestPrincipals.andrea;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class UserControllerTest {

    private static final LocalDate FECHA_NACIMIENTO = LocalDate.of(2005, 3, 10);

    private static final String USUARIO_VALIDO_JSON = """
            {
              "username": "andrea",
              "email": "andrea@email.com",
              "firstName": "Andrea",
              "lastName": "Gomez",
              "phone": "3001234567",
              "city": "Santa Marta",
              "birthDate": "2005-03-10"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // Dependencias del filtro JWT de SecurityConfig. En estas pruebas la identidad
    // llega con .with(user(...)), asi que el filtro solo actua cuando hay un encabezado Bearer.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("TEST-CTRL-USR-001: registrar usuario valido retorna 201")
    void registrarValidoRetorna201() throws Exception {
        // ARRANGE
        when(userService.register(any(RegisterUserRequest.class))).thenReturn(respuestaAndrea());

        // ACT + ASSERT
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_VALIDO_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.city").value("Santa Marta"));

        verify(userService).register(new RegisterUserRequest(
                "andrea", "andrea@email.com", "Andrea", "Gomez", "3001234567", "Santa Marta", FECHA_NACIMIENTO
        ));
    }

    @Test
    @DisplayName("TEST-CTRL-USR-002: email invalido retorna 400 y no llama al Service")
    void emailInvalidoRetorna400() throws Exception {
        // ARRANGE
        String json = USUARIO_VALIDO_JSON.replace("andrea@email.com", "andrea-sin-arroba");

        // ACT + ASSERT
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.email").value("Email must be valid"));

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("TEST-CTRL-USR-003: username duplicado retorna 409")
    void usernameDuplicadoRetorna409() throws Exception {
        // ARRANGE
        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new DuplicateResourceException("Username already exists: andrea"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_VALIDO_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username already exists: andrea"));
    }

    @Test
    @DisplayName("TEST-CTRL-USR-004: consultar usuario por email retorna 200")
    void consultarPorEmailRetorna200() throws Exception {
        // ARRANGE
        when(userService.findByEmail("andrea@email.com")).thenReturn(respuestaAndrea());

        // ACT + ASSERT
        mockMvc.perform(get("/api/users/by-email").with(user(andrea())).param("email", "andrea@email.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("andrea@email.com"))
                .andExpect(jsonPath("$.firstName").value("Andrea"));

        verify(userService).findByEmail("andrea@email.com");
    }

    @Test
    @DisplayName("TEST-CTRL-USR-005: consultar usuario por username retorna 200")
    void consultarPorUsernameRetorna200() throws Exception {
        // ARRANGE
        when(userService.findByUsername("andrea")).thenReturn(respuestaAndrea());

        // ACT + ASSERT
        mockMvc.perform(get("/api/users/by-username").with(user(andrea())).param("username", "andrea"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(userService).findByUsername("andrea");
    }

    @Test
    @DisplayName("Consultar por email sin el parametro retorna 400")
    void consultarSinParametroRetorna400() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(get("/api/users/by-email").with(user(andrea())))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Missing required parameter: email"))
                .andExpect(jsonPath("$.details.email").value("Parameter is required"));

        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("FR-SEC-USR-02: un anonimo no puede consultar usuarios (401)")
    void anonimoNoPuedeConsultarUsuarios() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@email.com"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verify(userService, never()).findByEmail(anyString());
    }

    private UserResponse respuestaAndrea() {
        return new UserResponse(
                1L, "andrea", "andrea@email.com", true, "Andrea", "Gomez",
                "3001234567", "Santa Marta", FECHA_NACIMIENTO
        );
    }
}
