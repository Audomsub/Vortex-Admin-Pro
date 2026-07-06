package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a single-use token issued to a {@link User} to authenticate a
 * password-reset request delivered via email.
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique, unguessable token string embedded in the reset link sent to the user. */
    @Column(nullable = false, unique = true)
    private String token;

    /** Point in time after which the token is no longer valid and must be rejected. */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /** {@code true} once the token has been redeemed; prevents replay of an already-used reset link. */
    private boolean used;

    /** The user who requested the password reset. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Sets {@code createdAt} to the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
