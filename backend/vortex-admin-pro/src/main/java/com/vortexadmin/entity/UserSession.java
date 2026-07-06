package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Records a single authenticated session for a {@link User}, capturing the
 * originating device and network details for security monitoring and session management.
 */
@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** IP address from which the session was initiated. */
    private String ipAddress;

    /** Human-readable country name resolved from the IP address (e.g., "Thailand"). */
    private String country;

    /** ISO 3166-1 alpha-2 or alpha-3 country code derived from the IP (max 3 characters). */
    @Column(name = "country_code", length = 3)
    private String countryCode;

    /** Raw HTTP {@code User-Agent} header string identifying the browser or API client. */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /** Timestamp when the session was created (login event). */
    @Column(name = "login_at")
    private LocalDateTime loginAt;

    /** Timestamp when the session was explicitly ended; {@code null} means the session is still active. */
    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    /** The user who owns this session. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Records {@code loginAt} as the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        loginAt = LocalDateTime.now();
    }
}
