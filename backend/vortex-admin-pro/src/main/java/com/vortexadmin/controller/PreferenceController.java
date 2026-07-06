package com.vortexadmin.controller;

import com.vortexadmin.dto.request.PreferenceRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.PreferenceResponse;
import com.vortexadmin.service.PreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles HTTP requests for managing the authenticated user's personal UI preferences,
 * such as theme and language settings, delegating business logic to PreferenceService.
 */
@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    /**
     * Retrieves the UI preferences for the currently authenticated user.
     *
     * @return the {@link PreferenceResponse} containing the user's current preference settings
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PreferenceResponse>> getMyPreferences() {
        return ResponseEntity.ok(ApiResponse.success("Preferences fetched successfully", preferenceService.getMyPreferences()));
    }

    /**
     * Updates the UI preferences for the currently authenticated user.
     *
     * @param request the preference update payload containing the new preference values
     * @return the updated {@link PreferenceResponse} reflecting the persisted preferences
     */
    @PutMapping
    public ResponseEntity<ApiResponse<PreferenceResponse>> updateMyPreferences(@Valid @RequestBody PreferenceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Preferences updated successfully", preferenceService.updateMyPreferences(request)));
    }
}
