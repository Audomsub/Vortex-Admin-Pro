package com.vortexadmin.service;

import com.vortexadmin.dto.request.EmailTemplateRequest;
import com.vortexadmin.dto.response.EmailTemplateResponse;

import java.util.List;

/**
 * Service contract for managing reusable email templates that can be rendered and sent
 * for various system events such as welcome emails and password-reset notifications.
 */
public interface EmailTemplateService {

    /**
     * Returns all email templates stored in the system.
     *
     * @return a list of all email template responses
     */
    List<EmailTemplateResponse> getAllTemplates();

    /**
     * Returns a single email template identified by its unique name.
     *
     * @param name the template name to look up (e.g., "welcome", "password-reset")
     * @return the matching email template response
     * @throws com.vortexadmin.exception.ApiException if no template with the given name exists
     */
    EmailTemplateResponse getTemplateByName(String name);

    /**
     * Creates a new email template or updates an existing one if a template with the same
     * name already exists.
     *
     * @param request the template payload including the name, subject, and HTML body
     * @return the saved email template response
     */
    EmailTemplateResponse saveTemplate(EmailTemplateRequest request);
}
