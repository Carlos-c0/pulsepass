package com.pulsepass.service.impl;

import com.pulsepass.domain.Artist;
import com.pulsepass.domain.Event;
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
import com.pulsepass.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final ArtistRepository artistRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository,
                            VenueRepository venueRepository,
                            ArtistRepository artistRepository,
                            EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.artistRepository = artistRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional
    public EventResponse create(CreateEventRequest request) {
        // BR-EVENT-001: el codigo del evento es unico
        if (eventRepository.existsByEventCode(request.eventCode())) {
            throw new DuplicateResourceException("Event code already exists: " + request.eventCode());
        }

        // BR-EVENT-002: el venue debe existir
        Venue venue = venueRepository.findByCode(request.venueCode())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + request.venueCode()));

        // BR-EVENT-003: el venue debe estar activo
        requireActiveVenue(venue);

        // BR-EVENT-004: la fecha debe ser futura
        requireFutureDate(request.eventDate());

        // BR-EVENT-006: minimumAge >= 0 (0 = sin restriccion de edad)
        int minimumAge = resolveMinimumAge(request.minimumAge());

        // BR-EVENT-005: el estado inicial lo decide el servicio, no el request
        Event event = new Event(
                request.eventCode(),
                request.name(),
                request.description(),
                request.category(),
                EventStatus.DRAFT,
                request.eventDate(),
                minimumAge,
                venue
        );

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse findByCode(String eventCode) {
        return eventMapper.toResponse(findEvent(eventCode));
    }

    @Override
    public List<EventSummaryResponse> findPublishedEvents() {
        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED)
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }

    @Override
    @Transactional
    public EventResponse publish(String eventCode) {
        Event event = findEvent(eventCode);

        // BR-EVENT-007: solo se publica un evento en DRAFT
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Only DRAFT events can be published. Current status: " + event.getStatus());
        }

        // BR-EVENT-008: la fecha debe seguir siendo futura
        requireFutureDate(event.getEventDate());

        // BR-EVENT-009: el venue debe seguir activo
        requireActiveVenue(event.getVenue());

        event.setStatus(EventStatus.PUBLISHED);

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventResponse addArtist(String eventCode, Long artistId) {
        Event event = findEvent(eventCode);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + artistId));

        // BR-EVENT-011: no se agregan artistas a eventos CANCELLED o FINISHED
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.FINISHED) {
            throw new BusinessRuleException(
                    "Artists cannot be added to a " + event.getStatus() + " event: " + eventCode);
        }

        if (!Boolean.TRUE.equals(artist.getActive())) {
            throw new BusinessRuleException("Artist is not active: " + artist.getStageName());
        }

        // BR-EVENT-010: el mismo artista no se asocia dos veces
        boolean alreadyAssociated = event.getArtists()
                .stream()
                .anyMatch(existing -> Objects.equals(existing.getId(), artistId));

        if (alreadyAssociated) {
            throw new DuplicateResourceException(
                    "Artist " + artist.getStageName() + " is already associated with event " + eventCode);
        }

        event.addArtist(artist);

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public List<EventSummaryResponse> findByArtist(String stageName) {
        Artist artist = artistRepository.findByStageNameIgnoreCase(stageName)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + stageName));

        return eventRepository.findByArtistStageName(artist.getStageName())
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }

    private Event findEvent(String eventCode) {
        return eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventCode));
    }

    private void requireActiveVenue(Venue venue) {
        if (!Boolean.TRUE.equals(venue.getActive())) {
            throw new BusinessRuleException("Venue is not active: " + venue.getCode());
        }
    }

    private void requireFutureDate(LocalDateTime eventDate) {
        if (eventDate == null || !eventDate.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Event date must be in the future.");
        }
    }

    private int resolveMinimumAge(Integer minimumAge) {
        if (minimumAge == null) {
            return 0;
        }
        if (minimumAge < 0) {
            throw new BusinessRuleException("Minimum age cannot be negative.");
        }
        return minimumAge;
    }
}
