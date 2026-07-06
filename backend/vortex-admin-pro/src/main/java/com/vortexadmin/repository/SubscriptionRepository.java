package com.vortexadmin.repository;

import com.vortexadmin.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Subscription} entities, providing standard CRUD
 * operations and custom methods for organization-scoped subscription lookup, status aggregation,
 * and bulk deletion.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Finds the most recent subscription for the specified organization that matches the given
     * status, ordered by creation date descending.  Used to retrieve the currently active
     * subscription for billing and plan-management operations.
     *
     * @param organizationId the primary key of the organization
     * @param status         the subscription status to match (e.g., "ACTIVE", "CANCELLED")
     * @return an {@link Optional} containing the matching subscription if one exists,
     *         or empty if no subscription with that status is found for the organization
     */
    Optional<Subscription> findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(Long organizationId, String status);

    /**
     * Returns all subscriptions associated with the specified organization.
     *
     * @param organizationId the primary key of the organization
     * @return a list of all subscriptions for the given organization
     */
    List<Subscription> findByOrganizationId(Long organizationId);

    /**
     * Counts subscriptions that have the specified status across all organizations.
     * Used in billing dashboard analytics (e.g., counting all "ACTIVE" subscriptions).
     *
     * @param status the subscription status to count (e.g., "ACTIVE", "TRIAL")
     * @return the total number of subscriptions with the given status
     */
    long countByStatus(String status);

    /**
     * Deletes all subscription records for the specified organization using a native SQL DELETE.
     * Intended for use during organization hard-delete flows where cascading is handled manually.
     *
     * @param organizationId the primary key of the organization whose subscriptions should be removed
     */
    @Modifying
    @Query(value = "DELETE FROM subscriptions WHERE organization_id = :organizationId", nativeQuery = true)
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);
}
