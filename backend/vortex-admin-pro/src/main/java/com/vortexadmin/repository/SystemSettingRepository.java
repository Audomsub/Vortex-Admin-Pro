package com.vortexadmin.repository;

import com.vortexadmin.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link SystemSetting} entities, providing standard CRUD
 * operations and a key-based lookup method for individual configuration entries.
 */
@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

    /**
     * Finds a system setting by its unique configuration key.
     *
     * @param settingKey the key identifying the setting (e.g., "maintenance_mode", "pw_min_length")
     * @return an {@link Optional} containing the matching {@link SystemSetting},
     *         or empty if no setting with that key exists
     */
    Optional<SystemSetting> findBySettingKey(String settingKey);
}
