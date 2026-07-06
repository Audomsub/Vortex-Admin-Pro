package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Records a single payment transaction attempt made against an {@link Invoice},
 * capturing the gateway used and the outcome of the charge.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The invoice this payment is settling. */
    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /** Amount charged in the system's base currency (precision 10, scale 2). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** The payment gateway through which this transaction was processed (e.g., MOCK, STRIPE, PROMPTPAY). */
    @Column(name = "payment_provider")
    private String paymentProvider; // MOCK, STRIPE, PROMPTPAY

    /** Outcome of the payment attempt: SUCCESS, FAILED, or PENDING. */
    @Column(nullable = false)
    private String status; // SUCCESS, FAILED, PENDING

    /** Timestamp when the payment attempt was made. */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * Records {@code paidAt} as the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        paidAt = LocalDateTime.now();
    }
}
