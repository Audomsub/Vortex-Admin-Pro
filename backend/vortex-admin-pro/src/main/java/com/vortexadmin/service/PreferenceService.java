package com.vortexadmin.service;

import com.vortexadmin.dto.request.PreferenceRequest;
import com.vortexadmin.dto.response.PreferenceResponse;

public interface PreferenceService {

    PreferenceResponse getMyPreferences();

    PreferenceResponse updateMyPreferences(PreferenceRequest request);
}
