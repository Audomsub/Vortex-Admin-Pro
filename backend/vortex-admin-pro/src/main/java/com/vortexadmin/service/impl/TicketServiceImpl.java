package com.vortexadmin.service.impl;

import com.vortexadmin.exception.ApiException;
import com.vortexadmin.model.entity.Ticket;
import com.vortexadmin.model.entity.TicketMessage;
import com.vortexadmin.repository.TicketMessageRepository;
import com.vortexadmin.repository.TicketRepository;
import com.vortexadmin.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    @Transactional
    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public Ticket updateTicketStatus(Long id, String status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ticket not found"));
        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }

    @Override
    public List<TicketMessage> getTicketMessages(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Ticket not found");
        }
        return ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Override
    @Transactional
    public TicketMessage addMessage(Long ticketId, TicketMessage message) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Ticket not found");
        }
        message.setTicketId(ticketId);
        return ticketMessageRepository.save(message);
    }
}
