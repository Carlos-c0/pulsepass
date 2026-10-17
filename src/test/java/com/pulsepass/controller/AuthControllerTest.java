package com.pulsepass.controller;

import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.RestAccessDeniedHandler;
import com.pulsepass.security.RestAuthenticationEntryPoint;
import com.pulsepass.security.SecurityConfig;
import com.pulsepass.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.pulsepass.support.TestPrincipals.andrea;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class AuthControllerTest {

    private static final String LOGIN_JSON = """
            {
              "identifier": "andrea",
              "password": "UnaFraseSeguraLarga2026!"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("T-SEC-005: login correcto de un anonimo retorna 200 con Bearer")
    void loginCorrectoRetornaBearer() throws Exception {
        // ARRANGE
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("jwt-firmado", "Bearer", 900));

        // ACT + ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("jwt-firmado"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));

        verify(authService).login(new LoginRequest("andrea", "UnaFraseSeguraLarga2026!"));
    }

    @Test
    @DisplayName("T-SEC-006 / T-SEC-019: login con contrasena incorrecta retorna 401 generico, sin detalles")
    void loginIncorrectoRetorna401Generico() throws Exception {
        // ARRANGE
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.details").isEmpty())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Login sin contrasena retorna 400 y no llama al Service")
    void loginSinContrasenaRetorna400() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"identifier\": \"andrea\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").value("Password is required"));

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("FR-SEC-AUTH-010: /api/auth/me retorna la identidad autenticada")
    void meRetornaLaIdentidad() throws Exception {
        // ARRANGE
        when(authService.currentUser(andrea()))
                .thenReturn(new AuthenticatedUserResponse(1L, "andrea", "andrea@email.com", List.of("USER")));

        // ACT + ASSERT
        mockMvc.perform(get("/api/auth/me").with(user(andrea())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(authService).currentUser(andrea());
    }

    @Test
    @DisplayName("/api/auth/me sin token retorna 401")
    void meSinTokenRetorna401() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));

        verify(authService, never()).currentUser(any());
    }
}
