package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an in-app notification delivered to a specific {@link User},
 * tracking whether the user has acknowledged it.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    /** {@code false} until the recipient explicitly marks the notification as read; defaults to {@code false} on creation. */
    private Boolean isRead;

    private LocalDateTime createdAt;

    /** The user who should receive and see this notification. */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Sets {@code createdAt} to the current time and initializes {@code isRead} to {@code false}
     * before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        isRead = false;
    }
}
