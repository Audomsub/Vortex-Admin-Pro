package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a pending email invitation for a person to join an {@link Organization},
 * tracking its lifecycle from issuance through acceptance, expiry, or revocation.
 */
@Entity
@Table(name = "organization_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The organisation the invitee is being invited to join. */
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /** Email address of the person being invited; used to match against a user account on acceptance. */
    @Column(nullable = false)
    private String email;

    /** Unique, unguessable token embedded in the invitation link sent to the invitee. */
    @Column(nullable = false, unique = true)
    private String token;

    /** Organisation-level role to be granted to the user upon accepting the invitation (e.g., ADMIN, MEMBER). */
    @Column(nullable = false)
    private String role; // role granted on acceptance: ADMIN, MEMBER

    /** Point in time after which the invitation can no longer be accepted. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Current lifecycle state of the invitation: PENDING, ACCEPTED, EXPIRED, or REVOKED; defaults to PENDING. */
    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, EXPIRED, REVOKED

    /** The user who sent this invitation; may be {@code null} if sent by the system. */
    @ManyToOne
    @JoinColumn(name = "invited_by_user_id")
    private User invitedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Sets {@code createdAt} to the current time and defaults {@code status} to "PENDING"
     * before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }
}
