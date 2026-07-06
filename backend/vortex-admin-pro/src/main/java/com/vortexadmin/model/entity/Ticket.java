package com.vortexadmin.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a customer support ticket submitted to the help-desk system,
 * tracking its subject, priority, and resolution status.
 */
@Data
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Brief summary of the issue reported by the customer. */
    private String subject;

    /** Display name of the customer who submitted the ticket. */
    private String customerName;

    /** Current resolution state of the ticket: Open, In Progress, or Resolved. */
    private String status; // Open, In Progress, Resolved

    /** Urgency level assigned to the ticket: Low, Medium, or High. */
    private String priority; // Low, Medium, High

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Sets both audit timestamps to the current time before the first database insert.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Refreshes {@code updatedAt} to the current time before every database update.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
