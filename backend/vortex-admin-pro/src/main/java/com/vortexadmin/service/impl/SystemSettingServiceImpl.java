package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.SettingRequest;
import com.vortexadmin.dto.response.SettingResponse;
import com.vortexadmin.entity.SystemSetting;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.SystemSettingRepository;
import com.vortexadmin.service.SystemSettingService;
import com.vortexadmin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles system-wide configuration business logic, including retrieval of all
 * key-value settings, lookup by key, and upsert operations that create the setting
 * if it does not already exist or update its value if it does.
 */
@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    /**
     * Maps a {@link SystemSetting} entity to a {@link SettingResponse} DTO.
     *
     * @param setting the system setting entity to map
     * @return the corresponding setting response DTO
     */
    private SettingResponse mapToResponse(SystemSetting setting) {
        return SettingResponse.builder()
                .id(setting.getId())
                .key(setting.getSettingKey())
                .value(setting.getSettingValue())
                .build();
    }

    /**
     * Returns all system settings stored in the database.
     *
     * @return a list of all setting response DTOs
     */
    @Override
    public List<SettingResponse> getAllSettings() {
        return systemSettingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns the system setting identified by the given key.
     *
     * @param key the setting key to look up
     * @return the setting response DTO for the matching key
     * @throws ApiException with {@code 404} if no setting with that key exists
     */
    @Override
    public SettingResponse getSettingByKey(String key) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Setting not found"));
        return mapToResponse(setting);
    }

    /**
     * Creates or updates the system setting identified by the key in the request.
     * If a setting with the given key already exists its value is updated; otherwise
     * a new setting record is created (upsert behaviour).
     *
     * @param request the upsert request containing the setting key and new value
     */
    @Override
    @Transactional
    public void updateSetting(SettingRequest request) {
        Optional<SystemSetting> settingOpt = systemSettingRepository.findBySettingKey(request.getKey());

        SystemSetting setting;
        if (settingOpt.isPresent()) {
            setting = settingOpt.get();
            setting.setSettingValue(request.getValue());
        } else {
            setting = SystemSetting.builder()
                    .settingKey(request.getKey())
                    .settingValue(request.getValue())
                    .build();
        }
        systemSettingRepository.save(setting);
    }
}
