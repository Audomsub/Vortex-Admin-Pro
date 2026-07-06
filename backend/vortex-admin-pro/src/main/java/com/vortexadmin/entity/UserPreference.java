package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores per-user UI preferences such as locale and color theme, held in a
 * one-to-one relationship with {@link User}.
 */
@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user whose preferences are stored here; each user has exactly one preference record. */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** BCP 47 language code for the UI locale (e.g., "en", "th"); defaults to "en". */
    @Column(nullable = false)
    private String language; // en, th

    /** UI color scheme selection ("dark" or "light"); defaults to "dark". */
    @Column(nullable = false)
    private String theme; // dark, light

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Initializes audit timestamps and applies default locale ("en") and theme ("dark")
     * before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (language == null) language = "en";
        if (theme == null) theme = "dark";
    }

    /**
     * Refreshes {@code updatedAt} to the current time before every database update.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
