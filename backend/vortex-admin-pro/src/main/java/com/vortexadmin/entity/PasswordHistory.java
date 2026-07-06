package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Records previously used BCrypt password hashes for a {@link User} to enforce
 * a password-reuse policy and prevent cycling through recent credentials.
 */
@Entity
@Table(name = "password_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** BCrypt hash of a password the user has previously used; compared during password change to block reuse. */
    @Column(nullable = false)
    private String passwordHash;

    /** Timestamp when this password hash was originally set on the account. */
    private LocalDateTime changedAt;

    /** The user whose password history this record belongs to. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Records {@code changedAt} as the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        changedAt = LocalDateTime.now();
    }
}
