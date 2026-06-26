package com.vortexadmin.service;

import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordPolicyService {

    private final SystemSettingRepository settingRepository;

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

    private int getInt(String key, int defaultValue) {
        return settingRepository.findBySettingKey(key)
                .map(s -> {
                    try { return Integer.parseInt(s.getSettingValue()); }
                    catch (NumberFormatException e) { return defaultValue; }
                })
                .orElse(defaultValue);
    }

    private boolean getBool(String key, boolean defaultValue) {
        return settingRepository.findBySettingKey(key)
                .map(s -> "true".equalsIgnoreCase(s.getSettingValue()))
                .orElse(defaultValue);
    }
}
