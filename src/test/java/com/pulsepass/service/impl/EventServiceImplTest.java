package com.pulsepass.service.impl;

import com.pulsepass.domain.Artist;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventCategory;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Venue;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.EventMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.VenueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    private static final String EVENT_CODE = "CMF-2026";
    private static final String VENUE_CODE = "VEN-SMR-01";
    private static final LocalDateTime FECHA_FUTURA = LocalDateTime.of(2030, 6, 1, 20, 0);
    private static final LocalDateTime FECHA_PASADA = LocalDateTime.of(2020, 6, 1, 20, 0);

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    // ---------- findByCode ----------

    @Test
    @DisplayName("TEST-EVENT-001: evento existente retorna DTO")
    void eventoExistenteRetornaDto() {
        // ARRANGE
        Event evento = evento(EventStatus.PUBLISHED, FECHA_FUTURA, venueActivo());
        EventResponse esperado = respuestaDe(evento);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(evento));
        when(eventMapper.toResponse(evento)).thenReturn(esperado);

        // ACT
        EventResponse resultado = eventService.findByCode(EVENT_CODE);

        // ASSERT
        assertThat(resultado).isEqualTo(esperado);
        assertThat(resultado.venueCode()).isEqualTo(VENUE_CODE);
    }

    @Test
    @DisplayName("TEST-EVENT-002: evento inexistente lanza ResourceNotFoundException")
    void eventoInexistenteLanzaResourceNotFound() {
        // ARRANGE
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.findByCode(EVENT_CODE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Event not found: CMF-2026");

        verify(eventMapper, never()).toResponse(any());
    }

    // ---------- create ----------

    @Test
    @DisplayName("TEST-EVENT-003: crear evento valido ejecuta save() y lo deja en DRAFT")
    void crearEventoValidoEjecutaSave() {
        // ARRANGE
        Venue venue = venueActivo();
        CreateEventRequest request = solicitud(FECHA_FUTURA, 18);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventMapper.toResponse(any(Event.class)))
                .thenAnswer(invocation -> respuestaDe(invocation.getArgument(0)));

        // ACT
        EventResponse resultado = eventService.create(request);

        // ASSERT
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        Event guardado = captor.getValue();

        assertThat(guardado.getEventCode()).isEqualTo(EVENT_CODE);
        assertThat(guardado.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(guardado.getVenue()).isSameAs(venue);
        assertThat(guardado.getMinimumAge()).isEqualTo(18);
        assertThat(resultado.status()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    @DisplayName("TEST-EVENT-004: venue inexistente lanza error y save() nunca se ejecuta")
    void crearEventoConVenueInexistenteNoGuarda() {
        // ARRANGE
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.create(solicitud(FECHA_FUTURA, 18)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Venue not found: VEN-SMR-01");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-EVENT-005: venue inactivo lanza BusinessRuleException")
    void crearEventoEnVenueInactivoLanzaBusinessRule() {
        // ARRANGE
        Venue venue = venueActivo();
        venue.setActive(false);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.of(venue));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.create(solicitud(FECHA_FUTURA, 18)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Venue is not active");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-EVENT-006: fecha pasada lanza BusinessRuleException")
    void crearEventoConFechaPasadaLanzaBusinessRule() {
        // ARRANGE
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.of(venueActivo()));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.create(solicitud(FECHA_PASADA, 18)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Event date must be in the future.");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-EVENT-001: codigo de evento duplicado lanza DuplicateResourceException")
    void crearEventoConCodigoDuplicadoLanzaDuplicateResource() {
        // ARRANGE
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.create(solicitud(FECHA_FUTURA, 18)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Event code already exists: CMF-2026");

        verify(venueRepository, never()).findByCode(anyString());
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-EVENT-006: edad minima negativa lanza BusinessRuleException")
    void crearEventoConEdadMinimaNegativaLanzaBusinessRule() {
        // ARRANGE
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.of(venueActivo()));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.create(solicitud(FECHA_FUTURA, -1)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Minimum age cannot be negative.");

        verify(eventRepository, never()).save(any());
    }

    // ---------- publish ----------

    @Test
    @DisplayName("TEST-EVENT-007: publicar DRAFT valido lo deja en PUBLISHED")
    void publicarEventoDraftLoDejaPublished() {
        // ARRANGE
        Event evento = evento(EventStatus.DRAFT, FECHA_FUTURA, venueActivo());
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(evento));
        when(eventRepository.save(evento)).thenReturn(evento);
        when(eventMapper.toResponse(evento)).thenAnswer(invocation -> respuestaDe(evento));

        // ACT
        EventResponse resultado = eventService.publish(EVENT_CODE);

        // ASSERT
        assertThat(evento.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(resultado.status()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository).save(evento);
    }

    @Test
    @DisplayName("TEST-EVENT-008: publicar CANCELLED lanza BusinessRuleException y no persiste")
    void publicarEventoCanceladoNoPersiste() {
        // ARRANGE
        Event evento = evento(EventStatus.CANCELLED, FECHA_FUTURA, venueActivo());
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(evento));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.publish(EVENT_CODE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only DRAFT events can be published");

        assertThat(evento.getStatus()).isEqualTo(EventStatus.CANCELLED);
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-EVENT-008: publicar un evento con fecha pasada lanza BusinessRuleException")
    void publicarEventoConFechaPasadaNoPersiste() {
        // ARRANGE
        Event evento = evento(EventStatus.DRAFT, FECHA_PASADA, venueActivo());
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(evento));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.publish(EVENT_CODE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Event date must be in the future.");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-EVENT-009: publicar un evento con venue inactivo lanza BusinessRuleException")
    void publicarEventoConVenueInactivoNoPersiste() {
        // ARRANGE
        Venue venue = venueActivo();
        venue.setActive(false);
        Event evento = evento(EventStatus.DRAFT, FECHA_FUTURA, venue);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(evento));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.publish(EVENT_CODE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Venue is not active");

        assertThat(evento.getStatus()).isEqualTo(EventStatus.DRAFT);
        verify(eventRepository, never()).save(any());
    }

    // ---------- addArtist ----------

    @Test
    @DisplayName("FR-SVC-007: asociar un artista valido lo agrega al evento y guarda")
    void asociarArtistaValidoLoAgregaAlEvento() {
        // ARRANGE
        Event evento = evento(EventStatus.PUBLISHED, FECHA_FUTURA, venueActivo());
        Artist artista = artista(1L, "Solar Beat");
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(evento));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artista));
        when(eventRepository.save(evento)).thenReturn(evento);
        when(eventMapper.toResponse(evento)).thenReturn(respuestaDe(evento));

        // ACT
        eventService.addArtist(EVENT_CODE, 1L);

        // ASSERT
        assertThat(evento.getArtists()).containsOnly(artista);
        verify(eventRepository).save(evento);
    }

    @Test
    @DisplayName("BR-EVENT-010: asociar dos veces el mismo artista lanza DuplicateResourceException")
    void asociarArtistaRepetidoLanzaDuplicateResource() {
        // ARRANGE
        Event evento = evento(EventStatus.PUBLISHED, FECHA_FUTURA, venueActivo());
        Artist artista = artista(1L, "Solar Beat");
        evento.addArtist(artista);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(evento));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artista));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.addArtist(EVENT_CODE, 1L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already associated");

        assertThat(evento.getArtists()).hasSize(1);
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-EVENT-011: no se agregan artistas a un evento CANCELLED")
    void asociarArtistaAEventoCanceladoLanzaBusinessRule() {
        // ARRANGE
        Event evento = evento(EventStatus.CANCELLED, FECHA_FUTURA, venueActivo());
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(evento));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artista(1L, "Solar Beat")));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.addArtist(EVENT_CODE, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CANCELLED");

        assertThat(evento.getArtists()).isEmpty();
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("FR-SVC-007: asociar un artista inexistente lanza ResourceNotFoundException")
    void asociarArtistaInexistenteLanzaResourceNotFound() {
        // ARRANGE
        Event evento = evento(EventStatus.PUBLISHED, FECHA_FUTURA, venueActivo());
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(evento));
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.addArtist(EVENT_CODE, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Artist not found: 99");

        verify(eventRepository, never()).save(any());
    }

    // ---------- consultas de lista ----------

    @Test
    @DisplayName("FR-SVC-005: findPublishedEvents consulta solo PUBLISHED y retorna resumenes")
    void eventosPublicadosSeRetornanComoResumen() {
        // ARRANGE
        Event evento = evento(EventStatus.PUBLISHED, FECHA_FUTURA, venueActivo());
        when(eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED)).thenReturn(List.of(evento));
        when(eventMapper.toSummary(evento)).thenReturn(resumenDe(evento));

        // ACT
        List<EventSummaryResponse> resultado = eventService.findPublishedEvents();

        // ASSERT
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).eventCode()).isEqualTo(EVENT_CODE);
        verify(eventRepository).findByStatusOrderByEventDateAsc(eq(EventStatus.PUBLISHED));
    }

    @Test
    @DisplayName("FR-SVC-008: findByArtist retorna los eventos del artista como resumenes")
    void eventosPorArtistaSeRetornanComoResumen() {
        // ARRANGE
        Event evento = evento(EventStatus.PUBLISHED, FECHA_FUTURA, venueActivo());
        when(artistRepository.findByStageNameIgnoreCase("solar beat"))
                .thenReturn(Optional.of(artista(1L, "Solar Beat")));
        when(eventRepository.findByArtistStageName("Solar Beat")).thenReturn(List.of(evento));
        when(eventMapper.toSummary(evento)).thenReturn(resumenDe(evento));

        // ACT
        List<EventSummaryResponse> resultado = eventService.findByArtist("solar beat");

        // ASSERT
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).venueName()).isEqualTo("Marina Convention Center");
    }

    // ---------- datos de apoyo ----------

    private Venue venueActivo() {
        return new Venue(VENUE_CODE, "Marina Convention Center", "Santa Marta", "Calle 1 # 2-3", 3);
    }

    private Event evento(EventStatus status, LocalDateTime fecha, Venue venue) {
        return new Event(
                EVENT_CODE, "Caribbean Music Fest 2026", "Festival de musica del Caribe",
                EventCategory.MUSIC, status, fecha, 18, venue
        );
    }

    private Artist artista(Long id, String stageName) {
        Artist artista = new Artist(stageName, "Colombia", "Electronic");
        ReflectionTestUtils.setField(artista, "id", id);
        return artista;
    }

    private CreateEventRequest solicitud(LocalDateTime fecha, Integer edadMinima) {
        return new CreateEventRequest(
                EVENT_CODE, "Caribbean Music Fest 2026", "Festival de musica del Caribe",
                EventCategory.MUSIC, fecha, edadMinima, VENUE_CODE
        );
    }

    private EventResponse respuestaDe(Event evento) {
        return new EventResponse(
                evento.getId(), evento.getEventCode(), evento.getName(), evento.getDescription(),
                evento.getCategory(), evento.getStatus(), evento.getEventDate(), evento.getMinimumAge(),
                evento.getVenue().getCode(), evento.getVenue().getName(), List.of()
        );
    }

    private EventSummaryResponse resumenDe(Event evento) {
        return new EventSummaryResponse(
                evento.getId(), evento.getEventCode(), evento.getName(), evento.getCategory(),
                evento.getStatus(), evento.getEventDate(),
                evento.getVenue().getName(), evento.getVenue().getCity()
        );
    }
}
