package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents an RBAC role (e.g., SUPER_ADMIN, ADMIN, MANAGER, USER) that groups
 * a set of fine-grained {@link Permission}s and is assigned to {@link User}s.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique, human-readable role identifier (e.g., "SUPER_ADMIN"). */
    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    private LocalDateTime createdAt;

    /** Fine-grained permissions granted to users holding this role, loaded lazily via the role_permissions join table. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    /**
     * Sets {@code createdAt} to the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
