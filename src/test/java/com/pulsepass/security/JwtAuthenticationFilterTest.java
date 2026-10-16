package com.pulsepass.security;

import com.pulsepass.domain.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String SECRETO = "cHVsc2VwYXNzLXRlc3Qtc2VjcmV0LXNvbG8tcGFyYS1wcnVlYmFzLTIwMjYhIQ==";

    // JwtService real con clave de prueba: asi el filtro se prueba con tokens de verdad
    private final JwtService jwtService =
            new JwtService(new JwtProperties("pulsepass-api", SECRETO, Duration.ofMinutes(15)));

    // Registra cada vez que el filtro rechaza el request
    private final List<AuthenticationException> rechazos = new ArrayList<>();

    @Mock
    private CustomUserDetailsService userDetailsService;

    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets/TCK-0001");
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain chain = new MockFilterChain();

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Sin encabezado Authorization el request sigue como anonimo")
    void sinEncabezadoSigueComoAnonimo() throws Exception {
        // ACT
        filtro().doFilter(request, response, chain);

        // ASSERT
        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(rechazos).isEmpty();
    }

    @Test
    @DisplayName("Bearer valido deja el principal y sus roles en el contexto")
    void bearerValidoAutentica() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Bearer " + jwtService.generateToken(1L));
        when(userDetailsService.loadUserById(1L)).thenReturn(andrea(true));

        // ACT
        filtro().doFilter(request, response, chain);

        // ASSERT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(chain.getRequest()).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(andrea(true));
        assertThat(authentication.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("SEC-008: Bearer invalido se rechaza con 401 y no continua la cadena")
    void bearerInvalidoSeRechaza() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Bearer token-falso");

        // ACT
        filtro().doFilter(request, response, chain);

        // ASSERT
        assertThat(chain.getRequest()).isNull();
        assertThat(rechazos).hasSize(1);
        assertThat(rechazos.get(0).getMessage()).isEqualTo("Invalid or expired token");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserById(any());
    }

    @Test
    @DisplayName("Un esquema distinto de Bearer se rechaza")
    void esquemaDistintoSeRechaza() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Basic YW5kcmVhOmNsYXZl");

        // ACT
        filtro().doFilter(request, response, chain);

        // ASSERT
        assertThat(chain.getRequest()).isNull();
        assertThat(rechazos).hasSize(1);
    }

    @Test
    @DisplayName("Dos encabezados Authorization se rechazan aunque uno sea valido")
    void dosEncabezadosSeRechazan() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Bearer " + jwtService.generateToken(1L));
        request.addHeader("Authorization", "Bearer " + jwtService.generateToken(2L));

        // ACT
        filtro().doFilter(request, response, chain);

        // ASSERT
        assertThat(chain.getRequest()).isNull();
        assertThat(rechazos).hasSize(1);
        verify(userDetailsService, never()).loadUserById(any());
    }

    @Test
    @DisplayName("SEC-011: un usuario desactivado con token vigente se rechaza")
    void usuarioDesactivadoSeRechaza() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Bearer " + jwtService.generateToken(1L));
        when(userDetailsService.loadUserById(1L)).thenReturn(andrea(false));

        // ACT
        filtro().doFilter(request, response, chain);

        // ASSERT
        assertThat(chain.getRequest()).isNull();
        assertThat(rechazos).hasSize(1);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Un token de un usuario que ya no existe se rechaza")
    void usuarioInexistenteSeRechaza() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Bearer " + jwtService.generateToken(99L));
        when(userDetailsService.loadUserById(99L)).thenThrow(new UsernameNotFoundException("Invalid credentials"));

        // ACT
        filtro().doFilter(request, response, chain);

        // ASSERT
        assertThat(chain.getRequest()).isNull();
        assertThat(rechazos).hasSize(1);
    }

    private JwtAuthenticationFilter filtro() {
        return new JwtAuthenticationFilter(jwtService, userDetailsService,
                (req, res, ex) -> rechazos.add(ex));
    }

    private SecurityPrincipal andrea(boolean activa) {
        return new SecurityPrincipal(1L, "andrea", "andrea@email.com", "$2a$10$hashFicticio", activa, Set.of(Role.USER));
    }
}
