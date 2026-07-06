package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a tenant organisation within the multi-tenant system, grouping
 * users under a shared identity with customizable branding and a subscription plan.
 */
@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** URL-safe unique identifier used in subdomains or URL paths to address this organisation. */
    @Column(nullable = false, unique = true)
    private String slug;

    /** Publicly accessible URL of the organisation's logo image. */
    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    /** CSS hex color string for the organisation's primary brand color (e.g., "#3B82F6"). */
    @Column(name = "primary_color")
    private String primaryColor;

    /** CSS hex color string for the organisation's secondary brand color. */
    @Column(name = "secondary_color")
    private String secondaryColor;

    /** Subscription tier for this organisation: FREE, PRO, BUSINESS, or ENTERPRISE; defaults to FREE. */
    @Column(name = "plan_type")
    private String planType; // FREE, PRO, BUSINESS, ENTERPRISE

    /** The user who created or currently owns this organisation. */
    @ManyToOne
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Sets audit timestamps and defaults {@code planType} to "FREE" before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (planType == null) planType = "FREE";
    }

    /**
     * Refreshes {@code updatedAt} to the current time before every database update.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
