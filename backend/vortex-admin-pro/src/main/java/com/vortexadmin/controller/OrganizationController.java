package com.vortexadmin.controller;

import com.vortexadmin.dto.request.InviteMemberRequest;
import com.vortexadmin.dto.request.OrganizationRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.InvitationResponse;
import com.vortexadmin.dto.response.OrganizationMemberResponse;
import com.vortexadmin.dto.response.OrganizationResponse;
import com.vortexadmin.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests for organization (tenant) management, including CRUD operations,
 * member management, and invitation lifecycle, delegating all business logic to OrganizationService.
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * Creates a new organization owned by the authenticated user.
     *
     * @param request the organization creation payload containing the name and optional details
     * @return the created {@link OrganizationResponse} reflecting the persisted organization
     */
    @PostMapping
    @PreAuthorize("hasAuthority('organization.create')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(@Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Organization created successfully", organizationService.createOrganization(request)));
    }

    /**
     * Retrieves all organizations that the authenticated user belongs to.
     *
     * @return a list of {@link OrganizationResponse} objects for the user's organizations
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getMyOrganizations() {
        return ResponseEntity.ok(ApiResponse.success("Organizations fetched successfully", organizationService.getMyOrganizations()));
    }

    /**
     * Retrieves a single organization by its unique identifier.
     *
     * @param id the unique ID of the organization to retrieve
     * @return the {@link OrganizationResponse} for the specified organization
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Organization fetched successfully", organizationService.getOrganizationById(id)));
    }

    /**
     * Updates an existing organization's details by its unique identifier.
     *
     * @param id      the unique ID of the organization to update
     * @param request the update payload containing the new name and/or other organization details
     * @return the updated {@link OrganizationResponse} reflecting the persisted changes
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(@PathVariable Long id, @Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Organization updated successfully", organizationService.updateOrganization(id, request)));
    }

    /**
     * Deletes an organization and all associated data by its unique identifier.
     *
     * @param id the unique ID of the organization to delete
     * @return a success response with no data payload upon successful deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(ApiResponse.success("Organization deleted successfully", null));
    }

    /**
     * Retrieves all members of a specific organization.
     *
     * @param id the unique ID of the organization whose members are being requested
     * @return a list of {@link OrganizationMemberResponse} objects representing the organization's members
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<OrganizationMemberResponse>>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Members fetched successfully", organizationService.getMembers(id)));
    }

    /**
     * Removes a member from an organization.
     *
     * @param id     the unique ID of the organization from which the member will be removed
     * @param userId the unique ID of the user to remove from the organization
     * @return a success response with no data payload upon successful removal
     */
    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        organizationService.removeMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    /**
     * Sends an email invitation to a new member to join an organization.
     *
     * @param id      the unique ID of the organization to invite the member to
     * @param request the invite payload containing the invitee's email and optional role
     * @return the created {@link InvitationResponse} containing the invitation token and expiry details
     */
    @PostMapping("/{id}/invite")
    @PreAuthorize("hasAuthority('organization.invite')")
    public ResponseEntity<ApiResponse<InvitationResponse>> inviteMember(@PathVariable Long id, @Valid @RequestBody InviteMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Invitation sent successfully", organizationService.inviteMember(id, request)));
    }

    /**
     * Retrieves all pending and accepted invitations for a specific organization.
     *
     * @param id the unique ID of the organization whose invitations are being listed
     * @return a list of {@link InvitationResponse} objects representing all invitations for the organization
     */
    @GetMapping("/{id}/invitations")
    @PreAuthorize("hasAuthority('organization.invite')")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getInvitations(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invitations fetched successfully", organizationService.getInvitations(id)));
    }

    /**
     * Revokes a pending invitation, preventing the invitee from accepting it.
     *
     * @param id           the unique ID of the organization that issued the invitation
     * @param invitationId the unique ID of the invitation to revoke
     * @return a success response with no data payload upon successful revocation
     */
    @DeleteMapping("/{id}/invitations/{invitationId}")
    @PreAuthorize("hasAuthority('organization.invite')")
    public ResponseEntity<ApiResponse<Void>> revokeInvitation(@PathVariable Long id, @PathVariable Long invitationId) {
        organizationService.revokeInvitation(id, invitationId);
        return ResponseEntity.ok(ApiResponse.success("Invitation revoked successfully", null));
    }
}
