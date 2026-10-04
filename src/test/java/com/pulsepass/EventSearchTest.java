package com.pulsepass;

import com.pulsepass.domain.Artist;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Venue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EventSearchTest extends PersistenceTestSupport {

    @Test
    void buscarPorArtistaDevuelveCadaEventoUnaSolaVez() {
        Venue venue = crearVenue("VEN-SMR-01", "Santa Marta");
        Artist solarBeat = artistaInicial("Solar Beat");
        Artist neonWaves = artistaInicial("Neon Waves");
        crearEvento("EVT-SB-1", venue, EventStatus.PUBLISHED, FECHA_BASE, solarBeat);
        crearEvento("EVT-SB-2", venue, EventStatus.PUBLISHED, FECHA_BASE.plusDays(1), solarBeat, neonWaves);
        crearEvento("EVT-SB-3", venue, EventStatus.DRAFT, FECHA_BASE.plusDays(2), solarBeat, neonWaves);
        crearEvento("EVT-NW-1", venue, EventStatus.PUBLISHED, FECHA_BASE.plusDays(3), neonWaves);
        flushAndClear();

        List<Event> eventos = eventRepository.findByArtistStageName("Solar Beat");

        assertThat(eventos)
                .extracting(Event::getEventCode)
                .containsExactlyInAnyOrder("EVT-SB-1", "EVT-SB-2", "EVT-SB-3");
    }

    @Test
    void artistaSinEventosDevuelveListaVacia() {
        assertThat(eventRepository.findByArtistStageName("Digital Pulse")).isEmpty();
    }

    @Test
    void buscarPorCiudadYArtistaFiltraPorAmbos() {
        Venue santaMarta = crearVenue("VEN-SMR-01", "Santa Marta");
        Venue bogota = crearVenue("VEN-BOG-01", "Bogota");
        Artist solarBeat = artistaInicial("Solar Beat");
        Artist neonWaves = artistaInicial("Neon Waves");
        crearEvento("EVT-SMR-SB", santaMarta, EventStatus.PUBLISHED, FECHA_BASE, solarBeat);
        crearEvento("EVT-BOG-SB", bogota, EventStatus.PUBLISHED, FECHA_BASE.plusDays(1), solarBeat);
        crearEvento("EVT-SMR-NW", santaMarta, EventStatus.PUBLISHED, FECHA_BASE.plusDays(2), neonWaves);
        flushAndClear();

        List<Event> eventos = eventRepository.findByVenueCityAndArtistStageName("Santa Marta", "Solar Beat");

        assertThat(eventos).extracting(Event::getEventCode).containsExactly("EVT-SMR-SB");
    }

    @Test
    void eventosRecomendadosCumplenTodosLosFiltrosSinDuplicadosYOrdenadosPorFecha() {
        Venue santaMarta = crearVenue("VEN-SMR-01", "Santa Marta");
        Venue bogota = crearVenue("VEN-BOG-01", "Bogota");
        Artist solarBeat = artistaInicial("Solar Beat");
        Artist solarWind = crearArtista("Solar Wind");
        Artist neonWaves = artistaInicial("Neon Waves");

        crearEvento("REC-OK-2", santaMarta, EventStatus.PUBLISHED, FECHA_BASE.plusDays(20), solarBeat, solarWind);
        crearEvento("REC-OK-1", santaMarta, EventStatus.PUBLISHED, FECHA_BASE.plusDays(10), solarWind);
        crearEvento("REC-BORRADOR", santaMarta, EventStatus.DRAFT, FECHA_BASE.plusDays(11), solarWind);
        crearEvento("REC-PASADO", santaMarta, EventStatus.PUBLISHED, FECHA_BASE.minusDays(1), solarWind);
        crearEvento("REC-OTRA-CIUDAD", bogota, EventStatus.PUBLISHED, FECHA_BASE.plusDays(12), solarWind);
        crearEvento("REC-OTRO-ARTISTA", santaMarta, EventStatus.PUBLISHED, FECHA_BASE.plusDays(13), neonWaves);
        flushAndClear();

        List<Event> recomendados = eventRepository.findRecommendedEvents(FECHA_BASE, "Santa Marta", "SOLAR");

        assertThat(recomendados).extracting(Event::getEventCode).containsExactly("REC-OK-1", "REC-OK-2");
    }
}