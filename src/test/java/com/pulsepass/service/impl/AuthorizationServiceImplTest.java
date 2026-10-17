package com.pulsepass.service.impl;

import com.pulsepass.domain.Role;
import com.pulsepass.security.SecurityPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorizationServiceImplTest {

    private final AuthorizationServiceImpl authorizationService = new AuthorizationServiceImpl();

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("El propietario accede a su recurso")
    void propietarioAccede() {
        autenticar(principal(1L, Role.USER));

        assertThatCode(() -> authorizationService.requireOwnerOrAnyRole(1L, Role.ADMIN))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SEC-007: un USER no accede al recurso de otro usuario")
    void terceroNoAccede() {
        autenticar(principal(1L, Role.USER));

        assertThatThrownBy(() -> authorizationService.requireOwnerOrAnyRole(2L, Role.ADMIN))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("STAFF accede a un recurso ajeno cuando la regla lo incluye")
    void staffAccedeSiLaReglaLoIncluye() {
        autenticar(principal(3L, Role.STAFF));

        assertThatCode(() -> authorizationService.requireOwnerOrAnyRole(2L, Role.STAFF, Role.ADMIN))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("STAFF no es propietario automatico cuando la regla solo admite ADMIN")
    void staffNoAccedeSiLaReglaSoloAdmiteAdmin() {
        autenticar(principal(3L, Role.STAFF));

        assertThatThrownBy(() -> authorizationService.requireOwnerOrAnyRole(2L, Role.ADMIN))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("ADMIN accede cuando la regla lo incluye")
    void adminAccede() {
        autenticar(principal(4L, Role.ADMIN));

        assertThatCode(() -> authorizationService.requireOwnerOrAnyRole(2L, Role.ADMIN))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Un propietario nulo nunca coincide: solo pasan los roles indicados")
    void propietarioNuloNuncaCoincide() {
        autenticar(principal(1L, Role.USER));

        assertThatThrownBy(() -> authorizationService.requireOwnerOrAnyRole(null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Sin identidad autenticada se niega el acceso")
    void sinIdentidadSeNiega() {
        assertThatThrownBy(() -> authorizationService.requireOwnerOrAnyRole(1L))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(authorizationService.hasAnyRole(Role.ADMIN)).isFalse();
    }

    @Test
    @DisplayName("hasAnyRole lee los roles del usuario autenticado")
    void hasAnyRoleLeeLosRoles() {
        autenticar(principal(3L, Role.STAFF));

        assertThat(authorizationService.hasAnyRole(Role.STAFF, Role.ADMIN)).isTrue();
        assertThat(authorizationService.hasAnyRole(Role.ADMIN)).isFalse();
    }

    private void autenticar(SecurityPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
    }

    private SecurityPrincipal principal(Long id, Role role) {
        return new SecurityPrincipal(id, "usuario" + id, "usuario" + id + "@email.com", "$2a$10$hash", true, Set.of(role));
    }
}
