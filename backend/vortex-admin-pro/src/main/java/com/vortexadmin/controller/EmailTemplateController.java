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

@RestController
@RequestMapping("/api/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @GetMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<List<EmailTemplateResponse>>> getAllTemplates() {
        return ResponseEntity.ok(ApiResponse.success("Success", emailTemplateService.getAllTemplates()));
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> getTemplate(@PathVariable String name) {
        return ResponseEntity.ok(ApiResponse.success("Success", emailTemplateService.getTemplateByName(name)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> saveTemplate(@Valid @RequestBody EmailTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Template saved", emailTemplateService.saveTemplate(request)));
    }
}
