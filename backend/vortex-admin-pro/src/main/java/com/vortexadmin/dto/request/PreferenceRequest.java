package com.vortexadmin.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PreferenceRequest {

    @Pattern(regexp = "en|th|zh", message = "Language must be 'en', 'th' or 'zh'")
    private String language;

    @Pattern(regexp = "dark|light", message = "Theme must be 'dark' or 'light'")
    private String theme;
}
