package com.pulsepass;

import com.pulsepass.domain.*;
import com.pulsepass.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class PersistenceIntegrationTest {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void venueTieneMuchosEventos() {
        Venue venue = venueRepository.save(
                new Venue("VEN-TEST-01", "Test Arena", "Bogota", "Calle 1", 1000)
        );

        eventRepository.save(new Event(
                "EVT-TEST-01", "Evento Uno", "Descripcion", EventCategory.MUSIC,
                EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 18, venue
        ));
        eventRepository.save(new Event(
                "EVT-TEST-02", "Evento Dos", "Descripcion", EventCategory.MUSIC,
                EventStatus.PUBLISHED, LocalDateTime.now().plusDays(20), 18, venue
        ));

        List<Event> eventos = eventRepository.findByVenue_CodeOrderByEventDateAsc("VEN-TEST-01");

        assertThat(eventos).hasSize(2);
        assertThat(eventos.get(0).getVenue().getCode()).isEqualTo("VEN-TEST-01");
    }


    @Test
    void userTieneUnUnicoUserProfile() {
        User user = userRepository.save(new User("carlos_test", "carlos.test@example.com"));

        UserProfile profile = userProfileRepository.save(
                new UserProfile(user, "Carlos", "Crespo", "3000000000", "Santa Marta", LocalDate.of(2000, 1, 1))
        );

        Optional<UserProfile> encontrado = userProfileRepository.findByUser_Id(user.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getFirstName()).isEqualTo("Carlos");
        assertThat(encontrado.get().getUser().getUsername()).isEqualTo("carlos_test");
    }

    @Test
    void eventoPuedeTenerVariosArtistasSinDuplicarAsociacion() {
        Venue venue = venueRepository.save(
                new Venue("VEN-TEST-02", "Test Stadium", "Medellin", "Calle 2", 2000)
        );

        Artist artista1 = artistRepository.save(new Artist("Test Artist A", "Colombia", "Rock"));
        Artist artista2 = artistRepository.save(new Artist("Test Artist B", "Mexico", "Pop"));

        Event evento = new Event(
                "EVT-TEST-03", "Festival Test", "Descripcion", EventCategory.MUSIC,
                EventStatus.PUBLISHED, LocalDateTime.now().plusDays(30), 18, venue
        );
        evento.addArtist(artista1);
        evento.addArtist(artista2);
        eventRepository.save(evento);

        List<Event> eventosDeArtista1 = eventRepository.findByArtistStageName("Test Artist A");

        assertThat(eventosDeArtista1).hasSize(1);
        assertThat(eventosDeArtista1.get(0).getArtists()).hasSize(2);
    }


    @Test
    void ticketSeAsociaAUsuarioYEvento() {
        Venue venue = venueRepository.save(
                new Venue("VEN-TEST-03", "Test Hall", "Cali", "Calle 3", 500)
        );
        Event evento = eventRepository.save(new Event(
                "EVT-TEST-04", "Evento Ticket", "Descripcion", EventCategory.MUSIC,
                EventStatus.PUBLISHED, LocalDateTime.now().plusDays(5), 18, venue
        ));
        User user = userRepository.save(new User("ana_test", "ana.test@example.com"));

        Ticket ticket = ticketRepository.save(new Ticket(
                "TCK-TEST-01", TicketType.VIP, new BigDecimal("250000.00"),
                TicketStatus.PAID, LocalDateTime.now(), user, evento
        ));

        Ticket encontrado = ticketRepository.findById(ticket.getId()).orElseThrow();

        assertThat(encontrado.getUser().getUsername()).isEqualTo("ana_test");
        assertThat(encontrado.getEvent().getEventCode()).isEqualTo("EVT-TEST-04");
    }

    @Test
    void queryMethodsSimplesYConNavegacion() {
        userRepository.save(new User("maria_test", "Maria.Test@Example.com"));

        Optional<User> porUsername = userRepository.findByUsername("maria_test");
        assertThat(porUsername).isPresent();

        Optional<User> porEmail = userRepository.findByEmailIgnoreCase("maria.test@example.com");
        assertThat(porEmail).isPresent();
        assertThat(porEmail.get().getUsername()).isEqualTo("maria_test");
    }

    @Test
    void jpqlConJoinYCount() {
        Venue venue = venueRepository.save(
                new Venue("VEN-TEST-04", "Test Center", "Barranquilla", "Calle 4", 800)
        );
        Event evento = eventRepository.save(new Event(
                "EVT-TEST-05", "Evento Conteo", "Descripcion", EventCategory.MUSIC,
                EventStatus.PUBLISHED, LocalDateTime.now().plusDays(15), 18, venue
        ));
        User user1 = userRepository.save(new User("user_paid_1", "paid1@example.com"));
        User user2 = userRepository.save(new User("user_paid_2", "paid2@example.com"));
        User user3 = userRepository.save(new User("user_reserved", "reserved@example.com"));

        ticketRepository.save(new Ticket("TCK-TEST-02", TicketType.GENERAL, new BigDecimal("100000.00"),
                TicketStatus.PAID, LocalDateTime.now(), user1, evento));
        ticketRepository.save(new Ticket("TCK-TEST-03", TicketType.GENERAL, new BigDecimal("100000.00"),
                TicketStatus.PAID, LocalDateTime.now(), user2, evento));
        ticketRepository.save(new Ticket("TCK-TEST-04", TicketType.GENERAL, new BigDecimal("100000.00"),
                TicketStatus.RESERVED, LocalDateTime.now(), user3, evento));

        long totalPagados = ticketRepository.countPaidTicketsByEventCode("EVT-TEST-05");

        assertThat(totalPagados).isEqualTo(2);
    }

    @Test
    void codigoDeVenueDuplicadoEsRechazadoPorPostgres() {
        venueRepository.saveAndFlush(
                new Venue("VEN-DUP-01", "Primer Venue", "Bogota", "Calle 5", 300)
        );

        assertThatThrownBy(() ->
                venueRepository.saveAndFlush(
                        new Venue("VEN-DUP-01", "Segundo Venue", "Medellin", "Calle 6", 400)
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

}