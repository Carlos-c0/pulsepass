package com.pulsepass.service.impl;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventCategory;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.TicketStatus;
import com.pulsepass.domain.TicketType;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserProfile;
import com.pulsepass.domain.Venue;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class TicketServiceImplTest {

    private static final String EVENT_CODE = "CMF-2026";
    private static final String TICKET_CODE = "TCK-0001";
    private static final LocalDateTime FECHA_EVENTO = LocalDateTime.of(2030, 6, 1, 20, 0);
    private static final LocalDateTime FECHA_PASADA = LocalDateTime.of(2020, 6, 1, 20, 0);
    private static final BigDecimal PRECIO_VIP = new BigDecimal("200000.00");

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketPricingStrategy ticketPricingStrategy;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    // ---------- purchase ----------

    @Test
    @DisplayName("TEST-TICKET-001: compra valida genera un ticket PAID")
    void compraValidaGeneraTicketPaid() {
        // ARRANGE: Andrea tiene 25 el dia del evento y quedan cupos (0 de 3)
        User andrea = usuario(1L, "andrea");
        Event evento = evento(EventStatus.PUBLISHED, FECHA_EVENTO);
        existeUsuario(andrea);
        existeEvento(evento);
        existePerfil(andrea, LocalDate.of(2005, 3, 10));
        when(ticketRepository.countPaidTicketsByEventCode(EVENT_CODE)).thenReturn(0L);
        when(ticketPricingStrategy.calculatePrice(TicketType.VIP)).thenReturn(PRECIO_VIP);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class)))
                .thenAnswer(invocation -> respuestaDe(invocation.getArgument(0)));

        // ACT
        TicketResponse resultado = ticketService.purchase(compra("andrea@email.com", TicketType.VIP));

        // ASSERT
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        Ticket guardado = captor.getValue();

        assertThat(guardado.getStatus()).isEqualTo(TicketStatus.PAID);
        assertThat(guardado.getType()).isEqualTo(TicketType.VIP);
        assertThat(guardado.getPrice()).isEqualByComparingTo("200000.00");
        assertThat(guardado.getTicketCode()).startsWith("TCK-");
        assertThat(guardado.getPurchaseDate()).isNotNull();
        assertThat(guardado.getUser()).isSameAs(andrea);
        assertThat(guardado.getEvent()).isSameAs(evento);

        assertThat(resultado.status()).isEqualTo(TicketStatus.PAID);
        assertThat(resultado.userEmail()).isEqualTo("andrea@email.com");
        assertThat(resultado.eventCode()).isEqualTo(EVENT_CODE);

        assertThat(evento.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-002: usuario inexistente lanza ResourceNotFoundException")
    void usuarioInexistenteLanzaResourceNotFound() {
        // ARRANGE
        when(userRepository.findByEmailIgnoreCase("nadie@email.com")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> ticketService.purchase(compra("nadie@email.com", TicketType.GENERAL)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: nadie@email.com");

        verify(eventRepository, never()).findByEventCode(anyString());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-003: usuario inactivo lanza BusinessRuleException")
    void usuarioInactivoLanzaBusinessRule() {
        // ARRANGE: Miguel existe pero esta inactivo
        User miguel = usuario(4L, "miguel");
        miguel.setActive(false);
        existeUsuario(miguel);

        // ACT + ASSERT
        assertThatThrownBy(() -> ticketService.purchase(compra("miguel@email.com", TicketType.GENERAL)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("User is not active: miguel@email.com");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-004: evento DRAFT lanza BusinessRuleException")
    void eventoDraftLanzaBusinessRule() {
        // ARRANGE
        existeUsuario(usuario(1L, "andrea"));
        existeEvento(evento(EventStatus.DRAFT, FECHA_EVENTO));

        // ACT + ASSERT
        assertThatThrownBy(() -> ticketService.purchase(compra("andrea@email.com", TicketType.GENERAL)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Current status: DRAFT");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-005: evento CANCELLED lanza BusinessRuleException")
    void eventoCanceladoLanzaBusinessRule() {
        // ARRANGE
        existeUsuario(usuario(1L, "andrea"));
        existeEvento(evento(EventStatus.CANCELLED, FECHA_EVENTO));

        // ACT + ASSERT
        assertThatThrownBy(() -> ticketService.purchase(compra("andrea@email.com", TicketType.GENERAL)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Current status: CANCELLED");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-TICKET-005: evento que ya ocurrio lanza BusinessRuleException")
    void eventoQueYaOcurrioLanzaBusinessRule() {
        // ARRANGE
        existeUsuario(usuario(1L, "andrea"));
        existeEvento(evento(EventStatus.PUBLISHED, FECHA_PASADA));

        // ACT + ASSERT
        assertThatThrownBy(() -> ticketService.purchase(compra("andrea@email.com", TicketType.GENERAL)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Event has already occurred: CMF-2026");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-006: usuario menor de edad lanza BusinessRuleException")
    void usuarioMenorDeEdadLanzaBusinessRule() {
        // ARRANGE: Laura tiene 17 el dia del evento y el evento exige 18
        User laura = usuario(3L, "laura");
        existeUsuario(laura);
        existeEvento(evento(EventStatus.PUBLISHED, FECHA_EVENTO));
        existePerfil(laura, LocalDate.of(2012, 12, 31));

        // ACT + ASSERT
        assertThatThrownBy(() -> ticketService.purchase(compra("laura@email.com", TicketType.GENERAL)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("User does not meet minimum age.");

        verify(ticketRepository, never()).countPaidTicketsByEventCode(anyString());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-007: evento sin capacidad lanza BusinessRuleException")
    void eventoSinCapacidadLanzaBusinessRule() {
        // ARRANGE: el venue tiene capacidad 3 y ya hay 3 tickets PAID
        User andrea = usuario(1L, "andrea");
        existeUsuario(andrea);
        existeEvento(evento(EventStatus.PUBLISHED, FECHA_EVENTO));
        existePerfil(andrea, LocalDate.of(2005, 3, 10));
        when(ticketRepository.countPaidTicketsByEventCode(EVENT_CODE)).thenReturn(3L);

        // ACT + ASSERT
        assertThatThrownBy(() -> ticketService.purchase(compra("andrea@email.com", TicketType.GENERAL)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Event has no available capacity: CMF-2026");

        verify(ticketPricingStrategy, never()).calculatePrice(any());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-008: el ultimo ticket disponible se guarda y el evento pasa a SOLD_OUT")
    void ultimoTicketDisponibleDejaElEventoSoldOut() {
        // ARRANGE: el venue tiene capacidad 3 y ya hay 2 tickets PAID
        User andrea = usuario(1L, "andrea");
        Event evento = evento(EventStatus.PUBLISHED, FECHA_EVENTO);
        existeUsuario(andrea);
        existeEvento(evento);
        existePerfil(andrea, LocalDate.of(2005, 3, 10));
        when(ticketRepository.countPaidTicketsByEventCode(EVENT_CODE)).thenReturn(2L);
        when(ticketPricingStrategy.calculatePrice(TicketType.GENERAL)).thenReturn(new BigDecimal("100000.00"));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class)))
                .thenAnswer(invocation -> respuestaDe(invocation.getArgument(0)));

        // ACT
        TicketResponse resultado = ticketService.purchase(compra("andrea@email.com", TicketType.GENERAL));

        // ASSERT
        assertThat(resultado.status()).isEqualTo(TicketStatus.PAID);
        assertThat(evento.getStatus()).isEqualTo(EventStatus.SOLD_OUT);
        verify(ticketRepository).save(any(Ticket.class));
        verify(eventRepository).save(evento);
    }

    // ---------- cancel ----------

    @Test
    @DisplayName("TEST-TICKET-009: cancelar un ticket PAID lo deja CANCELLED")
    void cancelarTicketPaidLoDejaCancelled() {
        // ARRANGE
        Ticket ticket = ticket(TicketStatus.PAID, FECHA_EVENTO);
        when(ticketRepository.findByTicketCode(TICKET_CODE)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenAnswer(invocation -> respuestaDe(ticket));

        // ACT
        TicketResponse resultado = ticketService.cancel(TICKET_CODE);

        // ASSERT
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(resultado.status()).isEqualTo(TicketStatus.CANCELLED);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("TEST-TICKET-010: cancelar un ticket USED lanza BusinessRuleException")
    void cancelarTicketUsedLanzaBusinessRule() {
        // ARRANGE
        Ticket ticket = ticket(TicketStatus.USED, FECHA_EVENTO);
        when(ticketRepository.findByTicketCode(TICKET_CODE)).thenReturn(Optional.of(ticket));

        // ACT + ASSERT
        assertThatThrownBy(() -> ticketService.cancel(TICKET_CODE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only PAID tickets can be cancelled. Current status: USED");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-TICKET-012: no se cancela un ticket despues de la fecha del evento")
    void cancelarDespuesDeLaFechaDelEventoLanzaBusinessRule() {
        // ARRANGE
        Ticket ticket = ticket(TicketStatus.PAID, FECHA_PASADA);
        when(ticketRepository.findByTicketCode(TICKET_CODE)).thenReturn(Optional.of(ticket));

        // ACT + ASSERT
        assertThatThrownBy(() -> ticketService.cancel(TICKET_CODE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot be cancelled after the event date");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PAID);
        verify(ticketRepository, never()).save(any());
    }

    // ---------- markAsUsed ----------

    @Test
    @DisplayName("TEST-TICKET-011: marcar un ticket PAID como usado lo deja USED")
    void marcarTicketPaidComoUsadoLoDejaUsed() {
        // ARRANGE
        Ticket ticket = ticket(TicketStatus.PAID, FECHA_EVENTO);
        when(ticketRepository.findByTicketCode(TICKET_CODE)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenAnswer(invocation -> respuestaDe(ticket));

        // ACT
        TicketResponse resultado = ticketService.markAsUsed(TICKET_CODE);

        // ASSERT
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
        assertThat(resultado.status()).isEqualTo(TicketStatus.USED);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("TEST-TICKET-012: usar un ticket CANCELLED lanza BusinessRuleException")
    void usarTicketCancelledLanzaBusinessRule() {
        // ARRANGE
        Ticket ticket = ticket(TicketStatus.CANCELLED, FECHA_EVENTO);
        when(ticketRepository.findByTicketCode(TICKET_CODE)).thenReturn(Optional.of(ticket));

        // ACT + ASSERT
        assertThatThrownBy(() -> ticketService.markAsUsed(TICKET_CODE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only PAID tickets can be marked as used. Current status: CANCELLED");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        verify(ticketRepository, never()).save(any());
    }

    // ---------- consultas ----------

    @Test
    @DisplayName("FR-SVC-014: ticket inexistente lanza ResourceNotFoundException")
    void ticketInexistenteLanzaResourceNotFound() {
        // ARRANGE
        when(ticketRepository.findByTicketCode("TCK-XXXX")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> ticketService.findByCode("TCK-XXXX"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ticket not found: TCK-XXXX");
    }

    @Test
    @DisplayName("FR-SVC-015: tickets de un usuario se retornan como DTO")
    void ticketsDeUsuarioSeRetornanComoDto() {
        // ARRANGE
        Ticket ticket = ticket(TicketStatus.PAID, FECHA_EVENTO);
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(true);
        when(ticketRepository.findByUser_EmailIgnoreCaseOrderByPurchaseDateDesc("andrea@email.com"))
                .thenReturn(List.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(respuestaDe(ticket));

        // ACT
        List<TicketResponse> resultado = ticketService.findByUserEmail("andrea@email.com");

        // ASSERT
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).ticketCode()).isEqualTo(TICKET_CODE);
    }

    @Test
    @DisplayName("FR-SVC-016: tickets pagados de un evento se consultan con estado PAID")
    void ticketsPagadosDeEventoSeConsultanConEstadoPaid() {
        // ARRANGE
        Ticket ticket = ticket(TicketStatus.PAID, FECHA_EVENTO);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(true);
        when(ticketRepository.findByEvent_EventCodeAndStatus(EVENT_CODE, TicketStatus.PAID))
                .thenReturn(List.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(respuestaDe(ticket));

        // ACT
        List<TicketResponse> resultado = ticketService.findPaidTicketsByEvent(EVENT_CODE);

        // ASSERT
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).eventName()).isEqualTo("Caribbean Music Fest 2026");
        verify(ticketRepository).findByEvent_EventCodeAndStatus(eq(EVENT_CODE), eq(TicketStatus.PAID));
    }

    // ---------- stubs reutilizables ----------

    private void existeUsuario(User usuario) {
        when(userRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
    }

    private void existeEvento(Event evento) {
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(evento));
    }

    private void existePerfil(User usuario, LocalDate fechaNacimiento) {
        UserProfile perfil = new UserProfile(
                usuario, "Nombre", "Apellido", "3001234567", "Santa Marta", fechaNacimiento
        );
        when(userProfileRepository.findByUser_Id(usuario.getId())).thenReturn(Optional.of(perfil));
    }

    // ---------- datos de apoyo ----------

    private User usuario(Long id, String username) {
        User usuario = new User(username, username + "@email.com");
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private Event evento(EventStatus status, LocalDateTime fecha) {
        Venue venue = new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Calle 1 # 2-3", 3);
        return new Event(
                EVENT_CODE, "Caribbean Music Fest 2026", "Festival de musica del Caribe",
                EventCategory.MUSIC, status, fecha, 18, venue
        );
    }

    private Ticket ticket(TicketStatus status, LocalDateTime fechaEvento) {
        return new Ticket(
                TICKET_CODE, TicketType.VIP, PRECIO_VIP, status, fechaEvento.minusDays(30),
                usuario(1L, "andrea"), evento(EventStatus.PUBLISHED, fechaEvento)
        );
    }

    private PurchaseTicketRequest compra(String email, TicketType type) {
        return new PurchaseTicketRequest(email, EVENT_CODE, type);
    }

    private TicketResponse respuestaDe(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(), ticket.getTicketCode(), ticket.getType(), ticket.getPrice(),
                ticket.getStatus(), ticket.getPurchaseDate(), ticket.getUser().getEmail(),
                ticket.getEvent().getEventCode(), ticket.getEvent().getName()
        );
    }
}
