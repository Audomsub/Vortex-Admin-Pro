package com.vortexadmin.service;

import com.vortexadmin.dto.response.TwoFactorSetupResponse;
import com.vortexadmin.dto.response.TwoFactorStatusResponse;

public interface TwoFactorService {

    TwoFactorStatusResponse getStatus();

    TwoFactorSetupResponse setup();

    TwoFactorStatusResponse verifyAndEnable(String code);

    void disable(String code);
}
