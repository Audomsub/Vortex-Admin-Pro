package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.InviteMemberRequest;
import com.vortexadmin.dto.request.OrganizationRequest;
import com.vortexadmin.dto.response.InvitationResponse;
import com.vortexadmin.dto.response.OrganizationMemberResponse;
import com.vortexadmin.dto.response.OrganizationResponse;
import com.vortexadmin.entity.Organization;
import com.vortexadmin.entity.OrganizationInvitation;
import com.vortexadmin.entity.OrganizationMember;
import com.vortexadmin.entity.User;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.OrganizationInvitationRepository;
import com.vortexadmin.repository.OrganizationMemberRepository;
import com.vortexadmin.repository.OrganizationRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.OrganizationService;
import com.vortexadmin.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private static final List<String> MANAGER_ROLES = List.of("OWNER", "ADMIN");

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMemberRepository memberRepository;

    @Autowired
    private OrganizationInvitationRepository invitationRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private OrganizationMember requireMembership(Long organizationId, Long userId) {
        return memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this organization"));
    }

    private void requireManager(Long organizationId, Long userId) {
        OrganizationMember member = requireMembership(organizationId, userId);
        if (!MANAGER_ROLES.contains(member.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You must be an owner or admin of this organization");
        }
    }

    private String generateSlug(String name) {
        String base = name.toLowerCase().trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isEmpty()) base = "org";
        String slug = base;
        int suffix = 1;
        while (organizationRepository.existsBySlug(slug)) {
            slug = base + "-" + (++suffix);
        }
        return slug;
    }

    private OrganizationResponse mapToResponse(Organization org, String currentUserRole) {
        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .logoUrl(org.getLogoUrl())
                .primaryColor(org.getPrimaryColor())
                .secondaryColor(org.getSecondaryColor())
                .planType(org.getPlanType())
                .ownerId(org.getOwner().getId())
                .ownerName(org.getOwner().getUsername())
                .memberCount(memberRepository.countByOrganizationId(org.getId()))
                .currentUserRole(currentUserRole)
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }

    private InvitationResponse mapToResponse(OrganizationInvitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .organizationId(invitation.getOrganization().getId())
                .organizationName(invitation.getOrganization().getName())
                .email(invitation.getEmail())
                .token(invitation.getToken())
                .role(invitation.getRole())
                .status(invitation.getStatus())
                .expiresAt(invitation.getExpiresAt())
                .createdAt(invitation.getCreatedAt())
                .build();
    }

    private OrganizationMemberResponse mapToResponse(OrganizationMember member) {
        User user = member.getUser();
        String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") + " "
                + (user.getLastName() != null ? user.getLastName() : "")).trim();
        return OrganizationMemberResponse.builder()
                .id(member.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(fullName.isEmpty() ? user.getUsername() : fullName)
                .avatarUrl(user.getAvatarUrl())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    @Override
    @Transactional
    public OrganizationResponse createOrganization(OrganizationRequest request) {
        User currentUser = getCurrentUser();

        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug().toLowerCase().trim()
                : generateSlug(request.getName());

        if (organizationRepository.existsBySlug(slug)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Slug is already taken");
        }

        Organization org = Organization.builder()
                .name(request.getName())
                .slug(slug)
                .logoUrl(request.getLogoUrl())
                .primaryColor(request.getPrimaryColor())
                .secondaryColor(request.getSecondaryColor())
                .planType("FREE")
                .owner(currentUser)
                .build();
        org = organizationRepository.save(org);

        OrganizationMember ownerMember = OrganizationMember.builder()
                .organization(org)
                .user(currentUser)
                .role("OWNER")
                .build();
        memberRepository.save(ownerMember);

        return mapToResponse(org, "OWNER");
    }

    @Override
    public List<OrganizationResponse> getMyOrganizations() {
        Long userId = SecurityUtils.getCurrentUserId();
        return memberRepository.findByUserId(userId).stream()
                .map(m -> mapToResponse(m.getOrganization(), m.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public OrganizationResponse getOrganizationById(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));
        OrganizationMember member = requireMembership(id, SecurityUtils.getCurrentUserId());
        return mapToResponse(org, member.getRole());
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(Long id, OrganizationRequest request) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));
        Long userId = SecurityUtils.getCurrentUserId();
        requireManager(id, userId);

        org.setName(request.getName());
        if (request.getLogoUrl() != null) {
            org.setLogoUrl(request.getLogoUrl());
        }
        if (request.getPrimaryColor() != null) {
            org.setPrimaryColor(request.getPrimaryColor());
        }
        if (request.getSecondaryColor() != null) {
            org.setSecondaryColor(request.getSecondaryColor());
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()
                && !request.getSlug().equalsIgnoreCase(org.getSlug())) {
            String newSlug = request.getSlug().toLowerCase().trim();
            if (organizationRepository.existsBySlug(newSlug)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Slug is already taken");
            }
            org.setSlug(newSlug);
        }

        Organization saved = organizationRepository.save(org);
        OrganizationMember member = requireMembership(id, userId);
        return mapToResponse(saved, member.getRole());
    }

    @Override
    @Transactional
    public void deleteOrganization(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));
        Long userId = SecurityUtils.getCurrentUserId();
        if (!org.getOwner().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the organization owner can delete it");
        }
        invitationRepository.deleteByOrganizationId(id);
        memberRepository.deleteByOrganizationId(id);
        organizationRepository.delete(org);
    }

    @Override
    public List<OrganizationMemberResponse> getMembers(Long organizationId) {
        requireMembership(organizationId, SecurityUtils.getCurrentUserId());
        return memberRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeMember(Long organizationId, Long userId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        requireManager(organizationId, currentUserId);

        OrganizationMember target = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found in this organization"));

        if ("OWNER".equals(target.getRole())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The organization owner cannot be removed");
        }
        memberRepository.delete(target);
    }

    @Override
    @Transactional
    public InvitationResponse inviteMember(Long organizationId, InviteMemberRequest request) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));
        User currentUser = getCurrentUser();
        requireManager(organizationId, currentUser.getId());

        String email = request.getEmail().toLowerCase().trim();

        userRepository.findByEmail(email).ifPresent(user -> {
            if (memberRepository.existsByOrganizationIdAndUserId(organizationId, user.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "User is already a member of this organization");
            }
        });

        if (invitationRepository.existsByOrganizationIdAndEmailAndStatus(organizationId, email, "PENDING")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A pending invitation already exists for this email");
        }

        OrganizationInvitation invitation = OrganizationInvitation.builder()
                .organization(org)
                .email(email)
                .role(request.getRole())
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status("PENDING")
                .invitedBy(currentUser)
                .build();

        return mapToResponse(invitationRepository.save(invitation));
    }

    @Override
    public List<InvitationResponse> getInvitations(Long organizationId) {
        requireManager(organizationId, SecurityUtils.getCurrentUserId());
        return invitationRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvitationResponse> getMyPendingInvitations() {
        User currentUser = getCurrentUser();
        return invitationRepository.findByEmailAndStatus(currentUser.getEmail().toLowerCase(), "PENDING").stream()
                .filter(inv -> inv.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrganizationResponse acceptInvitation(String token) {
        OrganizationInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invitation not found"));

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invitation is no longer valid");
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus("EXPIRED");
            invitationRepository.save(invitation);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invitation has expired");
        }

        User currentUser = getCurrentUser();
        if (!currentUser.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This invitation was sent to a different email address");
        }

        Organization org = invitation.getOrganization();
        if (memberRepository.existsByOrganizationIdAndUserId(org.getId(), currentUser.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You are already a member of this organization");
        }

        OrganizationMember member = OrganizationMember.builder()
                .organization(org)
                .user(currentUser)
                .role(invitation.getRole())
                .build();
        memberRepository.save(member);

        invitation.setStatus("ACCEPTED");
        invitationRepository.save(invitation);

        return mapToResponse(org, member.getRole());
    }

    @Override
    @Transactional
    public void revokeInvitation(Long organizationId, Long invitationId) {
        requireManager(organizationId, SecurityUtils.getCurrentUserId());
        OrganizationInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invitation not found"));
        if (!invitation.getOrganization().getId().equals(organizationId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invitation does not belong to this organization");
        }
        invitation.setStatus("REVOKED");
        invitationRepository.save(invitation);
    }
}
