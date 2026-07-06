package com.vortexadmin.repository;

import com.vortexadmin.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link OrganizationMember} entities, providing standard CRUD
 * operations and custom methods for organization/user-scoped membership management including
 * projection queries and bulk deletion.
 */
@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    /**
     * Returns all membership records for the specified organization.
     *
     * @param organizationId the primary key of the organization
     * @return a list of all membership entries for the given organization
     */
    List<OrganizationMember> findByOrganizationId(Long organizationId);

    /**
     * Returns only the user IDs of all members belonging to the specified organization.
     * Used for storage-usage calculations and bulk user-scoped queries without loading
     * full member entities.
     *
     * @param organizationId the primary key of the organization
     * @return a list of user primary keys that are members of the given organization
     */
    @Query("SELECT m.user.id FROM OrganizationMember m WHERE m.organization.id = :organizationId")
    List<Long> findUserIdsByOrganizationId(@Param("organizationId") Long organizationId);

    /**
     * Returns all organizations the specified user is a member of.
     *
     * @param userId the primary key of the user
     * @return a list of membership records for the given user across all organizations
     */
    List<OrganizationMember> findByUserId(Long userId);

    /**
     * Finds the membership record for a specific user within a specific organization.
     *
     * @param organizationId the primary key of the organization
     * @param userId         the primary key of the user
     * @return an {@link Optional} containing the membership record if it exists, otherwise empty
     */
    Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId);

    /**
     * Checks whether the specified user is already a member of the given organization.
     *
     * @param organizationId the primary key of the organization
     * @param userId         the primary key of the user
     * @return {@code true} if the user is a member, otherwise {@code false}
     */
    boolean existsByOrganizationIdAndUserId(Long organizationId, Long userId);

    /**
     * Counts the total number of members in the specified organization.
     *
     * @param organizationId the primary key of the organization
     * @return the number of members belonging to the given organization
     */
    long countByOrganizationId(Long organizationId);

    /**
     * Deletes all membership records for the specified organization using a native SQL DELETE.
     * Intended for use during organization hard-delete flows where cascading is handled manually.
     *
     * @param organizationId the primary key of the organization whose members should be removed
     */
    @Modifying
    @Query(value = "DELETE FROM organization_members WHERE organization_id = :organizationId", nativeQuery = true)
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);
}
