package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Records the result of a single outbound webhook dispatch attempt to a
 * {@link WebhookEndpoint}, preserving the payload, HTTP response, and success status
 * for auditing and retry decisions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "webhook_deliveries")
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The endpoint to which this delivery was dispatched. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_endpoint_id", nullable = false)
    private WebhookEndpoint webhookEndpoint;

    /** The event type name that triggered this delivery (e.g., "user.created"). */
    @Column(nullable = false)
    private String eventType;

    /** JSON body that was sent in the HTTP POST request to the endpoint. */
    @Column(columnDefinition = "TEXT")
    private String payload;

    /** HTTP response status code returned by the endpoint (e.g., 200, 404, 500). */
    private Integer statusCode;

    /** Raw response body returned by the endpoint, captured for debugging failed deliveries. */
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    /** {@code true} if the endpoint responded with a 2xx HTTP status code. */
    private boolean success;

    /** Automatically set by Hibernate to the time the delivery attempt was made; not updatable. */
    @CreationTimestamp
    @Column(name = "delivered_at", updatable = false)
    private LocalDateTime deliveredAt;
}
