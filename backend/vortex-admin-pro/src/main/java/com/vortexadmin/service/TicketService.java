package com.vortexadmin.service;

import com.vortexadmin.model.entity.Ticket;
import com.vortexadmin.model.entity.TicketMessage;

import java.util.List;
import java.util.Map;

public interface TicketService {

    List<Ticket> getAllTickets();

    Ticket createTicket(Ticket ticket);

    Ticket updateTicketStatus(Long id, String status);

    List<TicketMessage> getTicketMessages(Long ticketId);

    TicketMessage addMessage(Long ticketId, TicketMessage message);
}
