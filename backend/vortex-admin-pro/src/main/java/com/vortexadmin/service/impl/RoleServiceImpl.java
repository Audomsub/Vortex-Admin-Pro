package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.RoleRequest;
import com.vortexadmin.dto.response.RoleResponse;
import com.vortexadmin.entity.Permission;
import com.vortexadmin.entity.Role;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.PermissionRepository;
import com.vortexadmin.repository.RoleRepository;
import com.vortexadmin.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream().map(Permission::getCode).collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<RoleResponse> getAllRolesInMyCompany() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
        return mapToResponse(role);
    }

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

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
        roleRepository.delete(role);
    }

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
