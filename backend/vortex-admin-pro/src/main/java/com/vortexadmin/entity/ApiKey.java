package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a programmatic API key that grants machine-to-machine access on
 * behalf of a {@link User}, with optional scopes, rate limits, and expiry.
 */
@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Short public prefix (e.g., "vap_") displayed in the UI to identify the key without revealing the secret. */
    @Column(nullable = false)
    private String prefix;

    /** BCrypt hash of the full API key secret — the raw value is never persisted. */
    @Column(name = "key_hash", nullable = false, unique = true)
    private String keyHash;

    /** {@code true} if the key has been explicitly disabled before its expiry date. */
    private boolean revoked;

    /** Timestamp of the most recent successful request authenticated by this key; {@code null} if never used. */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /** Optional hard expiry; {@code null} means the key does not expire. */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** The user on whose behalf this key operates. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Maximum number of API requests allowed per minute; {@code null} means no per-minute cap. */
    @Column(name = "rate_limit_per_minute")
    private Integer rateLimitPerMinute;

    /** Maximum number of API requests allowed per hour; {@code null} means no per-hour cap. */
    @Column(name = "rate_limit_per_hour")
    private Integer rateLimitPerHour;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** List of permission scope strings this key is authorized to use (e.g., "user.read", "task.write"). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_key_scopes", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "scope")
    private List<String> scopes;

    /**
     * Sets {@code createdAt} to the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
