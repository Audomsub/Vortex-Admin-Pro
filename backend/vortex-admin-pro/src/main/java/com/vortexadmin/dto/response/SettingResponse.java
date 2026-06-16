package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettingResponse {
    private Long id;
    private String key;
    private String value;
}
