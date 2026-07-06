package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Join entity linking a {@link User} to an {@link Organization} with an assigned role,
 * enforcing a unique membership constraint so a user can belong to an organisation only once.
 */
@Entity
@Table(name = "organization_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The organisation this membership record belongs to. */
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /** The user who is a member of the organisation. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The member's role within the organisation: OWNER, ADMIN, or MEMBER. */
    @Column(nullable = false)
    private String role; // OWNER, ADMIN, MEMBER

    /** Timestamp when the user became a member of this organisation. */
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    /**
     * Records {@code joinedAt} as the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        joinedAt = LocalDateTime.now();
    }
}
