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

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("hasAuthority('organization.create')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(@Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Organization created successfully", organizationService.createOrganization(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getMyOrganizations() {
        return ResponseEntity.ok(ApiResponse.success("Organizations fetched successfully", organizationService.getMyOrganizations()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Organization fetched successfully", organizationService.getOrganizationById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(@PathVariable Long id, @Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Organization updated successfully", organizationService.updateOrganization(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(ApiResponse.success("Organization deleted successfully", null));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<OrganizationMemberResponse>>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Members fetched successfully", organizationService.getMembers(id)));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        organizationService.removeMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    @PostMapping("/{id}/invite")
    @PreAuthorize("hasAuthority('organization.invite')")
    public ResponseEntity<ApiResponse<InvitationResponse>> inviteMember(@PathVariable Long id, @Valid @RequestBody InviteMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Invitation sent successfully", organizationService.inviteMember(id, request)));
    }

    @GetMapping("/{id}/invitations")
    @PreAuthorize("hasAuthority('organization.invite')")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getInvitations(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invitations fetched successfully", organizationService.getInvitations(id)));
    }

    @DeleteMapping("/{id}/invitations/{invitationId}")
    @PreAuthorize("hasAuthority('organization.invite')")
    public ResponseEntity<ApiResponse<Void>> revokeInvitation(@PathVariable Long id, @PathVariable Long invitationId) {
        organizationService.revokeInvitation(id, invitationId);
        return ResponseEntity.ok(ApiResponse.success("Invitation revoked successfully", null));
    }
}
