package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Stores a reusable email template with a subject line and body content,
 * identified by a unique machine-readable name for lookup at send time.
 */
@Entity
@Table(name = "email_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique machine-readable key used to look up this template at send time (e.g., "welcome", "password_reset"). */
    @Column(nullable = false, unique = true)
    private String name;

    /** Email subject line; may contain placeholder variables for personalization (e.g., "Hello {{firstName}}"). */
    @Column(nullable = false)
    private String subject;

    /** Full email body (HTML or plain text); may contain placeholder variables substituted at send time. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Automatically set by Hibernate on insert; not updatable. */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** Automatically maintained by Hibernate on every update. */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
