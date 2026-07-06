package com.vortexadmin.controller;

import com.vortexadmin.dto.request.EmailTemplateRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.EmailTemplateResponse;
import com.vortexadmin.service.EmailTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests for managing email templates used by the system for transactional emails,
 * delegating all business logic to EmailTemplateService.
 */
@RestController
@RequestMapping("/api/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    /**
     * Retrieves all email templates configured in the system.
     *
     * @return a list of {@link EmailTemplateResponse} objects representing all available email templates
     */
    @GetMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<List<EmailTemplateResponse>>> getAllTemplates() {
        return ResponseEntity.ok(ApiResponse.success("Success", emailTemplateService.getAllTemplates()));
    }

    /**
     * Retrieves a single email template by its unique name identifier.
     *
     * @param name the unique name/slug of the email template (e.g., {@code "welcome"}, {@code "password-reset"})
     * @return the {@link EmailTemplateResponse} for the specified template
     */
    @GetMapping("/{name}")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> getTemplate(@PathVariable String name) {
        return ResponseEntity.ok(ApiResponse.success("Success", emailTemplateService.getTemplateByName(name)));
    }

    /**
     * Creates or updates an email template with the provided subject and body content.
     *
     * @param request the template payload containing the name, subject line, and HTML/text body
     * @return the saved {@link EmailTemplateResponse} reflecting the persisted template
     */
    @PostMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> saveTemplate(@Valid @RequestBody EmailTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Template saved", emailTemplateService.saveTemplate(request)));
    }
}
