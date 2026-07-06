package com.vortexadmin.service;

import com.vortexadmin.dto.request.SettingRequest;
import com.vortexadmin.dto.response.SettingResponse;

import java.util.List;

/**
 * Service contract for managing system-wide configuration settings, such as maintenance mode,
 * password policy rules, and other administrative key/value configuration entries.
 */
public interface SystemSettingService {

    /**
     * Returns all system settings stored in the database.
     *
     * @return a list of all setting responses
     */
    List<SettingResponse> getAllSettings();

    /**
     * Returns a single system setting by its unique key.
     *
     * @param key the setting key to look up (e.g., "maintenance_mode", "pw_min_length")
     * @return the matching setting response
     * @throws com.vortexadmin.exception.ApiException if no setting with the given key exists
     */
    SettingResponse getSettingByKey(String key);

    /**
     * Creates or updates a system setting identified by its key.  If the key already exists
     * its value is overwritten; otherwise a new setting record is inserted.
     *
     * @param request the setting payload containing the key and the new value
     */
    void updateSetting(SettingRequest request);
}
