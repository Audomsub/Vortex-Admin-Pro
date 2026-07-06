package com.vortexadmin.service;

import com.vortexadmin.dto.response.TwoFactorSetupResponse;
import com.vortexadmin.dto.response.TwoFactorStatusResponse;

/**
 * Service contract for TOTP-based two-factor authentication (2FA) lifecycle management,
 * including status retrieval, setup initiation, code verification, and deactivation.
 */
public interface TwoFactorService {

    /**
     * Returns the current 2FA enrollment status for the currently authenticated user.
     *
     * @return a {@link TwoFactorStatusResponse} indicating whether 2FA is enabled and,
     *         if so, the enrollment details
     */
    TwoFactorStatusResponse getStatus();

    /**
     * Initiates the 2FA setup process for the currently authenticated user by generating a
     * TOTP secret and returning a QR code URL and the raw secret for manual entry.
     *
     * @return a {@link TwoFactorSetupResponse} containing the OTP auth URI and backup secret
     */
    TwoFactorSetupResponse setup();

    /**
     * Verifies the provided TOTP code against the pending setup secret and, if valid, activates
     * 2FA for the currently authenticated user.
     *
     * @param code the 6-digit TOTP code from the authenticator application
     * @return a {@link TwoFactorStatusResponse} reflecting the newly enabled 2FA state
     * @throws com.vortexadmin.exception.ApiException if the code is invalid or the setup has
     *         not been initiated
     */
    TwoFactorStatusResponse verifyAndEnable(String code);

    /**
     * Disables 2FA for the currently authenticated user after verifying the provided TOTP code.
     *
     * @param code the 6-digit TOTP code used to confirm the disable action
     * @throws com.vortexadmin.exception.ApiException if the code is invalid or 2FA is not
     *         currently enabled for the user
     */
    void disable(String code);
}
