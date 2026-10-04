package com.pulsepass;

import com.pulsepass.domain.User;
import com.pulsepass.domain.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class UserProfileTest extends PersistenceTestSupport {

    @Test
    void usuarioTieneUnPerfilQueSeRecuperaConSusDatos() {
        User user = crearUsuario("andrea");
        userProfileRepository.save(new UserProfile(
                user, "Andrea", "Perez", "3001112233", "Santa Marta", LocalDate.of(2000, 1, 1)
        ));
        flushAndClear();

        UserProfile perfil = userProfileRepository.findByUser_Id(user.getId()).orElseThrow();

        assertThat(perfil.getFirstName()).isEqualTo("Andrea");
        assertThat(perfil.getLastName()).isEqualTo("Perez");
        assertThat(perfil.getPhone()).isEqualTo("3001112233");
        assertThat(perfil.getCity()).isEqualTo("Santa Marta");
        assertThat(perfil.getBirthDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(perfil.getUser().getUsername()).isEqualTo("andrea");
    }

    @Test
    void segundoPerfilParaElMismoUsuarioEsRechazadoPorPostgres() {
        User user = crearUsuario("carlos");
        userProfileRepository.saveAndFlush(new UserProfile(
                user, "Carlos", "Gomez", "3000000000", "Bogota", LocalDate.of(1999, 5, 20)
        ));

        assertThatThrownBy(() -> userProfileRepository.saveAndFlush(
                new UserProfile(user, "Otro", "Perfil", null, null, null)
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void usernameDuplicadoEsRechazadoPorPostgres() {
        userRepository.saveAndFlush(new User("laura", "laura@example.com"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(new User("laura", "otra.laura@example.com"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void emailDuplicadoEsRechazadoPorPostgres() {
        userRepository.saveAndFlush(new User("miguel", "miguel@example.com"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(new User("miguel_2", "miguel@example.com"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void usuarioSeBuscaPorUsernameYPorEmailSinDistinguirMayusculas() {
        userRepository.save(new User("maria_test", "Maria.Test@Example.com"));
        flushAndClear();

        assertThat(userRepository.findByUsername("maria_test")).isPresent();

        User porEmail = userRepository.findByEmailIgnoreCase("MARIA.test@example.COM").orElseThrow();
        assertThat(porEmail.getUsername()).isEqualTo("maria_test");
        assertThat(porEmail.getActive()).isTrue();
    }
}