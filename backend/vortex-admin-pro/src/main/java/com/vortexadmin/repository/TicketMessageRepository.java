package com.vortexadmin.repository;

import com.vortexadmin.model.entity.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link TicketMessage} entities, providing standard CRUD
 * operations and a method to retrieve the message thread for a specific support ticket.
 */
@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    /**
     * Returns all messages belonging to the specified ticket, ordered chronologically from
     * oldest to newest so that the conversation is displayed in natural reading order.
     *
     * @param ticketId the primary key of the parent support ticket
     * @return a list of messages for the given ticket in ascending creation order
     */
    List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
