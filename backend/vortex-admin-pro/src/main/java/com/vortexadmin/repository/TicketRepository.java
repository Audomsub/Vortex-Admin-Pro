package com.vortexadmin.repository;

import com.vortexadmin.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Ticket} entities, providing standard CRUD operations
 * for support-ticket management.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
