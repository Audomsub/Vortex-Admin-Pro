package com.vortexadmin.controller;

import com.vortexadmin.dto.request.SettingRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.SettingResponse;
import com.vortexadmin.service.SystemSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests for system-wide configuration settings, allowing administrators
 * to view and update key-value settings, delegating business logic to SystemSettingService.
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    /**
     * Retrieves all system settings available in the application.
     *
     * @return a list of {@link SettingResponse} objects representing all configuration entries
     */
    @GetMapping
    @PreAuthorize("hasAuthority('settings.view')")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success("Settings fetched", systemSettingService.getAllSettings()));
    }

    /**
     * Retrieves a single system setting by its configuration key.
     *
     * @param key the unique string key of the setting to retrieve (e.g., {@code "maintenance_mode"})
     * @return the {@link SettingResponse} for the specified key
     */
    @GetMapping("/{key}")
    @PreAuthorize("hasAuthority('settings.view')")
    public ResponseEntity<ApiResponse<SettingResponse>> getSettingByKey(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success("Setting fetched", systemSettingService.getSettingByKey(key)));
    }

    /**
     * Creates or updates a system setting with the provided key-value pair.
     *
     * @param request the setting payload containing the key and new value
     * @return a success response with no data payload upon successful upsert
     */
    @PostMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<Void>> updateSetting(@Valid @RequestBody SettingRequest request) {
        systemSettingService.updateSetting(request);
        return ResponseEntity.ok(ApiResponse.success("Setting updated successfully", null));
    }
}
