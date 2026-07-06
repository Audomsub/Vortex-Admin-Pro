package com.vortexadmin.controller;

import com.vortexadmin.dto.request.RoleRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.RoleResponse;
import com.vortexadmin.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests for role and permission management, delegating
 * all business logic to RoleService.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Retrieves all roles defined within the authenticated user's company/tenant.
     *
     * @return a list of {@link RoleResponse} objects representing all tenant roles
     */
    @GetMapping
    @PreAuthorize("hasAuthority('role.read')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.success("Roles fetched", roleService.getAllRolesInMyCompany()));
    }

    /**
     * Retrieves all available permissions in the system.
     *
     * @return a list of {@link com.vortexadmin.dto.response.PermissionResponse} objects
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('role.read')")
    public ResponseEntity<ApiResponse<List<com.vortexadmin.dto.response.PermissionResponse>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.success("Permissions fetched", roleService.getAllPermissions()));
    }

    /**
     * Retrieves a single role by its unique identifier.
     *
     * @param id the unique ID of the role to retrieve
     * @return the {@link RoleResponse} for the specified role
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role.read')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Role fetched", roleService.getRoleById(id)));
    }

    /**
     * Creates a new role with the specified name and permissions.
     *
     * @param request the role creation payload containing the role name and permission IDs
     * @return a success response with no data payload upon successful creation
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role.create')")
    public ResponseEntity<ApiResponse<Void>> createRole(@Valid @RequestBody RoleRequest request) {
        roleService.createRole(request);
        return ResponseEntity.ok(ApiResponse.success("Role created successfully", null));
    }

    /**
     * Updates an existing role's name and/or permissions by its unique identifier.
     *
     * @param id      the unique ID of the role to update
     * @param request the update payload containing the new role name and permission IDs
     * @return a success response with no data payload upon successful update
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role.update')")
    public ResponseEntity<ApiResponse<Void>> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", null));
    }

    /**
     * Deletes a role by its unique identifier.
     *
     * @param id the unique ID of the role to delete
     * @return a success response with no data payload upon successful deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }
}
