package com.vortexadmin.repository;

import com.vortexadmin.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Organization} entities, providing standard CRUD operations
 * and slug-based lookup and existence-check methods.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    /**
     * Finds an organization by its unique URL-friendly slug.
     *
     * @param slug the slug to search for (e.g., "acme-corp")
     * @return an {@link Optional} containing the matching organization, or empty if not found
     */
    Optional<Organization> findBySlug(String slug);

    /**
     * Checks whether an organization with the given slug already exists.
     * Used during organization creation to enforce slug uniqueness.
     *
     * @param slug the slug to check for existence
     * @return {@code true} if an organization with that slug exists, otherwise {@code false}
     */
    boolean existsBySlug(String slug);
}
