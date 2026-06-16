package com.vortexadmin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorStatusResponse {
    private boolean enabled;
    private int remainingBackupCodes;
    // Only populated once, immediately after 2FA is enabled
    private List<String> backupCodes;
}
