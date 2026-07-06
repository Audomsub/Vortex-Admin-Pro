package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores the TOTP-based two-factor authentication configuration for a {@link User},
 * including the shared secret, activation state, and one-time backup codes.
 */
@Entity
@Table(name = "user_2fa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTwoFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user for whom 2FA is configured; each user has at most one 2FA record. */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Base32-encoded TOTP shared secret registered in the authenticator app — must be kept confidential. */
    @Column(name = "secret_key", nullable = false)
    private String secretKey;

    /** {@code true} once the user has verified and fully activated 2FA; defaults to {@code false}. */
    @Column(nullable = false)
    private Boolean enabled;

    /** Comma-separated BCrypt hashes of unused one-time backup codes; consumed codes must be removed. */
    // Comma-separated BCrypt hashes of unused backup codes
    @Column(name = "backup_codes", columnDefinition = "TEXT")
    private String backupCodes;

    /** Timestamp of the last successful TOTP code verification, used to prevent code replay within the same time window. */
    @Column(name = "last_used_totp_at")
    private LocalDateTime lastUsedTotpAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Initializes audit timestamps and defaults {@code enabled} to {@code false}
     * before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (enabled == null) enabled = false;
    }

    /**
     * Refreshes {@code updatedAt} to the current time before every database update.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
