package com.pulsepass.repository;

import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUser_EmailIgnoreCase(String email);

    List<Ticket> findByUser_EmailIgnoreCaseAndStatus(String email, TicketStatus status);

    List<Ticket> findByEvent_EventCodeAndStatus(String eventCode, TicketStatus status);

    @Query("""
        SELECT COUNT(t) FROM Ticket t
        WHERE t.event.eventCode = :eventCode
        AND t.status = 'PAID'
        """)
    long countPaidTicketsByEventCode(@Param("eventCode") String eventCode);

    @Query("""
        SELECT t FROM Ticket t
        WHERE t.event.eventDate > :afterDate
        ORDER BY t.event.eventDate ASC
        """)
    List<Ticket> findTicketsOfFutureEvents(@Param("afterDate") LocalDateTime afterDate);

}