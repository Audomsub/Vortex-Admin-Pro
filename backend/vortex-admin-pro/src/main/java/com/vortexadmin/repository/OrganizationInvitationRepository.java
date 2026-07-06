package com.vortexadmin.repository;

import com.vortexadmin.entity.OrganizationInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link OrganizationInvitation} entities, providing standard
 * CRUD operations and custom methods for token-based acceptance, organization/email-scoped
 * lookups, and bulk deletion during organization teardown.
 */
@Repository
public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, Long> {

    /**
     * Finds an invitation by its unique acceptance token.
     * Used when a recipient clicks the invite link to accept membership.
     *
     * @param token the unique token embedded in the invitation link
     * @return an {@link Optional} containing the matching invitation, or empty if not found
     */
    Optional<OrganizationInvitation> findByToken(String token);

    /**
     * Returns all invitations sent for the specified organization.
     *
     * @param organizationId the primary key of the organization
     * @return a list of all invitations associated with the given organization
     */
    List<OrganizationInvitation> findByOrganizationId(Long organizationId);

    /**
     * Returns all invitations for the specified email address that match the given status.
     * Used to find pending invitations when a user registers or logs in for the first time.
     *
     * @param email  the email address of the invited user
     * @param status the invitation status to filter by (e.g., "PENDING", "ACCEPTED")
     * @return a list of matching invitations for the given email and status
     */
    List<OrganizationInvitation> findByEmailAndStatus(String email, String status);

    /**
     * Checks whether an invitation with the given status already exists for the specified
     * organization and email address.  Used to prevent duplicate pending invitations.
     *
     * @param organizationId the primary key of the organization
     * @param email          the email address of the invitee
     * @param status         the status to check for (e.g., "PENDING")
     * @return {@code true} if a matching invitation exists, otherwise {@code false}
     */
    boolean existsByOrganizationIdAndEmailAndStatus(Long organizationId, String email, String status);

    /**
     * Deletes all invitations for the specified organization using a native SQL DELETE.
     * Intended for use during organization hard-delete flows where cascading is handled manually.
     *
     * @param organizationId the primary key of the organization whose invitations should be removed
     */
    @Modifying
    @Query(value = "DELETE FROM organization_invitations WHERE organization_id = :organizationId", nativeQuery = true)
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);
}
