package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable audit trail entry recording who performed what action on which
 * entity, capturing the IP address and a change-detail payload for compliance
 * and forensic purposes.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Verb describing the operation performed (e.g., CREATE, UPDATE, DELETE, LOGIN). */
    private String action;

    /** Simple class name of the entity that was acted upon (e.g., "User", "Task"). */
    private String entityType;

    /** Primary key of the affected entity row. */
    private Long entityId;

    /** IP address from which the request originated. */
    private String ipAddress;

    /** JSON or human-readable payload capturing the before/after state of the change. */
    @Column(columnDefinition = "TEXT")
    private String details;

    private LocalDateTime createdAt;

    /** The user who triggered the action; may be {@code null} for system-initiated events. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Sets {@code createdAt} to the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
