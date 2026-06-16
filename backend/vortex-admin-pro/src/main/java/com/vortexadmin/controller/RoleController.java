package com.vortexadmin.controller;

import com.vortexadmin.dto.request.RoleRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.RoleResponse;
import com.vortexadmin.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('role.read')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.success("Roles fetched", roleService.getAllRolesInMyCompany()));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('role.read')")
    public ResponseEntity<ApiResponse<List<com.vortexadmin.dto.response.PermissionResponse>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.success("Permissions fetched", roleService.getAllPermissions()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role.read')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Role fetched", roleService.getRoleById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role.create')")
    public ResponseEntity<ApiResponse<Void>> createRole(@Valid @RequestBody RoleRequest request) {
        roleService.createRole(request);
        return ResponseEntity.ok(ApiResponse.success("Role created successfully", null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role.update')")
    public ResponseEntity<ApiResponse<Void>> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }
}
