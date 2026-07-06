package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a user-configured HTTP endpoint that receives outbound webhook
 * event notifications for a specific set of event types within an organisation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "webhook_endpoints")
public class WebhookEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** The target URL that will receive HTTP POST requests carrying webhook event payloads. */
    @Column(nullable = false)
    private String url;

    /** HMAC signing secret used to generate the request signature header so the receiver can verify authenticity. */
    @Column(nullable = false)
    private String secret;

    /** Comma-separated list of event type names this endpoint is subscribed to (e.g., "user.created,task.completed"). */
    @Column(name = "events_subscribed")
    private String eventsSubscribed; // Comma separated events like "user.created,task.completed"

    /** Denormalized foreign key to the owning organisation; stored as a plain {@code Long} rather than a JPA relation. */
    @Column(name = "organization_id")
    private Long organizationId;

    /** {@code true} if the endpoint is enabled and should receive deliveries; set to {@code false} to pause without deletion. */
    private boolean active;

    /** Automatically set by Hibernate on insert; not updatable. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Automatically maintained by Hibernate on every update. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
