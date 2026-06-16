package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.PreferenceRequest;
import com.vortexadmin.dto.response.PreferenceResponse;
import com.vortexadmin.entity.User;
import com.vortexadmin.entity.UserPreference;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.UserPreferenceRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.PreferenceService;
import com.vortexadmin.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenceServiceImpl implements PreferenceService {

    @Autowired
    private UserPreferenceRepository preferenceRepository;

    @Autowired
    private UserRepository userRepository;

    private UserPreference getOrCreate() {
        Long userId = SecurityUtils.getCurrentUserId();
        return preferenceRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
            return preferenceRepository.save(UserPreference.builder()
                    .user(user)
                    .language("en")
                    .theme("dark")
                    .build());
        });
    }

    private PreferenceResponse mapToResponse(UserPreference preference) {
        return PreferenceResponse.builder()
                .language(preference.getLanguage())
                .theme(preference.getTheme())
                .build();
    }

    @Override
    @Transactional
    public PreferenceResponse getMyPreferences() {
        return mapToResponse(getOrCreate());
    }

    @Override
    @Transactional
    public PreferenceResponse updateMyPreferences(PreferenceRequest request) {
        UserPreference preference = getOrCreate();
        if (request.getLanguage() != null) {
            preference.setLanguage(request.getLanguage());
        }
        if (request.getTheme() != null) {
            preference.setTheme(request.getTheme());
        }
        return mapToResponse(preferenceRepository.save(preference));
    }
}
