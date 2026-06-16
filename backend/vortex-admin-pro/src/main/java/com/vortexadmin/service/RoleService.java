package com.vortexadmin.service;

import com.vortexadmin.dto.request.RoleRequest;
import com.vortexadmin.dto.response.PermissionResponse;
import com.vortexadmin.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> getAllRolesInMyCompany();
    RoleResponse getRoleById(Long id);
    void createRole(RoleRequest request);
    void updateRole(Long id, RoleRequest request);
    void deleteRole(Long id);
    List<PermissionResponse> getAllPermissions();
}
