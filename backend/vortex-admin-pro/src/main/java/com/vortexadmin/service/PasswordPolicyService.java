package com.vortexadmin.service;

import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Service responsible for enforcing the system-configured password policy by validating
 * proposed passwords against rules stored as {@link com.vortexadmin.entity.SystemSetting}
 * entries (minimum length, character-class requirements, etc.).
 */
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {

    private final SystemSettingRepository settingRepository;

    /**
     * Validates the provided password against the currently active password policy.
     * Policy rules are read dynamically from the {@code system_settings} table so that
     * administrators can adjust requirements without redeploying the application.
     * <p>
     * Evaluated rules (controlled by the corresponding setting keys):
     * <ul>
     *   <li>{@code pw_min_length} — minimum character length (default: 8)</li>
     *   <li>{@code pw_require_uppercase} — must contain at least one uppercase letter</li>
     *   <li>{@code pw_require_lowercase} — must contain at least one lowercase letter</li>
     *   <li>{@code pw_require_digit} — must contain at least one digit</li>
     *   <li>{@code pw_require_special} — must contain at least one special character</li>
     * </ul>
     *
     * @param password the plain-text password to validate
     * @throws ApiException with HTTP 400 if the password is {@code null}, too short, or
     *         fails any enabled character-class requirement
     */
    public void validate(String password) {
        int minLength       = getInt("pw_min_length", 8);
        boolean reqUpper    = getBool("pw_require_uppercase", false);
        boolean reqLower    = getBool("pw_require_lowercase", false);
        boolean reqDigit    = getBool("pw_require_digit", false);
        boolean reqSpecial  = getBool("pw_require_special", false);

        if (password == null || password.length() < minLength) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Password must be at least " + minLength + " characters long");
        }
        if (reqUpper && !password.matches(".*[A-Z].*")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Password must contain at least one uppercase letter");
        }
        if (reqLower && !password.matches(".*[a-z].*")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Password must contain at least one lowercase letter");
        }
        if (reqDigit && !password.matches(".*[0-9].*")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Password must contain at least one digit");
        }
        if (reqSpecial && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Password must contain at least one special character");
        }
    }

    /**
     * Reads an integer setting value from the database, falling back to the provided default
     * when the setting is absent or its value cannot be parsed as an integer.
     *
     * @param key          the setting key to look up
     * @param defaultValue the value to return when the setting is missing or invalid
     * @return the parsed integer value, or {@code defaultValue}
     */
    private int getInt(String key, int defaultValue) {
        return settingRepository.findBySettingKey(key)
                .map(s -> {
                    try { return Integer.parseInt(s.getSettingValue()); }
                    catch (NumberFormatException e) { return defaultValue; }
                })
                .orElse(defaultValue);
    }

    /**
     * Reads a boolean setting value from the database, falling back to the provided default
     * when the setting is absent.  The string {@code "true"} (case-insensitive) is treated as
     * {@code true}; all other values are treated as {@code false}.
     *
     * @param key          the setting key to look up
     * @param defaultValue the value to return when the setting is missing
     * @return {@code true} if the setting value equals {@code "true"} (case-insensitive),
     *         otherwise {@code defaultValue}
     */
    private boolean getBool(String key, boolean defaultValue) {
        return settingRepository.findBySettingKey(key)
                .map(s -> "true".equalsIgnoreCase(s.getSettingValue()))
                .orElse(defaultValue);
    }
}
