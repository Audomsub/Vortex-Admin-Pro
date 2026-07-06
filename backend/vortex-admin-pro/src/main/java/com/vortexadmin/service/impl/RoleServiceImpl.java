package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.RoleRequest;
import com.vortexadmin.dto.response.RoleResponse;
import com.vortexadmin.entity.Permission;
import com.vortexadmin.entity.Role;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.PermissionRepository;
import com.vortexadmin.repository.RoleRepository;
import com.vortexadmin.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Handles role and permission management business logic, including cached retrieval
 * of roles and permissions, role creation with permission assignment, role updates,
 * and role deletion with cache eviction after every mutation.
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Maps a {@link Role} entity to a {@link RoleResponse} DTO, including the list of
     * permission codes attached to the role.
     *
     * @param role the role entity to map
     * @return the corresponding role response DTO
     */
    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions() != null
                        ? role.getPermissions().stream().map(Permission::getCode).collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }

    /**
     * Returns all roles with their associated permissions, fetched via a join query to
     * avoid N+1 selects. Results are served from the {@code roles} cache when available.
     *
     * @return a list of all role response DTOs
     */
    @Cacheable("roles")
    @Override
    public List<RoleResponse> getAllRolesInMyCompany() {
        return roleRepository.findAllWithPermissions().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns the role identified by the given id, including its permissions.
     *
     * @param id the id of the role to retrieve
     * @return the role response DTO for the requested role
     * @throws ApiException with {@code 404} if no role with that id exists
     */
    @Override
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
        return mapToResponse(role);
    }

    /**
     * Creates a new role with the given name, description, and optional set of permissions.
     * Evicts all entries from the {@code roles} cache after the role is persisted.
     *
     * @param request the creation request containing the role name, description, and optional permission ids
     */
    @CacheEvict(value = "roles", allEntries = true)
    @Override
    @Transactional
    public void createRole(RoleRequest request) {
        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionIds() != null) {
            permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(permissions)
                .build();
        roleRepository.save(role);
    }

    /**
     * Updates the name, description, and permission set of an existing role.
     * Evicts all entries from the {@code roles} cache after the role is updated.
     *
     * @param id      the id of the role to update
     * @param request the update payload containing the new name, description, and optional permission ids
     * @throws ApiException with {@code 404} if no role with that id exists
     */
    @CacheEvict(value = "roles", allEntries = true)
    @Override
    @Transactional
    public void updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(permissions);
        }

        roleRepository.save(role);
    }

    /**
     * Permanently deletes the role identified by the given id.
     * Evicts all entries from the {@code roles} cache after deletion.
     *
     * @param id the id of the role to delete
     * @throws ApiException with {@code 404} if no role with that id exists
     */
    @CacheEvict(value = "roles", allEntries = true)
    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
        roleRepository.delete(role);
    }

    /**
     * Returns all available permissions in the system. Results are served from the
     * {@code permissions} cache when available.
     *
     * @return a list of all permission response DTOs containing id, code, and name
     */
    @Cacheable("permissions")
    @Override
    public List<com.vortexadmin.dto.response.PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> com.vortexadmin.dto.response.PermissionResponse.builder()
                        .id(p.getId())
                        .code(p.getCode())
                        .name(p.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
