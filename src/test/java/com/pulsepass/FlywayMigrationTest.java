package com.pulsepass;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NFR-002, NFR-003, QT-001 y QT-002.
 * Si el contexto de Spring arranca, Flyway ya construyo el esquema desde una base vacia
 * y Hibernate lo valido (ddl-auto=validate). Aqui se comprueba de forma explicita.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FlywayMigrationTest extends PersistenceTestSupport {

    @Autowired
    private Flyway flyway;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Test
    void flywayAplicaV1V2yV3DesdeUnaBaseVacia() {
        MigrationInfo[] aplicadas = flyway.info().applied();

        List<String> versiones = Arrays.stream(aplicadas)
                .map(migracion -> migracion.getVersion().getVersion())
                .toList();

        assertThat(versiones).containsExactly("1", "2", "3");
        assertThat(aplicadas).allMatch(migracion -> migracion.getState() == MigrationState.SUCCESS);
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void hibernateSoloValidaElEsquema() {
        assertThat(ddlAuto).isEqualTo("validate");
    }

    @Test
    void v2InsertaElCatalogoInicialDeArtistas() {
        List<String> catalogo = List.of(
                "Solar Beat", "Neon Waves", "Caribbean Sound", "Ocean Drive", "Digital Pulse"
        );

        for (String stageName : catalogo) {
            assertThat(artistRepository.findByStageName(stageName))
                    .as("artista inicial %s", stageName)
                    .isPresent();
        }
    }
}
