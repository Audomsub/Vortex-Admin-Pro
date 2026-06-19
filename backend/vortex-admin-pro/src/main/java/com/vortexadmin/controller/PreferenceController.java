package com.vortexadmin.controller;

import com.vortexadmin.dto.request.PreferenceRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.PreferenceResponse;
import com.vortexadmin.service.PreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PreferenceResponse>> getMyPreferences() {
        return ResponseEntity.ok(ApiResponse.success("Preferences fetched successfully", preferenceService.getMyPreferences()));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PreferenceResponse>> updateMyPreferences(@Valid @RequestBody PreferenceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Preferences updated successfully", preferenceService.updateMyPreferences(request)));
    }
}
