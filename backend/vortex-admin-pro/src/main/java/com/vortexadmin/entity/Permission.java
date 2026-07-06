package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a fine-grained access control permission that can be grouped into
 * {@link Role}s to form the RBAC policy (e.g., "user.create", "dashboard.view").
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique machine-readable permission key used in authorization checks (e.g., "user.delete"). */
    @Column(unique = true)
    private String code;

    /** Human-readable display label shown in the admin UI. */
    private String name;

    private LocalDateTime createdAt;

    /**
     * Sets {@code createdAt} to the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
