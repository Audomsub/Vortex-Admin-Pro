package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.model.entity.Ticket;
import com.vortexadmin.model.entity.TicketMessage;
import com.vortexadmin.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles HTTP requests for the support ticket system, enabling users to submit tickets
 * and staff to manage them, delegating all business logic to TicketService.
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * Retrieves all support tickets in the system; accessible to admins and managers.
     *
     * @return a list of {@link Ticket} entities representing all support tickets
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ticket.read', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<List<Ticket>>> getAllTickets() {
        return ResponseEntity.ok(ApiResponse.success("Tickets fetched successfully", ticketService.getAllTickets()));
    }

    /**
     * Creates a new support ticket submitted by the authenticated user.
     *
     * @param ticket the ticket entity containing the subject and initial description
     * @return the created {@link Ticket} entity reflecting the persisted support request
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Ticket>> createTicket(@RequestBody Ticket ticket) {
        return ResponseEntity.ok(ApiResponse.success("Ticket created", ticketService.createTicket(ticket)));
    }

    /**
     * Updates the status of an existing support ticket (e.g., open, in-progress, resolved, closed).
     *
     * @param id   the unique ID of the ticket whose status is being updated
     * @param body a map containing the {@code "status"} key with the new status value
     * @return the updated {@link Ticket} entity reflecting the new status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ticket.update', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<Ticket>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", ticketService.updateTicketStatus(id, body.get("status"))));
    }

    /**
     * Retrieves the message thread for a specific support ticket.
     *
     * @param ticketId the unique ID of the ticket whose messages are being requested
     * @return a list of {@link TicketMessage} entities representing the conversation history
     */
    @GetMapping("/{ticketId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TicketMessage>>> getTicketMessages(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success("Messages fetched", ticketService.getTicketMessages(ticketId)));
    }

    /**
     * Adds a new reply message to an existing support ticket thread.
     *
     * @param ticketId the unique ID of the ticket to reply to
     * @param message  the ticket message entity containing the reply content
     * @return the created {@link TicketMessage} entity reflecting the persisted reply
     */
    @PostMapping("/{ticketId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TicketMessage>> addMessage(@PathVariable Long ticketId, @RequestBody TicketMessage message) {
        return ResponseEntity.ok(ApiResponse.success("Message added", ticketService.addMessage(ticketId, message)));
    }
}
