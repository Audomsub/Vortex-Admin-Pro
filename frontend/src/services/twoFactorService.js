import api from '../api/axios';

/**
 * Service object for managing TOTP-based two-factor authentication.
 */
export const twoFactorService = {
    /**
     * Retrieves the current 2FA enablement status for the authenticated user.
     * GET /2fa/status
     * @returns {Promise<import('axios').AxiosResponse>} Response containing `{ enabled, remainingBackupCodes }`.
     */
    getStatus: () => api.get('/2fa/status'),

    /**
     * Initiates the 2FA setup flow, returning a TOTP secret and OTP auth URL
     * for QR code generation.
     * POST /2fa/setup
     * @returns {Promise<import('axios').AxiosResponse>} Response containing `{ secret, otpAuthUrl }`.
     */
    setup: () => api.post('/2fa/setup'),

    /**
     * Verifies the TOTP code entered by the user to activate 2FA and receive
     * one-time backup codes.
     * POST /2fa/verify
     * @param {string} code - The 6-digit TOTP code from the authenticator app.
     * @returns {Promise<import('axios').AxiosResponse>} Response containing `{ backupCodes }`.
     */
    verify: (code) => api.post('/2fa/verify', { code }),

    /**
     * Disables 2FA for the authenticated user after verifying the current TOTP
     * or a backup code.
     * POST /2fa/disable
     * @param {string} code - A valid TOTP code or backup code.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    disable: (code) => api.post('/2fa/disable', { code }),
};
