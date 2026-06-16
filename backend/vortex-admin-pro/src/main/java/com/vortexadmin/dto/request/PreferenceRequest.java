package com.vortexadmin.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PreferenceRequest {

    @Pattern(regexp = "en|th", message = "Language must be 'en' or 'th'")
    private String language;

    @Pattern(regexp = "dark|light", message = "Theme must be 'dark' or 'light'")
    private String theme;
}
