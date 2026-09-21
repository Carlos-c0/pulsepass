package com.pulsepass;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Venue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** FR-VEN-001 a FR-VEN-004, AC-001, UC-01, QT-003 y QT-009. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class VenuePersistenceTest extends PersistenceTestSupport {

    @Test
    void venueSePersisteYSeRecuperaPorIdYPorCodigo() {
        Venue venue = venueRepository.save(new Venue(
                "VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Carrera 1 # 2-3", 5000
        ));
        flushAndClear();

        Venue porId = venueRepository.findById(venue.getId()).orElseThrow();
        Venue porCodigo = venueRepository.findByCode("VEN-SMR-01").orElseThrow();

        assertThat(porId.getCode()).isEqualTo("VEN-SMR-01");
        assertThat(porCodigo.getId()).isEqualTo(venue.getId());
        assertThat(porCodigo.getCapacity()).isPositive();
        assertThat(porCodigo.getActive()).isTrue();
    }

    @Test
    void codigoDeVenueDuplicadoEsRechazadoPorPostgres() {
        venueRepository.saveAndFlush(new Venue("VEN-DUP-01", "Primer Venue", "Bogota", "Calle 5", 300));

        assertThatThrownBy(() ->
                venueRepository.saveAndFlush(new Venue("VEN-DUP-01", "Segundo Venue", "Medellin", "Calle 6", 400))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void capacidadIgualOMenorQueCeroEsRechazadaPorPostgres(int capacidad) {
        assertThatThrownBy(() ->
                venueRepository.saveAndFlush(new Venue("VEN-CAP-01", "Sin cupo", "Bogota", "Calle 7", capacidad))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void venueTieneMuchosEventosYLaConsultaPorCodigoSoloDevuelveLosSuyos() {
        Venue venueA = crearVenue("VEN-A-01", "Bogota");
        Venue venueB = crearVenue("VEN-B-01", "Medellin");
        crearEvento("EVT-A-2", venueA, EventStatus.PUBLISHED, FECHA_BASE.plusDays(20));
        crearEvento("EVT-A-1", venueA, EventStatus.PUBLISHED, FECHA_BASE.plusDays(10));
        crearEvento("EVT-B-1", venueB, EventStatus.PUBLISHED, FECHA_BASE.plusDays(5));
        flushAndClear();

        List<Event> eventos = eventRepository.findByVenue_CodeOrderByEventDateAsc("VEN-A-01");

        assertThat(eventos).extracting(Event::getEventCode).containsExactly("EVT-A-1", "EVT-A-2");
        assertThat(eventos).allSatisfy(evento ->
                assertThat(evento.getVenue().getCode()).isEqualTo("VEN-A-01"));
    }
}
