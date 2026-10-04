package com.pulsepass.service.impl;

import com.pulsepass.domain.User;
import com.pulsepass.domain.UserProfile;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.UserMapper;
import com.pulsepass.repository.UserProfileRepository;
import com.pulsepass.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final LocalDate FECHA_NACIMIENTO = LocalDate.of(2005, 3, 10);

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // ---------- register ----------

    @Test
    @DisplayName("TEST-USER-001: registrar usuario valido guarda User y UserProfile")
    void registrarUsuarioValidoGuardaUsuarioYPerfil() {
        // ARRANGE
        RegisterUserRequest request = solicitud(FECHA_NACIMIENTO);
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class), any(UserProfile.class)))
                .thenAnswer(invocation -> respuestaDe(invocation.getArgument(0), invocation.getArgument(1)));

        // ACT
        UserResponse resultado = userService.register(request);

        // ASSERT
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userRepository).save(userCaptor.capture());
        verify(userProfileRepository).save(profileCaptor.capture());

        User usuarioGuardado = userCaptor.getValue();
        UserProfile perfilGuardado = profileCaptor.getValue();

        assertThat(usuarioGuardado.getUsername()).isEqualTo("andrea");
        assertThat(usuarioGuardado.getEmail()).isEqualTo("andrea@email.com");
        assertThat(usuarioGuardado.getActive()).isTrue();
        assertThat(perfilGuardado.getUser()).isSameAs(usuarioGuardado);
        assertThat(perfilGuardado.getFirstName()).isEqualTo("Andrea");
        assertThat(perfilGuardado.getBirthDate()).isEqualTo(FECHA_NACIMIENTO);
        assertThat(resultado.username()).isEqualTo("andrea");
        assertThat(resultado.active()).isTrue();
        assertThat(resultado.city()).isEqualTo("Santa Marta");
    }

    @Test
    @DisplayName("TEST-USER-002: username duplicado lanza DuplicateResourceException")
    void usernameDuplicadoLanzaDuplicateResource() {
        // ARRANGE
        when(userRepository.existsByUsername("andrea")).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.register(solicitud(FECHA_NACIMIENTO)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username already exists: andrea");

        verify(userRepository, never()).save(any());
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-USER-003: email duplicado lanza DuplicateResourceException")
    void emailDuplicadoLanzaDuplicateResource() {
        // ARRANGE
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.register(solicitud(FECHA_NACIMIENTO)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists: andrea@email.com");

        verify(userRepository, never()).save(any());
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-USER-004: birthDate futura lanza BusinessRuleException")
    void fechaDeNacimientoFuturaLanzaBusinessRule() {
        // ARRANGE
        LocalDate manana = LocalDate.now().plusDays(1);
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(false);

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.register(solicitud(manana)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Birth date cannot be in the future.");

        verify(userRepository, never()).save(any());
        verify(userProfileRepository, never()).save(any());
    }

    // ---------- consultas ----------

    @Test
    @DisplayName("FR-SVC-011: usuario existente por email retorna DTO con su perfil")
    void usuarioExistentePorEmailRetornaDtoConPerfil() {
        // ARRANGE
        User usuario = usuario(1L);
        UserProfile perfil = perfil(usuario);
        when(userRepository.findByEmailIgnoreCase("ANDREA@EMAIL.COM")).thenReturn(Optional.of(usuario));
        when(userProfileRepository.findByUser_Id(1L)).thenReturn(Optional.of(perfil));
        when(userMapper.toResponse(usuario, perfil)).thenReturn(respuestaDe(usuario, perfil));

        // ACT
        UserResponse resultado = userService.findByEmail("ANDREA@EMAIL.COM");

        // ASSERT
        assertThat(resultado.email()).isEqualTo("andrea@email.com");
        assertThat(resultado.firstName()).isEqualTo("Andrea");
        assertThat(resultado.birthDate()).isEqualTo(FECHA_NACIMIENTO);
    }

    @Test
    @DisplayName("FR-SVC-011: usuario inexistente por email lanza ResourceNotFoundException")
    void usuarioInexistentePorEmailLanzaResourceNotFound() {
        // ARRANGE
        when(userRepository.findByEmailIgnoreCase("nadie@email.com")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.findByEmail("nadie@email.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: nadie@email.com");

        verify(userMapper, never()).toResponse(any(), any());
    }

    @Test
    @DisplayName("FR-SVC-012: usuario existente por username retorna DTO")
    void usuarioExistentePorUsernameRetornaDto() {
        // ARRANGE
        User usuario = usuario(1L);
        UserProfile perfil = perfil(usuario);
        when(userRepository.findByUsername("andrea")).thenReturn(Optional.of(usuario));
        when(userProfileRepository.findByUser_Id(1L)).thenReturn(Optional.of(perfil));
        when(userMapper.toResponse(usuario, perfil)).thenReturn(respuestaDe(usuario, perfil));

        // ACT
        UserResponse resultado = userService.findByUsername("andrea");

        // ASSERT
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.username()).isEqualTo("andrea");
    }

    @Test
    @DisplayName("FR-SVC-012: usuario inexistente por username lanza ResourceNotFoundException")
    void usuarioInexistentePorUsernameLanzaResourceNotFound() {
        // ARRANGE
        when(userRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.findByUsername("nadie"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: nadie");
    }

    // ---------- datos de apoyo ----------

    private RegisterUserRequest solicitud(LocalDate fechaNacimiento) {
        return new RegisterUserRequest(
                "andrea", "andrea@email.com", "Andrea", "Gomez",
                "3001234567", "Santa Marta", fechaNacimiento
        );
    }

    private User usuario(Long id) {
        User usuario = new User("andrea", "andrea@email.com");
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private UserProfile perfil(User usuario) {
        return new UserProfile(usuario, "Andrea", "Gomez", "3001234567", "Santa Marta", FECHA_NACIMIENTO);
    }

    private UserResponse respuestaDe(User usuario, UserProfile perfil) {
        return new UserResponse(
                usuario.getId(), usuario.getUsername(), usuario.getEmail(), usuario.getActive(),
                perfil.getFirstName(), perfil.getLastName(), perfil.getPhone(),
                perfil.getCity(), perfil.getBirthDate()
        );
    }
}
