package com.vortexadmin.controller;

import com.vortexadmin.dto.request.TwoFactorVerifyRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.TwoFactorSetupResponse;
import com.vortexadmin.dto.response.TwoFactorStatusResponse;
import com.vortexadmin.service.TwoFactorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/2fa")
public class TwoFactorController {

    @Autowired
    private TwoFactorService twoFactorService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<TwoFactorStatusResponse>> getStatus() {
        return ResponseEntity.ok(ApiResponse.success("2FA status fetched successfully", twoFactorService.getStatus()));
    }

    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<TwoFactorSetupResponse>> setup() {
        return ResponseEntity.ok(ApiResponse.success("2FA setup initiated. Scan the QR code and verify.", twoFactorService.setup()));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<TwoFactorStatusResponse>> verify(@Valid @RequestBody TwoFactorVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication enabled. Save your backup codes.", twoFactorService.verifyAndEnable(request.getCode())));
    }

    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<Void>> disable(@Valid @RequestBody TwoFactorVerifyRequest request) {
        twoFactorService.disable(request.getCode());
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication disabled", null));
    }
}
