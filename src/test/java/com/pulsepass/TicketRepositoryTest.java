package com.pulsepass;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.TicketStatus;
import com.pulsepass.domain.TicketType;
import com.pulsepass.domain.User;
import com.pulsepass.domain.Venue;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** FR-TKT-001 a FR-TKT-008, FR-SRC-004, AC-005, AC-008, UC-05, UC-08, QT-006 a QT-008. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class TicketRepositoryTest extends PersistenceTestSupport {

    private Event crearEventoCmf() {
        Venue venue = crearVenue("VEN-SMR-01", "Santa Marta");
        return crearEvento("CMF-2026", venue, EventStatus.PUBLISHED, FECHA_BASE);
    }

    /** Tickets de ejemplo del PRD (seccion 16.3) sobre el evento CMF-2026. */
    private Event crearEscenarioDeVentas() {
        Event cmf = crearEventoCmf();
        crearTicket("TCK-0001", TicketType.VIP, "250000.00", TicketStatus.PAID, crearUsuario("andrea"), cmf);
        crearTicket("TCK-0002", TicketType.GENERAL, "120000.00", TicketStatus.PAID, crearUsuario("carlos"), cmf);
        crearTicket("TCK-0003", TicketType.GENERAL, "120000.00", TicketStatus.RESERVED, crearUsuario("laura"), cmf);
        crearTicket("TCK-0004", TicketType.VIP, "250000.00", TicketStatus.CANCELLED, crearUsuario("miguel"), cmf);
        return cmf;
    }

    @Test
    void ticketSeAsociaAUsuarioYEvento() {
        Event evento = crearEventoCmf();
        User andrea = crearUsuario("andrea");
        Ticket ticket = crearTicket("TCK-0001", TicketType.VIP, "250000.00", TicketStatus.PAID, andrea, evento);
        flushAndClear();

        Ticket encontrado = ticketRepository.findById(ticket.getId()).orElseThrow();

        assertThat(encontrado.getUser().getUsername()).isEqualTo("andrea");
        assertThat(encontrado.getEvent().getEventCode()).isEqualTo("CMF-2026");
        assertThat(encontrado.getType()).isEqualTo(TicketType.VIP);
        assertThat(encontrado.getStatus()).isEqualTo(TicketStatus.PAID);
        assertThat(encontrado.getPrice()).isEqualByComparingTo("250000.00");
    }

    @Test
    void ticketCodeDuplicadoEsRechazadoPorPostgres() {
        Event evento = crearEventoCmf();
        User andrea = crearUsuario("andrea");
        User carlos = crearUsuario("carlos");
        crearTicket("TCK-0001", TicketType.VIP, "250000.00", TicketStatus.PAID, andrea, evento);

        assertThatThrownBy(() -> ticketRepository.saveAndFlush(new Ticket(
                "TCK-0001", TicketType.GENERAL, new BigDecimal("120000.00"),
                TicketStatus.RESERVED, FECHA_COMPRA, carlos, evento
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void precioNegativoEsRechazadoPorPostgres() {
        Event evento = crearEventoCmf();
        User user = crearUsuario("andrea");

        assertThatThrownBy(() -> ticketRepository.saveAndFlush(new Ticket(
                "TCK-NEG", TicketType.GENERAL, new BigDecimal("-1.00"),
                TicketStatus.RESERVED, FECHA_COMPRA, user, evento
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void precioCeroEsValido() {
        Event evento = crearEventoCmf();
        User user = crearUsuario("andrea");

        Ticket gratis = ticketRepository.saveAndFlush(new Ticket(
                "TCK-FREE", TicketType.STUDENT, BigDecimal.ZERO,
                TicketStatus.RESERVED, FECHA_COMPRA, user, evento
        ));

        assertThat(gratis.getId()).isNotNull();
    }

    @Test
    void ticketConUsuarioYEventoInexistentesEsRechazadoPorPostgres() {
        assertThatThrownBy(() -> em.createNativeQuery("""
                INSERT INTO tickets (ticket_code, type, price, status, purchase_date, user_id, event_id)
                VALUES ('TCK-FK-01', 'GENERAL', 100, 'PAID', NOW(), 999999, 999999)
                """).executeUpdate()
        ).isInstanceOf(PersistenceException.class);
    }

    @Test
    void tipoYEstadoSeAlmacenanComoTextoLegible() {
        Event evento = crearEventoCmf();
        crearTicket("TCK-0001", TicketType.BACKSTAGE, "300000.00", TicketStatus.USED, crearUsuario("andrea"), evento);
        flushAndClear();

        String tipo = em.createNativeQuery(
                        "SELECT type FROM tickets WHERE ticket_code = :codigo", String.class)
                .setParameter("codigo", "TCK-0001")
                .getSingleResult();
        String estado = em.createNativeQuery(
                        "SELECT status FROM tickets WHERE ticket_code = :codigo", String.class)
                .setParameter("codigo", "TCK-0001")
                .getSingleResult();

        assertThat(tipo).isEqualTo("BACKSTAGE");
        assertThat(estado).isEqualTo("USED");
    }

    @Test
    void estadoDeTicketFueraDelCatalogoEsRechazadoPorPostgres() {
        Event evento = crearEventoCmf();
        crearTicket("TCK-0001", TicketType.GENERAL, "120000.00", TicketStatus.RESERVED, crearUsuario("andrea"), evento);
        flushAndClear();

        assertThatThrownBy(() -> em.createNativeQuery(
                        "UPDATE tickets SET status = 'REFUNDED' WHERE ticket_code = :codigo")
                .setParameter("codigo", "TCK-0001")
                .executeUpdate()
        ).isInstanceOf(PersistenceException.class);
    }

    @Test
    void ticketsDeUnUsuarioSeBuscanPorEmailYOpcionalmentePorEstado() {
        Event evento = crearEventoCmf();
        User andrea = crearUsuario("andrea");
        User carlos = crearUsuario("carlos");
        crearTicket("TCK-A1", TicketType.GENERAL, "120000.00", TicketStatus.PAID, andrea, evento);
        crearTicket("TCK-A2", TicketType.VIP, "250000.00", TicketStatus.RESERVED, andrea, evento);
        crearTicket("TCK-C1", TicketType.GENERAL, "120000.00", TicketStatus.PAID, carlos, evento);
        flushAndClear();

        List<Ticket> todos = ticketRepository.findByUser_EmailIgnoreCase("ANDREA@EXAMPLE.COM");
        List<Ticket> pagados = ticketRepository.findByUser_EmailIgnoreCaseAndStatus("Andrea@Example.com", TicketStatus.PAID);

        assertThat(todos).extracting(Ticket::getTicketCode).containsExactlyInAnyOrder("TCK-A1", "TCK-A2");
        assertThat(pagados).extracting(Ticket::getTicketCode).containsExactly("TCK-A1");
    }

    @Test
    void ticketsPagadosDeUnEventoExcluyenReservadosYCancelados() {
        crearEscenarioDeVentas();
        flushAndClear();

        List<Ticket> pagados = ticketRepository.findByEvent_EventCodeAndStatus("CMF-2026", TicketStatus.PAID);

        assertThat(pagados).extracting(Ticket::getTicketCode).containsExactlyInAnyOrder("TCK-0001", "TCK-0002");
    }

    @Test
    void conteoDeVentasSoloIncluyeTicketsPagadosDelEventoSolicitado() {
        Event cmf = crearEscenarioDeVentas();
        Event otro = crearEvento("EVT-OTRO", cmf.getVenue(), EventStatus.PUBLISHED, FECHA_BASE.plusDays(1));
        crearTicket("TCK-OTRO", TicketType.GENERAL, "50000.00", TicketStatus.PAID, crearUsuario("otro"), otro);
        crearEvento("EVT-VACIO", cmf.getVenue(), EventStatus.PUBLISHED, FECHA_BASE.plusDays(2));
        flushAndClear();

        assertThat(ticketRepository.countPaidTicketsByEventCode("CMF-2026")).isEqualTo(2);
        assertThat(ticketRepository.countPaidTicketsByEventCode("EVT-OTRO")).isEqualTo(1);
        assertThat(ticketRepository.countPaidTicketsByEventCode("EVT-VACIO")).isZero();
    }

    @Test
    void ticketsDeEventosFuturosQuedanOrdenadosCronologicamente() {
        Venue venue = crearVenue("VEN-FUT-01", "Bogota");
        Event lejano = crearEvento("EVT-LEJANO", venue, EventStatus.PUBLISHED, FECHA_BASE.plusDays(30));
        Event cercano = crearEvento("EVT-CERCANO", venue, EventStatus.PUBLISHED, FECHA_BASE.plusDays(10));
        Event pasado = crearEvento("EVT-PASADO", venue, EventStatus.FINISHED, FECHA_BASE.minusDays(10));
        User user = crearUsuario("andrea");
        crearTicket("TCK-LEJANO", TicketType.GENERAL, "100000.00", TicketStatus.PAID, user, lejano);
        crearTicket("TCK-CERCANO", TicketType.GENERAL, "100000.00", TicketStatus.PAID, user, cercano);
        crearTicket("TCK-PASADO", TicketType.GENERAL, "100000.00", TicketStatus.USED, user, pasado);
        flushAndClear();

        List<Ticket> tickets = ticketRepository.findTicketsOfFutureEvents(FECHA_BASE);

        assertThat(tickets).extracting(Ticket::getTicketCode).containsExactly("TCK-CERCANO", "TCK-LEJANO");
    }
}
