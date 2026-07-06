package com.vortexadmin.service;

import com.vortexadmin.dto.request.PreferenceRequest;
import com.vortexadmin.dto.response.PreferenceResponse;

/**
 * Service contract for managing user-specific UI preferences such as theme, language,
 * and notification settings for the currently authenticated user.
 */
public interface PreferenceService {

    /**
     * Returns the current preferences for the authenticated user.
     * If no preference record exists yet, a default record is returned.
     *
     * @return the user's current preferences
     */
    PreferenceResponse getMyPreferences();

    /**
     * Updates and persists the preferences of the currently authenticated user.
     * Creates the preference record if one does not already exist.
     *
     * @param request the updated preference values
     * @return the saved preference response reflecting the new state
     */
    PreferenceResponse updateMyPreferences(PreferenceRequest request);
}
