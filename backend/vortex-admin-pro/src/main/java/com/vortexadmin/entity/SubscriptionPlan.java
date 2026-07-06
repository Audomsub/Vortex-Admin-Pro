package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Defines a pricing tier available for subscription (e.g., FREE, PRO, BUSINESS, ENTERPRISE),
 * specifying price points, user cap, and storage quota.
 */
@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique tier identifier displayed to users (e.g., "FREE", "PRO", "BUSINESS", "ENTERPRISE"). */
    @Column(nullable = false, unique = true)
    private String name; // FREE, PRO, BUSINESS, ENTERPRISE

    /** Per-month price in the system's base currency charged on a monthly billing cycle (precision 10, scale 2). */
    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    /** Per-year price in the system's base currency charged on a yearly billing cycle; typically offers a discount (precision 10, scale 2). */
    @Column(name = "yearly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal yearlyPrice;

    /** Maximum number of active user accounts allowed under an organisation on this plan. */
    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    /** Maximum total file-storage quota in megabytes available to an organisation on this plan. */
    @Column(name = "max_storage_mb", nullable = false)
    private Long maxStorageMb;

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
