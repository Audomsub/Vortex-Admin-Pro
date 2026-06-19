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

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping
    @PreAuthorize("hasAuthority('settings.view')")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success("Settings fetched", systemSettingService.getAllSettings()));
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasAuthority('settings.view')")
    public ResponseEntity<ApiResponse<SettingResponse>> getSettingByKey(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success("Setting fetched", systemSettingService.getSettingByKey(key)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<Void>> updateSetting(@Valid @RequestBody SettingRequest request) {
        systemSettingService.updateSetting(request);
        return ResponseEntity.ok(ApiResponse.success("Setting updated successfully", null));
    }
}
