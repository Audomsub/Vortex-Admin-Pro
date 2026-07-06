package com.vortexadmin.controller;

import com.vortexadmin.dto.request.AcceptInvitationRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.InvitationResponse;
import com.vortexadmin.dto.response.OrganizationResponse;
import com.vortexadmin.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests for organization invitation flows, allowing users to view
 * pending invitations and accept them to join an organization, delegating to OrganizationService.
 */
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final OrganizationService organizationService;

    /**
     * Retrieves all pending organization invitations for the currently authenticated user.
     *
     * @return a list of {@link InvitationResponse} objects representing outstanding invitations
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getMyPendingInvitations() {
        return ResponseEntity.ok(ApiResponse.success("Pending invitations fetched successfully", organizationService.getMyPendingInvitations()));
    }

    /**
     * Accepts an organization invitation using the provided token, adding the user to the organization.
     *
     * @param request the accept-invitation payload containing the invitation token from the email link
     * @return the {@link OrganizationResponse} of the organization that the user has just joined
     */
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<OrganizationResponse>> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted successfully", organizationService.acceptInvitation(request.getToken())));
    }
}
