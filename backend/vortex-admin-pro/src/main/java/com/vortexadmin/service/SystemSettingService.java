package com.vortexadmin.service;

import com.vortexadmin.dto.request.SettingRequest;
import com.vortexadmin.dto.response.SettingResponse;

import java.util.List;

public interface SystemSettingService {
    List<SettingResponse> getAllSettings();
    SettingResponse getSettingByKey(String key);
    void updateSetting(SettingRequest request);
}
