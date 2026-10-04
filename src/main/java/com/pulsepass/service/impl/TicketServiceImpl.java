package com.pulsepass.service.impl;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.TicketStatus;
import com.pulsepass.domain.TicketType;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserProfile;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.TicketMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserProfileRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.TicketPricingStrategy;
import com.pulsepass.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final EventRepository eventRepository;
    private final TicketPricingStrategy ticketPricingStrategy;
    private final TicketMapper ticketMapper;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserRepository userRepository,
                             UserProfileRepository userProfileRepository,
                             EventRepository eventRepository,
                             TicketPricingStrategy ticketPricingStrategy,
                             TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.eventRepository = eventRepository;
        this.ticketPricingStrategy = ticketPricingStrategy;
        this.ticketMapper = ticketMapper;
    }

    @Override
    @Transactional
    public TicketResponse purchase(PurchaseTicketRequest request) {
        // BR-TICKET-001: el usuario debe existir
        User user = userRepository.findByEmailIgnoreCase(request.userEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userEmail()));

        // BR-TICKET-002: el usuario debe estar activo
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessRuleException("User is not active: " + user.getEmail());
        }

        // BR-TICKET-003: el evento debe existir
        Event event = eventRepository.findByEventCode(request.eventCode())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + request.eventCode()));

        // BR-TICKET-004: solo se compra para eventos PUBLISHED
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException(
                    "Tickets can only be purchased for PUBLISHED events. Current status: " + event.getStatus());
        }

        // BR-TICKET-005: el evento no puede haber ocurrido ya
        if (!event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Event has already occurred: " + event.getEventCode());
        }

        // BR-TICKET-006: edad minima evaluada en la fecha del evento
        requireMinimumAge(user, event);

        // BR-TICKET-007: debe quedar capacidad (paidTickets < venue.capacity)
        long paidTickets = ticketRepository.countPaidTicketsByEventCode(event.getEventCode());
        int capacity = event.getVenue().getCapacity();

        if (paidTickets >= capacity) {
            throw new BusinessRuleException("Event has no available capacity: " + event.getEventCode());
        }

        // BR-TICKET-009: el precio lo calcula el sistema y nunca es negativo
        BigDecimal price = calculatePrice(request.type());

        Ticket ticket = ticketRepository.save(new Ticket(
                generateTicketCode(),
                request.type(),
                price,
                TicketStatus.PAID,
                LocalDateTime.now(),
                user,
                event
        ));

        // BR-TICKET-008: si esta compra completa la capacidad, el evento pasa a SOLD_OUT
        if (paidTickets + 1 >= capacity) {
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);
        }

        return ticketMapper.toResponse(ticket);
    }

    @Override
    public TicketResponse findByCode(String ticketCode) {
        return ticketMapper.toResponse(findTicket(ticketCode));
    }

    @Override
    public List<TicketResponse> findByUserEmail(String email) {
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceNotFoundException("User not found: " + email);
        }

        return ticketRepository.findByUser_EmailIgnoreCaseOrderByPurchaseDateDesc(email)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    public List<TicketResponse> findPaidTicketsByEvent(String eventCode) {
        if (!eventRepository.existsByEventCode(eventCode)) {
            throw new ResourceNotFoundException("Event not found: " + eventCode);
        }

        return ticketRepository.findByEvent_EventCodeAndStatus(eventCode, TicketStatus.PAID)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TicketResponse cancel(String ticketCode) {
        Ticket ticket = findTicket(ticketCode);

        // BR-TICKET-010 y BR-TICKET-011: solo se cancela un ticket PAID
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException(
                    "Only PAID tickets can be cancelled. Current status: " + ticket.getStatus());
        }

        // BR-TICKET-012: no se cancela despues de la fecha del evento
        if (!LocalDateTime.now().isBefore(ticket.getEvent().getEventDate())) {
            throw new BusinessRuleException("Ticket cannot be cancelled after the event date: " + ticketCode);
        }

        ticket.setStatus(TicketStatus.CANCELLED);

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponse markAsUsed(String ticketCode) {
        Ticket ticket = findTicket(ticketCode);

        // BR-TICKET-013 y BR-TICKET-014: solo se usa un ticket PAID
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException(
                    "Only PAID tickets can be marked as used. Current status: " + ticket.getStatus());
        }

        ticket.setStatus(TicketStatus.USED);

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    private Ticket findTicket(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));
    }

    private void requireMinimumAge(User user, Event event) {
        Integer minimumAge = event.getMinimumAge();

        if (minimumAge == null || minimumAge <= 0) {
            return;
        }

        LocalDate birthDate = userProfileRepository.findByUser_Id(user.getId())
                .map(UserProfile::getBirthDate)
                .orElseThrow(() -> new BusinessRuleException(
                        "User birth date is required to validate minimum age."));

        int ageAtEvent = Period.between(birthDate, event.getEventDate().toLocalDate()).getYears();

        if (ageAtEvent < minimumAge) {
            throw new BusinessRuleException("User does not meet minimum age.");
        }
    }

    private BigDecimal calculatePrice(TicketType type) {
        BigDecimal price = ticketPricingStrategy.calculatePrice(type);

        if (price == null || price.signum() < 0) {
            throw new BusinessRuleException("Ticket price cannot be negative.");
        }

        return price;
    }

    private String generateTicketCode() {
        return "TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
