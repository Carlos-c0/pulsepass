package com.pulsepass;

import com.pulsepass.domain.*;
import com.pulsepass.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

abstract class PersistenceTestSupport {

    protected static final LocalDateTime FECHA_BASE = LocalDateTime.of(2030, 6, 1, 20, 0);
    protected static final LocalDateTime FECHA_COMPRA = FECHA_BASE.minusDays(30);

    @Autowired
    protected VenueRepository venueRepository;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected ArtistRepository artistRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UserProfileRepository userProfileRepository;

    @Autowired
    protected TicketRepository ticketRepository;

    @PersistenceContext
    protected EntityManager em;

    protected void flushAndClear() {
        em.flush();
        em.clear();
    }

    protected Venue crearVenue(String code, String city) {
        return venueRepository.save(new Venue(code, "Venue " + code, city, "Calle 1 # 2-3", 1000));
    }

    protected Event crearEvento(String eventCode, Venue venue, EventStatus status,
                                LocalDateTime fecha, Artist... artistas) {
        Event evento = eventRepository.save(new Event(
                eventCode, "Evento " + eventCode, "Descripcion", EventCategory.MUSIC,
                status, fecha, 18, venue
        ));
        for (Artist artista : artistas) {
            evento.addArtist(artista);
        }
        return evento;
    }

    protected Artist crearArtista(String stageName) {
        return artistRepository.save(new Artist(stageName, "Colombia", "Pop"));
    }

    protected Artist artistaInicial(String stageName) {
        return artistRepository.findByStageName(stageName).orElseThrow();
    }

    protected User crearUsuario(String username) {
        return userRepository.save(new User(username, username + "@example.com"));
    }

    protected Ticket crearTicket(String ticketCode, TicketType type, String price,
                                 TicketStatus status, User user, Event event) {
        return ticketRepository.save(new Ticket(
                ticketCode, type, new BigDecimal(price), status, FECHA_COMPRA, user, event
        ));
    }
}