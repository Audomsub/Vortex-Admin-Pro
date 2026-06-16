package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.model.entity.Ticket;
import com.vortexadmin.model.entity.TicketMessage;
import com.vortexadmin.repository.TicketMessageRepository;
import com.vortexadmin.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Ticket>>> getAllTickets() {
        return ResponseEntity.ok(ApiResponse.success("Tickets fetched successfully", ticketRepository.findAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Ticket>> createTicket(@RequestBody Ticket ticket) {
        Ticket saved = ticketRepository.save(ticket);
        return ResponseEntity.ok(ApiResponse.success("Ticket created", saved));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Ticket>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(body.get("status"));
        ticketRepository.save(ticket);
        return ResponseEntity.ok(ApiResponse.success("Status updated", ticket));
    }

    @GetMapping("/{ticketId}/messages")
    public ResponseEntity<ApiResponse<List<TicketMessage>>> getTicketMessages(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success("Messages fetched", ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)));
    }

    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<ApiResponse<TicketMessage>> addMessage(@PathVariable Long ticketId, @RequestBody TicketMessage message) {
        message.setTicketId(ticketId);
        TicketMessage saved = ticketMessageRepository.save(message);
        return ResponseEntity.ok(ApiResponse.success("Message added", saved));
    }
}
