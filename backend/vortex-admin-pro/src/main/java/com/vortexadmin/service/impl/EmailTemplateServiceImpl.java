package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.EmailTemplateRequest;
import com.vortexadmin.dto.response.EmailTemplateResponse;
import com.vortexadmin.entity.EmailTemplate;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.EmailTemplateRepository;
import com.vortexadmin.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;

    private EmailTemplateResponse mapToResponse(EmailTemplate template) {
        return EmailTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .subject(template.getSubject())
                .content(template.getContent())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    @Override
    public List<EmailTemplateResponse> getAllTemplates() {
        return emailTemplateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmailTemplateResponse getTemplateByName(String name) {
        return emailTemplateRepository.findByName(name)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Email template not found: " + name));
    }

    @Override
    @Transactional
    public EmailTemplateResponse saveTemplate(EmailTemplateRequest request) {
        EmailTemplate template = emailTemplateRepository.findByName(request.getName())
                .orElse(new EmailTemplate());
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setContent(request.getContent());
        return mapToResponse(emailTemplateRepository.save(template));
    }
}
