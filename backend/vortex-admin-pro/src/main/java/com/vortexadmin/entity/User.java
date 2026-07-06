package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an application user account, holding authentication credentials,
 * profile information, and a single RBAC role assignment.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    /** BCrypt-hashed password — never store or compare plaintext. */
    private String password;

    private String firstName;

    private String lastName;

    /** Publicly accessible URL pointing to the user's profile picture. */
    private String avatarUrl;

    /** Account lifecycle state (e.g., ACTIVE, INACTIVE, SUSPENDED). */
    private String status;

    /** Timestamp of the most recent successful authentication. */
    private LocalDateTime lastLogin;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** Soft-delete timestamp; {@code null} means the record is active. */
    private LocalDateTime deletedAt;

    /** Count of consecutive failed login attempts used to trigger account lockout. */
    private Integer failedLoginAttempts;

    /** Account is locked out until this time after exceeding the failed-attempt threshold; {@code null} means not locked. */
    private LocalDateTime lockoutUntil;

    /** The single RBAC role assigned to this user, governing coarse-grained access. */
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    /**
     * Initializes audit timestamps and resets the failed login counter before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        failedLoginAttempts = 0;
    }

    /**
     * Refreshes {@code updatedAt} to the current time before every database update.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
