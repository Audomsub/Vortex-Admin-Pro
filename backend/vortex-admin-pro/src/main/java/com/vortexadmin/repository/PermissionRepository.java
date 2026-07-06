package com.vortexadmin.repository;

import com.vortexadmin.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Permission} entities, providing standard CRUD operations
 * and a lookup method for finding permissions by their unique code string.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Finds a permission by its unique permission code (e.g., "user.read", "role.delete").
     *
     * @param code the permission code to search for
     * @return an {@link Optional} containing the matching permission, or empty if not found
     */
    Optional<Permission> findByCode(String code);
}
