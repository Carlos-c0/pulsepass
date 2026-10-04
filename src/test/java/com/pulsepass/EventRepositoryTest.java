package com.pulsepass;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventCategory;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Venue;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EventRepositoryTest extends PersistenceTestSupport {

    @Test
    void eventoSeRecuperaPorCodigoConSuVenueYaCargado() {
        Venue venue = venueRepository.save(new Venue(
                "VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Carrera 1 # 2-3", 5000
        ));
        crearEvento("CMF-2026", venue, EventStatus.PUBLISHED, FECHA_BASE);
        flushAndClear();

        Event evento = eventRepository.findByEventCode("CMF-2026").orElseThrow();

        // Con el contexto limpio, un venue LAZY llegaria como proxy sin inicializar.
        // El @EntityGraph lo trae en la misma consulta, asi que sirve fuera de la transaccion.
        PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(util.isLoaded(evento.getVenue())).isTrue();
        assertThat(evento.getVenue().getCode()).isEqualTo("VEN-SMR-01");
        assertThat(evento.getVenue().getCity()).isEqualTo("Santa Marta");
    }

    @Test
    void eventCodeDuplicadoEsRechazadoPorPostgres() {
        Venue venue = crearVenue("VEN-DUP-02", "Bogota");
        eventRepository.saveAndFlush(new Event(
                "EVT-DUP-01", "Primero", "Descripcion", EventCategory.MUSIC,
                EventStatus.DRAFT, FECHA_BASE, 18, venue
        ));

        assertThatThrownBy(() -> eventRepository.saveAndFlush(new Event(
                "EVT-DUP-01", "Segundo", "Descripcion", EventCategory.SPORTS,
                EventStatus.DRAFT, FECHA_BASE, 18, venue
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void eventoConVenueInexistenteEsRechazadoPorPostgres() {
        assertThatThrownBy(() -> em.createNativeQuery("""
                INSERT INTO events (event_code, name, category, status, event_date, venue_id)
                VALUES ('EVT-FK-01', 'Sin venue', 'MUSIC', 'DRAFT', NOW(), 999999)
                """).executeUpdate()
        ).isInstanceOf(PersistenceException.class);
    }

    @Test
    void categoriaYEstadoSeAlmacenanComoTextoLegible() {
        Venue venue = crearVenue("VEN-ENUM-01", "Bogota");
        crearEvento("EVT-ENUM-01", venue, EventStatus.SOLD_OUT, FECHA_BASE);
        flushAndClear();

        String categoria = (String) em.createNativeQuery(
                        "SELECT category FROM events WHERE event_code = :codigo")
                .setParameter("codigo", "EVT-ENUM-01")
                .getSingleResult();
        String estado = (String) em.createNativeQuery(
                        "SELECT status FROM events WHERE event_code = :codigo")
                .setParameter("codigo", "EVT-ENUM-01")
                .getSingleResult();

        assertThat(categoria).isEqualTo("MUSIC");
        assertThat(estado).isEqualTo("SOLD_OUT");
    }

    @Test
    void estadoFueraDelCatalogoEsRechazadoPorPostgres() {
        Venue venue = crearVenue("VEN-CHK-01", "Bogota");
        crearEvento("EVT-CHK-01", venue, EventStatus.DRAFT, FECHA_BASE);
        flushAndClear();

        assertThatThrownBy(() -> em.createNativeQuery(
                        "UPDATE events SET status = 'ARCHIVED' WHERE event_code = :codigo")
                .setParameter("codigo", "EVT-CHK-01")
                .executeUpdate()
        ).isInstanceOf(PersistenceException.class);
    }

    @Test
    void carteleraSoloTieneEventosPublicadosOrdenadosPorFecha() {
        Venue venue = crearVenue("VEN-CAR-01", "Bogota");
        crearEvento("EVT-PUB-2", venue, EventStatus.PUBLISHED, FECHA_BASE.plusDays(20));
        crearEvento("EVT-DRAFT", venue, EventStatus.DRAFT, FECHA_BASE.plusDays(1));
        crearEvento("EVT-PUB-1", venue, EventStatus.PUBLISHED, FECHA_BASE.plusDays(10));
        crearEvento("EVT-CANC", venue, EventStatus.CANCELLED, FECHA_BASE.plusDays(2));
        flushAndClear();

        List<Event> cartelera = eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED);

        assertThat(cartelera).extracting(Event::getEventCode).containsExactly("EVT-PUB-1", "EVT-PUB-2");
    }

    @Test
    void streamingUrlEsOpcionalYAceptaHasta500Caracteres() {
        Venue venue = crearVenue("VEN-URL-01", "Bogota");
        crearEvento("EVT-URL-01", venue, EventStatus.PUBLISHED, FECHA_BASE);
        Event conUrl = crearEvento("EVT-URL-02", venue, EventStatus.PUBLISHED, FECHA_BASE.plusDays(1));
        conUrl.setStreamingUrl("a".repeat(500));
        flushAndClear();

        Event sinUrlRecargado = eventRepository.findByEventCode("EVT-URL-01").orElseThrow();
        Event conUrlRecargado = eventRepository.findByEventCode("EVT-URL-02").orElseThrow();

        assertThat(sinUrlRecargado.getStreamingUrl()).isNull();
        assertThat(conUrlRecargado.getStreamingUrl()).hasSize(500);
    }

    @Test
    void streamingUrlDeMasDe500CaracteresEsRechazada() {
        Venue venue = crearVenue("VEN-URL-02", "Bogota");
        Event evento = crearEvento("EVT-URL-03", venue, EventStatus.PUBLISHED, FECHA_BASE);
        evento.setStreamingUrl("a".repeat(501));

        assertThatThrownBy(() -> eventRepository.flush()).isInstanceOf(DataAccessException.class);
    }
}