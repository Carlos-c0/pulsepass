package com.pulsepass.security;

import com.pulsepass.domain.Role;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("Login por username carga la identidad con su hash y sus roles")
    void cargaPorUsername() {
        // ARRANGE
        User andrea = usuario(1L, "andrea");
        when(userRepository.findByUsername("andrea")).thenReturn(Optional.of(andrea));
        when(userCredentialRepository.findById(1L)).thenReturn(Optional.of(credencial(andrea, Role.USER)));

        // ACT
        SecurityPrincipal principal = userDetailsService.loadUserByUsername("andrea");

        // ASSERT
        assertThat(principal.userId()).isEqualTo(1L);
        assertThat(principal.getUsername()).isEqualTo("andrea");
        assertThat(principal.getPassword()).isEqualTo("$2a$10$hashFicticio");
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Login por email: si no hay username igual, se busca por email sin distinguir mayusculas")
    void cargaPorEmail() {
        // ARRANGE
        User andrea = usuario(1L, "andrea");
        when(userRepository.findByUsername("ANDREA@EMAIL.COM")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("ANDREA@EMAIL.COM")).thenReturn(Optional.of(andrea));
        when(userCredentialRepository.findById(1L)).thenReturn(Optional.of(credencial(andrea, Role.USER)));

        // ACT
        SecurityPrincipal principal = userDetailsService.loadUserByUsername("ANDREA@EMAIL.COM");

        // ASSERT
        assertThat(principal.email()).isEqualTo("andrea@email.com");
    }

    @Test
    @DisplayName("Usuario inexistente lanza UsernameNotFoundException con mensaje generico")
    void usuarioInexistente() {
        // ARRANGE
        when(userRepository.findByUsername("nadie")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("nadie")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nadie"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("Usuario anterior a la Fase 4 sin credencial no puede autenticarse")
    void usuarioLegadoSinCredencial() {
        // ARRANGE
        User legado = usuario(5L, "legado");
        when(userRepository.findByUsername("legado")).thenReturn(Optional.of(legado));
        when(userCredentialRepository.findById(5L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("legado"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("Usuario inactivo se carga deshabilitado")
    void usuarioInactivo() {
        // ARRANGE
        User miguel = usuario(6L, "miguel");
        miguel.setActive(false);
        when(userRepository.findById(6L)).thenReturn(Optional.of(miguel));
        when(userCredentialRepository.findById(6L)).thenReturn(Optional.of(credencial(miguel, Role.USER)));

        // ACT
        SecurityPrincipal principal = userDetailsService.loadUserById(6L);

        // ASSERT
        assertThat(principal.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Por id se cargan los roles vigentes en la base de datos")
    void cargaPorIdConRolesVigentes() {
        // ARRANGE
        User laura = usuario(3L, "laura");
        when(userRepository.findById(3L)).thenReturn(Optional.of(laura));
        when(userCredentialRepository.findById(3L)).thenReturn(Optional.of(credencial(laura, Role.USER, Role.STAFF)));

        // ACT
        SecurityPrincipal principal = userDetailsService.loadUserById(3L);

        // ASSERT
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_STAFF");
    }

    @Test
    @DisplayName("SEC-003: toString del principal no muestra el hash")
    void toStringNoMuestraElHash() {
        SecurityPrincipal principal = new SecurityPrincipal(
                1L, "andrea", "andrea@email.com", "$2a$10$hashFicticio", true, Set.of(Role.USER));

        assertThat(principal.toString()).doesNotContain("$2a$10$hashFicticio");
    }

    private User usuario(Long id, String username) {
        User user = new User(username, username + "@email.com");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UserCredential credencial(User user, Role... roles) {
        return new UserCredential(user, "$2a$10$hashFicticio", LocalDateTime.of(2026, 10, 1, 8, 0), Set.of(roles));
    }
}
