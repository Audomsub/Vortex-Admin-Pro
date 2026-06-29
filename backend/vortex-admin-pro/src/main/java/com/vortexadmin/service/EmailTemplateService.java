package com.vortexadmin.service;

import com.vortexadmin.dto.request.EmailTemplateRequest;
import com.vortexadmin.dto.response.EmailTemplateResponse;

import java.util.List;

public interface EmailTemplateService {
    List<EmailTemplateResponse> getAllTemplates();
    EmailTemplateResponse getTemplateByName(String name);
    EmailTemplateResponse saveTemplate(EmailTemplateRequest request);
}
