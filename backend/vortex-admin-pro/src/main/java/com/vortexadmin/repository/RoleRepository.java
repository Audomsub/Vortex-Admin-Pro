package com.vortexadmin.repository;

import com.vortexadmin.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Role} entities, providing standard CRUD operations
 * and a custom query to eagerly fetch roles together with their assigned permissions.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its unique name.
     *
     * @param name the role name to search for (e.g., "ADMIN", "USER")
     * @return an {@link Optional} containing the matching role, or empty if not found
     */
    Optional<Role> findByName(String name);

    /**
     * Retrieves all roles, eagerly fetching each role's collection of permissions via a
     * LEFT JOIN FETCH to avoid N+1 queries during permission checks.
     *
     * @return a list of all roles with their permissions fully loaded
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions")
    List<Role> findAllWithPermissions();
}
