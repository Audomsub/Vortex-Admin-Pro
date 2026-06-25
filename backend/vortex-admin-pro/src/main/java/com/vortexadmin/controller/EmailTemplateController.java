package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.entity.EmailTemplate;
import com.vortexadmin.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateRepository emailTemplateRepository;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EmailTemplate>>> getAllTemplates() {
        return ResponseEntity.ok(ApiResponse.success("Success", emailTemplateRepository.findAll()));
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmailTemplate>> getTemplate(@PathVariable String name) {
        return emailTemplateRepository.findByName(name)
                .map(t -> ResponseEntity.ok(ApiResponse.success("Success", t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmailTemplate>> saveTemplate(@RequestBody EmailTemplate request) {
        EmailTemplate template = emailTemplateRepository.findByName(request.getName())
                .orElse(new EmailTemplate());
        
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setContent(request.getContent());
        
        return ResponseEntity.ok(ApiResponse.success("Template saved", emailTemplateRepository.save(template)));
    }
}
