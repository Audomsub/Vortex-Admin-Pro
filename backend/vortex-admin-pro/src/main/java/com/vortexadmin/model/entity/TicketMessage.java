package com.vortexadmin.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents a single message in the conversation thread of a support {@link Ticket},
 * distinguishing between customer-authored messages and staff responses.
 */
@Data
@Entity
@Table(name = "ticket_messages")
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Denormalized reference to the parent ticket; stored as a plain ID rather than a JPA relation. */
    private Long ticketId;

    /** Display name of the person who wrote this message. */
    private String senderName;

    @Column(columnDefinition = "TEXT")
    private String message;

    /** {@code true} if this message was authored by a support staff member; {@code false} for customer messages. */
    private boolean isStaff;

    private LocalDateTime createdAt;

    /**
     * Records {@code createdAt} as the current time before the first database insert.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
