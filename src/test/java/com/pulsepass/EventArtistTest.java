package com.pulsepass;

import com.pulsepass.domain.Artist;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Venue;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EventArtistTest extends PersistenceTestSupport {

    @Test
    void artistaSePersisteYSeRecupera() {
        Artist artista = crearArtista("Artista Nuevo");
        flushAndClear();

        Artist porId = artistRepository.findById(artista.getId()).orElseThrow();
        Artist porNombre = artistRepository.findByStageName("Artista Nuevo").orElseThrow();

        assertThat(porId.getStageName()).isEqualTo("Artista Nuevo");
        assertThat(porNombre.getId()).isEqualTo(artista.getId());
        assertThat(porNombre.getActive()).isTrue();
    }

    @Test
    void stageNameDuplicadoEsRechazadoPorPostgres() {
        // "Solar Beat" ya existe: lo inserta la migracion V2
        assertThatThrownBy(() ->
                artistRepository.saveAndFlush(new Artist("Solar Beat", "Colombia", "Pop"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void eventoConTresArtistasPersisteLaAsociacionSinDuplicarPares() {
        Venue venue = crearVenue("VEN-SMR-01", "Santa Marta");
        Artist solarBeat = artistaInicial("Solar Beat");
        Artist neonWaves = artistaInicial("Neon Waves");
        Artist caribbeanSound = artistaInicial("Caribbean Sound");

        Event evento = crearEvento("CMF-2026", venue, EventStatus.PUBLISHED, FECHA_BASE,
                solarBeat, neonWaves, caribbeanSound);
        evento.addArtist(solarBeat); // mismo par evento-artista: el Set no lo repite
        flushAndClear();

        Event recargado = eventRepository.findByEventCode("CMF-2026").orElseThrow();

        assertThat(recargado.getArtists())
                .extracting(Artist::getStageName)
                .containsExactlyInAnyOrder("Solar Beat", "Neon Waves", "Caribbean Sound");
    }

    @Test
    void parEventoArtistaDuplicadoEsRechazadoPorPostgres() {
        Venue venue = crearVenue("VEN-PAR-01", "Bogota");
        Artist artista = crearArtista("Artista Par");
        Event evento = crearEvento("EVT-PAR-01", venue, EventStatus.PUBLISHED, FECHA_BASE, artista);
        flushAndClear();

        // La clave primaria compuesta (event_id, artist_id) impide repetir el par
        assertThatThrownBy(() -> em.createNativeQuery(
                        "INSERT INTO event_artists (event_id, artist_id) VALUES (:evento, :artista)")
                .setParameter("evento", evento.getId())
                .setParameter("artista", artista.getId())
                .executeUpdate()
        ).isInstanceOf(PersistenceException.class);
    }

    @Test
    void artistaPuedeParticiparEnVariosEventos() {
        Venue venue = crearVenue("VEN-VAR-01", "Bogota");
        Artist artista = crearArtista("Artista Gira");
        crearEvento("EVT-VAR-1", venue, EventStatus.PUBLISHED, FECHA_BASE, artista);
        crearEvento("EVT-VAR-2", venue, EventStatus.PUBLISHED, FECHA_BASE.plusDays(1), artista);
        flushAndClear();

        assertThat(eventRepository.findByArtistStageName("Artista Gira"))
                .extracting(Event::getEventCode)
                .containsExactlyInAnyOrder("EVT-VAR-1", "EVT-VAR-2");
    }
}