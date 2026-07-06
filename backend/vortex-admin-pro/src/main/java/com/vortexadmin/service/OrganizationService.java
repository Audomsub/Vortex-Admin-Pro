package com.vortexadmin.service;

import com.vortexadmin.dto.request.InviteMemberRequest;
import com.vortexadmin.dto.request.OrganizationRequest;
import com.vortexadmin.dto.response.InvitationResponse;
import com.vortexadmin.dto.response.OrganizationMemberResponse;
import com.vortexadmin.dto.response.OrganizationResponse;

import java.util.List;

/**
 * Service contract for multi-tenant organization management including CRUD operations,
 * member management, and invitation lifecycle (send, accept, revoke).
 */
public interface OrganizationService {

    /**
     * Creates a new organization and automatically adds the currently authenticated user as
     * the initial owner/member.
     *
     * @param request the organization creation payload including name and optional slug
     * @return the newly created organization response
     * @throws com.vortexadmin.exception.ApiException if the slug is already in use
     */
    OrganizationResponse createOrganization(OrganizationRequest request);

    /**
     * Returns all organizations of which the currently authenticated user is a member.
     *
     * @return a list of organization responses for the calling user
     */
    List<OrganizationResponse> getMyOrganizations();

    /**
     * Returns a single organization by its primary key.
     *
     * @param id the primary key of the organization to retrieve
     * @return the matching organization response
     * @throws com.vortexadmin.exception.ApiException if no organization with the given ID exists
     */
    OrganizationResponse getOrganizationById(Long id);

    /**
     * Updates an existing organization's details such as name or slug.
     *
     * @param id      the primary key of the organization to update
     * @param request the updated organization data
     * @return the updated organization response
     * @throws com.vortexadmin.exception.ApiException if the organization is not found
     */
    OrganizationResponse updateOrganization(Long id, OrganizationRequest request);

    /**
     * Permanently deletes the specified organization along with all its members, invitations,
     * subscriptions, and invoices.
     *
     * @param id the primary key of the organization to delete
     * @throws com.vortexadmin.exception.ApiException if the organization is not found
     */
    void deleteOrganization(Long id);

    /**
     * Returns all current members of the specified organization.
     *
     * @param organizationId the primary key of the organization
     * @return a list of member responses for the given organization
     */
    List<OrganizationMemberResponse> getMembers(Long organizationId);

    /**
     * Removes a user from the specified organization.
     *
     * @param organizationId the primary key of the organization
     * @param userId         the primary key of the user to remove
     * @throws com.vortexadmin.exception.ApiException if the membership record is not found
     */
    void removeMember(Long organizationId, Long userId);

    /**
     * Sends a membership invitation email to the specified email address and persists the
     * invitation record with a unique acceptance token.
     *
     * @param organizationId the primary key of the organization sending the invitation
     * @param request        the invitation payload containing the invitee's email address
     * @return the created invitation response including the generated token
     * @throws com.vortexadmin.exception.ApiException if a pending invitation already exists
     *         for that email in this organization
     */
    InvitationResponse inviteMember(Long organizationId, InviteMemberRequest request);

    /**
     * Returns all invitations (in any status) sent by the specified organization.
     *
     * @param organizationId the primary key of the organization
     * @return a list of invitation responses for the given organization
     */
    List<InvitationResponse> getInvitations(Long organizationId);

    /**
     * Returns all pending invitations addressed to the currently authenticated user's email.
     *
     * @return a list of pending invitation responses for the calling user
     */
    List<InvitationResponse> getMyPendingInvitations();

    /**
     * Accepts a membership invitation identified by the given token and adds the currently
     * authenticated user to the corresponding organization.
     *
     * @param token the unique acceptance token from the invitation link
     * @return the organization the user has just joined
     * @throws com.vortexadmin.exception.ApiException if the token is invalid, expired, or the
     *         user is already a member
     */
    OrganizationResponse acceptInvitation(String token);

    /**
     * Revokes (cancels) a pending invitation so it can no longer be accepted.
     *
     * @param organizationId the primary key of the organization that issued the invitation
     * @param invitationId   the primary key of the invitation to revoke
     * @throws com.vortexadmin.exception.ApiException if the invitation is not found
     */
    void revokeInvitation(Long organizationId, Long invitationId);
}
