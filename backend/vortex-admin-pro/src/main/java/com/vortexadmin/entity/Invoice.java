package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a billing invoice generated for a {@link Subscription} period,
 * against which one or more {@link Payment}s can be applied.
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The subscription that this invoice was generated for. */
    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    /** Total amount due for this invoice in the system's base currency (precision 10, scale 2). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** Human-readable, globally unique reference number displayed on the invoice document (e.g., "INV-2024-0001"). */
    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    /** Current payment state of this invoice: PAID, PENDING, FAILED, or REFUNDED. */
    @Column(nullable = false)
    private String status; // PAID, PENDING, FAILED, REFUNDED

    /** Timestamp when this invoice was generated and issued. */
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    /**
     * Records {@code issuedAt} as the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        issuedAt = LocalDateTime.now();
    }
}
