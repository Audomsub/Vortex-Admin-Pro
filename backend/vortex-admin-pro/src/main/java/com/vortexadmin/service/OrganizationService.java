package com.vortexadmin.service;

import com.vortexadmin.dto.request.InviteMemberRequest;
import com.vortexadmin.dto.request.OrganizationRequest;
import com.vortexadmin.dto.response.InvitationResponse;
import com.vortexadmin.dto.response.OrganizationMemberResponse;
import com.vortexadmin.dto.response.OrganizationResponse;

import java.util.List;

public interface OrganizationService {

    OrganizationResponse createOrganization(OrganizationRequest request);

    List<OrganizationResponse> getMyOrganizations();

    OrganizationResponse getOrganizationById(Long id);

    OrganizationResponse updateOrganization(Long id, OrganizationRequest request);

    void deleteOrganization(Long id);

    List<OrganizationMemberResponse> getMembers(Long organizationId);

    void removeMember(Long organizationId, Long userId);

    InvitationResponse inviteMember(Long organizationId, InviteMemberRequest request);

    List<InvitationResponse> getInvitations(Long organizationId);

    List<InvitationResponse> getMyPendingInvitations();

    OrganizationResponse acceptInvitation(String token);

    void revokeInvitation(Long organizationId, Long invitationId);
}
