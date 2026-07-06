package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores a single configurable, system-wide key-value setting that controls
 * application behaviour (e.g., maintenance mode, max login attempts, feature flags).
 */
@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique identifier key for this setting (e.g., "maintenance_mode", "max_login_attempts"). */
    @Column(name = "setting_key", nullable = false, unique = true)
    private String settingKey;

    /** Current value of the setting, serialized as plain text or JSON depending on the setting type. */
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    /** Human-readable explanation of what this setting controls, shown in the admin UI. */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Sets {@code createdAt} to the current time before the first database insert.
     */
    @PrePersist
    public void preSave() {
        createdAt = LocalDateTime.now();
    }
}
