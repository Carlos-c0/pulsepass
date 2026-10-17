package com.pulsepass.service.impl;

import com.pulsepass.domain.Role;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.SecurityPrincipal;
import com.pulsepass.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String CONTRASENA = "UnaFraseSeguraLarga2026!";

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    // ---------- registro ----------

    @Test
    @DisplayName("FR-SEC-AUTH-001: el registro guarda la credencial con el hash y el rol USER")
    void registroGuardaCredencialConHashYRolUser() {
        // ARRANGE
        User andrea = new User("andrea", "andrea@email.com");
        when(userService.register(any(RegisterUserRequest.class))).thenReturn(respuestaAndrea());
        when(userRepository.getReferenceById(1L)).thenReturn(andrea);
        when(passwordEncoder.encode(CONTRASENA)).thenReturn("$2a$10$hashGenerado");

        // ACT
        UserResponse resultado = authService.register(solicitud());

        // ASSERT
        ArgumentCaptor<UserCredential> captor = ArgumentCaptor.forClass(UserCredential.class);
        verify(userCredentialRepository).save(captor.capture());
        UserCredential guardada = captor.getValue();

        assertThat(guardada.getUser()).isSameAs(andrea);
        assertThat(guardada.getPasswordHash()).isEqualTo("$2a$10$hashGenerado");
        assertThat(guardada.getPasswordHash()).isNotEqualTo(CONTRASENA);
        assertThat(guardada.getRoles()).containsExactly(Role.USER);
        assertThat(guardada.getPasswordChangedAt()).isNotNull();
        assertThat(resultado.username()).isEqualTo("andrea");
    }

    @Test
    @DisplayName("NFR-SEC-004: si UserService rechaza el registro, no se crea credencial")
    void registroRechazadoNoCreaCredencial() {
        // ARRANGE
        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new DuplicateResourceException("Username already exists: andrea"));

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.register(solicitud()))
                .isInstanceOf(DuplicateResourceException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userCredentialRepository, never()).save(any());
    }

    // ---------- login ----------

    @Test
    @DisplayName("FR-SEC-AUTH-002: login correcto retorna un Bearer de 900 segundos")
    void loginCorrectoRetornaToken() {
        // ARRANGE
        SecurityPrincipal andrea = new SecurityPrincipal(
                1L, "andrea", "andrea@email.com", "$2a$10$hash", true, Set.of(Role.USER));
        when(authenticationManager.authenticate(any()))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated(andrea, null, andrea.getAuthorities()));
        when(jwtService.generateToken(1L)).thenReturn("jwt-firmado");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(900L);

        // ACT
        AuthResponse respuesta = authService.login(new LoginRequest("andrea", CONTRASENA));

        // ASSERT
        assertThat(respuesta.accessToken()).isEqualTo("jwt-firmado");
        assertThat(respuesta.tokenType()).isEqualTo("Bearer");
        assertThat(respuesta.expiresIn()).isEqualTo(900L);
    }

    @Test
    @DisplayName("FR-SEC-AUTH-003: contrasena incorrecta lanza un 401 generico y no emite token")
    void contrasenaIncorrectaNoEmiteToken() {
        // ARRANGE
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.login(new LoginRequest("andrea", "otraContrasenaLarga")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("FR-SEC-AUTH-004: un usuario inactivo recibe el mismo mensaje generico")
    void usuarioInactivoRecibeMensajeGenerico() {
        // ARRANGE
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("User is disabled"));

        // ACT + ASSERT: el mensaje no revela que la cuenta existe pero esta inactiva
        assertThatThrownBy(() -> authService.login(new LoginRequest("miguel", CONTRASENA)))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(jwtService, never()).generateToken(any());
    }

    // ---------- usuario autenticado ----------

    @Test
    @DisplayName("FR-SEC-AUTH-010: /me retorna id, username, email y roles, sin secretos")
    void usuarioActualSinSecretos() {
        // ARRANGE
        SecurityPrincipal laura = new SecurityPrincipal(
                3L, "laura", "laura@email.com", "$2a$10$hash", true, Set.of(Role.USER, Role.STAFF));

        // ACT
        AuthenticatedUserResponse respuesta = authService.currentUser(laura);

        // ASSERT
        assertThat(respuesta.id()).isEqualTo(3L);
        assertThat(respuesta.username()).isEqualTo("laura");
        assertThat(respuesta.roles()).containsExactly("STAFF", "USER");
    }

    private RegisterUserRequest solicitud() {
        return new RegisterUserRequest(
                "andrea", "andrea@email.com", "Andrea", "Gomez",
                "3001234567", "Santa Marta", LocalDate.of(2005, 3, 10), CONTRASENA
        );
    }

    private UserResponse respuestaAndrea() {
        return new UserResponse(
                1L, "andrea", "andrea@email.com", true, "Andrea", "Gomez",
                "3001234567", "Santa Marta", LocalDate.of(2005, 3, 10)
        );
    }
}
