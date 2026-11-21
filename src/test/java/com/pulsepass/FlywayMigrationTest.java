package com.pulsepass;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Esta clase levanta su propio contenedor (no el compartido de TestcontainersConfiguration)
// para comprobar las migraciones sobre una base que nadie mas ha tocado.
@Testcontainers
@SpringBootTest
class FlywayMigrationTest extends PersistenceTestSupport {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Test
    void flywayAplicaV1aV4DesdeUnaBaseVacia() {
        MigrationInfo[] aplicadas = flyway.info().applied();

        List<String> versiones = Arrays.stream(aplicadas)
                .map(migracion -> migracion.getVersion().getVersion())
                .toList();

        assertThat(versiones).containsExactly("1", "2", "3", "4");
        assertThat(aplicadas).allMatch(migracion -> migracion.getState() == MigrationState.SUCCESS);
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void laTablaDeHistorialDeFlywayRegistraLasCuatroMigraciones() {
        List<String> versiones = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank",
                String.class
        );

        assertThat(versiones).containsExactly("1", "2", "3", "4");
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
