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

@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    private SettingResponse mapToResponse(SystemSetting setting) {
        return SettingResponse.builder()
                .id(setting.getId())
                .key(setting.getSettingKey())
                .value(setting.getSettingValue())
                .build();
    }

    @Override
    public List<SettingResponse> getAllSettings() {
        return systemSettingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SettingResponse getSettingByKey(String key) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Setting not found"));
        return mapToResponse(setting);
    }

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
