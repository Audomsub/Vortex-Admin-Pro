package com.vortexadmin.service;

import com.vortexadmin.model.entity.Ticket;
import com.vortexadmin.model.entity.TicketMessage;

import java.util.List;
import java.util.Map;

/**
 * Service contract for support ticket management, including ticket CRUD, status transitions,
 * and the message thread within each ticket.
 */
public interface TicketService {

    /**
     * Returns all support tickets in the system.
     *
     * @return a list of all tickets
     */
    List<Ticket> getAllTickets();

    /**
     * Creates and persists a new support ticket.
     *
     * @param ticket the ticket entity to create, populated with subject and description
     * @return the persisted ticket with its generated primary key and timestamps
     */
    Ticket createTicket(Ticket ticket);

    /**
     * Updates the status of the specified ticket (e.g., from "OPEN" to "IN_PROGRESS" or
     * "CLOSED").
     *
     * @param id     the primary key of the ticket to update
     * @param status the new status value
     * @return the updated ticket entity
     * @throws com.vortexadmin.exception.ApiException if no ticket with the given ID exists
     */
    Ticket updateTicketStatus(Long id, String status);

    /**
     * Returns all messages in the conversation thread for the specified ticket, ordered
     * chronologically from oldest to newest.
     *
     * @param ticketId the primary key of the parent ticket
     * @return a list of messages belonging to the given ticket in ascending creation order
     */
    List<TicketMessage> getTicketMessages(Long ticketId);

    /**
     * Appends a new message to the conversation thread of the specified ticket.
     *
     * @param ticketId the primary key of the ticket to reply to
     * @param message  the message entity to add, populated with sender and content
     * @return the persisted message entity
     * @throws com.vortexadmin.exception.ApiException if no ticket with the given ID exists
     */
    TicketMessage addMessage(Long ticketId, TicketMessage message);
}
