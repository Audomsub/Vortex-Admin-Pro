package com.vortexadmin.service;

import com.vortexadmin.dto.request.RoleRequest;
import com.vortexadmin.dto.response.PermissionResponse;
import com.vortexadmin.dto.response.RoleResponse;

import java.util.List;

/**
 * Service contract for role and permission management operations within the RBAC system,
 * including tenant-scoped role CRUD and the retrieval of all available permissions.
 */
public interface RoleService {

    /**
     * Returns all roles defined within the same company/tenant as the calling user.
     *
     * @return a list of role responses for the caller's tenant
     */
    List<RoleResponse> getAllRolesInMyCompany();

    /**
     * Returns a single role by its primary key.
     *
     * @param id the primary key of the role to retrieve
     * @return the matching role response
     * @throws com.vortexadmin.exception.ApiException if no role with the given ID exists
     */
    RoleResponse getRoleById(Long id);

    /**
     * Creates a new role with the specified name and permission assignments.
     *
     * @param request the role creation payload including the name and permission IDs
     * @throws com.vortexadmin.exception.ApiException if a role with the same name already exists
     */
    void createRole(RoleRequest request);

    /**
     * Updates an existing role's name and permission assignments.
     *
     * @param id      the primary key of the role to update
     * @param request the updated role data
     * @throws com.vortexadmin.exception.ApiException if the role is not found
     */
    void updateRole(Long id, RoleRequest request);

    /**
     * Deletes the specified role.
     *
     * @param id the primary key of the role to delete
     * @throws com.vortexadmin.exception.ApiException if the role is not found
     */
    void deleteRole(Long id);

    /**
     * Returns all permission definitions available in the system.
     * Used when assigning permissions to roles in the admin UI.
     *
     * @return a list of all defined permissions
     */
    List<PermissionResponse> getAllPermissions();
}
