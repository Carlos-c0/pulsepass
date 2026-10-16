package com.pulsepass;

import com.pulsepass.domain.Role;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.repository.UserCredentialRepository;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class UserCredentialPersistenceTest extends PersistenceTestSupport {

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Test
    void credencialSeGuardaConElMismoIdDelUsuarioYSusRoles() {
        User andrea = crearUsuario("andrea");
        userCredentialRepository.save(new UserCredential(
                andrea, "$2a$10$hashFicticioSoloParaLaPrueba", FECHA_COMPRA, Set.of(Role.USER, Role.STAFF)
        ));
        flushAndClear();

        UserCredential encontrada = userCredentialRepository.findById(andrea.getId()).orElseThrow();

        assertThat(encontrada.getUserId()).isEqualTo(andrea.getId());
        assertThat(encontrada.getPasswordHash()).startsWith("$2a$");
        assertThat(encontrada.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.STAFF);
    }

    @Test
    void usuarioSinCredencialNoTieneFilaEnUserCredentials() {
        User legado = crearUsuario("legado");
        flushAndClear();

        assertThat(userCredentialRepository.findById(legado.getId())).isEmpty();
    }

    @Test
    void rolFueraDelCatalogoEsRechazadoPorPostgres() {
        User andrea = crearUsuario("andrea");
        userCredentialRepository.saveAndFlush(new UserCredential(
                andrea, "$2a$10$hashFicticioSoloParaLaPrueba", FECHA_COMPRA, Set.of(Role.USER)
        ));

        assertThatThrownBy(() -> em.createNativeQuery(
                        "INSERT INTO user_roles (user_id, role) VALUES (:userId, 'ROOT')")
                .setParameter("userId", andrea.getId())
                .executeUpdate())
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    void rolRepetidoParaElMismoUsuarioEsRechazadoPorPostgres() {
        User andrea = crearUsuario("andrea");
        userCredentialRepository.saveAndFlush(new UserCredential(
                andrea, "$2a$10$hashFicticioSoloParaLaPrueba", FECHA_COMPRA, Set.of(Role.USER)
        ));

        assertThatThrownBy(() -> em.createNativeQuery(
                        "INSERT INTO user_roles (user_id, role) VALUES (:userId, 'USER')")
                .setParameter("userId", andrea.getId())
                .executeUpdate())
                .isInstanceOf(PersistenceException.class);
    }
}
