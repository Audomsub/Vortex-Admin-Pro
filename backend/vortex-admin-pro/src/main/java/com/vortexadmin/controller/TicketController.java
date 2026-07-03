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

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // BUG-013: all ticket endpoints now require authentication and appropriate permissions
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ticket.read', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<List<Ticket>>> getAllTickets() {
        return ResponseEntity.ok(ApiResponse.success("Tickets fetched successfully", ticketService.getAllTickets()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Ticket>> createTicket(@RequestBody Ticket ticket) {
        return ResponseEntity.ok(ApiResponse.success("Ticket created", ticketService.createTicket(ticket)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ticket.update', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<Ticket>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", ticketService.updateTicketStatus(id, body.get("status"))));
    }

    @GetMapping("/{ticketId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TicketMessage>>> getTicketMessages(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success("Messages fetched", ticketService.getTicketMessages(ticketId)));
    }

    @PostMapping("/{ticketId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TicketMessage>> addMessage(@PathVariable Long ticketId, @RequestBody TicketMessage message) {
        return ResponseEntity.ok(ApiResponse.success("Message added", ticketService.addMessage(ticketId, message)));
    }
}
