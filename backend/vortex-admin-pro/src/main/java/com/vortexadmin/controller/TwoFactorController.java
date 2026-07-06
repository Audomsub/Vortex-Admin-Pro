package com.vortexadmin.controller;

import com.vortexadmin.dto.request.TwoFactorVerifyRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.TwoFactorSetupResponse;
import com.vortexadmin.dto.response.TwoFactorStatusResponse;
import com.vortexadmin.service.TwoFactorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles HTTP requests for two-factor authentication (2FA) lifecycle management,
 * including status checks, TOTP setup, verification, and disabling, delegating to TwoFactorService.
 */
@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    /**
     * Retrieves the current two-factor authentication status for the authenticated user.
     *
     * @return a {@link TwoFactorStatusResponse} indicating whether 2FA is enabled and any backup code info
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<TwoFactorStatusResponse>> getStatus() {
        return ResponseEntity.ok(ApiResponse.success("2FA status fetched successfully", twoFactorService.getStatus()));
    }

    /**
     * Initiates the 2FA setup process by generating a TOTP secret and QR code for the user to scan.
     *
     * @return a {@link TwoFactorSetupResponse} containing the QR code URL and base32 secret
     */
    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<TwoFactorSetupResponse>> setup() {
        return ResponseEntity.ok(ApiResponse.success("2FA setup initiated. Scan the QR code and verify.", twoFactorService.setup()));
    }

    /**
     * Verifies a TOTP code and enables two-factor authentication for the authenticated user.
     *
     * @param request the verification request containing the 6-digit TOTP code from the authenticator app
     * @return a {@link TwoFactorStatusResponse} confirming 2FA is now enabled along with generated backup codes
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<TwoFactorStatusResponse>> verify(@Valid @RequestBody TwoFactorVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication enabled. Save your backup codes.", twoFactorService.verifyAndEnable(request.getCode())));
    }

    /**
     * Disables two-factor authentication for the authenticated user after verifying their TOTP code.
     *
     * @param request the verification request containing the TOTP code to confirm identity before disabling 2FA
     * @return a success response with no data payload upon successful disablement
     */
    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<Void>> disable(@Valid @RequestBody TwoFactorVerifyRequest request) {
        twoFactorService.disable(request.getCode());
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication disabled", null));
    }
}
