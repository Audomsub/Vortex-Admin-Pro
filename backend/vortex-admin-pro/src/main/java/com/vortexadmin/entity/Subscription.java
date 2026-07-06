package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an {@link Organization}'s active or historical subscription to a
 * {@link SubscriptionPlan}, tracking billing cycle and lifecycle dates.
 */
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The organisation that holds this subscription. */
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /** The pricing plan to which this subscription is tied. */
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    /** Current lifecycle state: ACTIVE, CANCELLED, or EXPIRED. */
    @Column(nullable = false)
    private String status; // ACTIVE, CANCELLED, EXPIRED

    /** Frequency at which the organisation is billed: MONTHLY or YEARLY. */
    @Column(name = "billing_cycle")
    private String billingCycle; // MONTHLY, YEARLY

    /** Date and time when this subscription period begins. */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /** Date and time when this subscription period ends; {@code null} for auto-renewing subscriptions. */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Sets audit timestamps before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Refreshes {@code updatedAt} to the current time before every database update.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
