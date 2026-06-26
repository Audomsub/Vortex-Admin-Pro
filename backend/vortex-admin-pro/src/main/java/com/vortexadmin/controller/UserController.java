package com.vortexadmin.controller;

import com.vortexadmin.dto.request.BulkActionRequest;
import com.vortexadmin.dto.request.ChangePasswordRequest;
import com.vortexadmin.dto.request.UserCreateRequest;
import com.vortexadmin.dto.request.UserUpdateRequest;
import com.vortexadmin.dto.request.UpdateMyProfileRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.UserActivityResponse;
import com.vortexadmin.dto.response.UserProfileResponse;
import com.vortexadmin.service.UserService;
import com.vortexadmin.service.ExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ExportService exportService;

    // --- Profile Operations ---
    
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('profile.view')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", userService.getMyProfile()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('profile.update')")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(@Valid @RequestBody UpdateMyProfileRequest request) {
        userService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", null));
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("hasAuthority('password.change')")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changeMyPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", null));
    }

    // --- Tenant Scoped CRUD (Requires Admin role theoretically) ---
    // PreAuthorize checks can be added later when we refine roles.

    @GetMapping
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", userService.getAllUsersInMyCompany()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User fetched", userService.getUserById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user.create')")
    public ResponseEntity<ApiResponse<Void>> createUser(@Valid @RequestBody UserCreateRequest request) {
        userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity<ApiResponse<Void>> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }



    @GetMapping("/export")
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<byte[]> exportUsers(@RequestParam(defaultValue = "excel") String format) throws Exception {
        List<UserProfileResponse> users = userService.getAllUsersInMyCompany();
        
        List<Map<String, Object>> data = new ArrayList<>();
        for (UserProfileResponse user : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            map.put("first name", user.getFirstName());
            map.put("last name", user.getLastName());
            map.put("role", user.getRoleName());
            map.put("status", user.getStatus());
            data.add(map);
        }
        
        List<String> headers = Arrays.asList("ID", "Username", "Email", "First Name", "Last Name", "Role", "Status");
        
        byte[] bytes;
        String contentType;
        String filename;
        
        if ("excel".equalsIgnoreCase(format)) {
            bytes = exportService.exportToExcel(data, headers);
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            filename = "users.xlsx";
        } else {
            bytes = exportService.exportToCsv(data, headers);
            contentType = "text/csv";
            filename = "users.csv";
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('user.create')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> importUsers(@RequestParam("file") MultipartFile file) throws Exception {
        int count = userService.importUsersFromCsv(file);
        Map<String, Integer> result = new HashMap<>();
        result.put("importedCount", count);
        return ResponseEntity.ok(ApiResponse.success("Users imported successfully", result));
    }

    @GetMapping("/{id}/activity")
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<ApiResponse<UserActivityResponse>> getUserActivity(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Activity fetched", userService.getUserActivity(id)));
    }

    @GetMapping("/geo-stats")
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getGeoStats() {
        return ResponseEntity.ok(ApiResponse.success("Geo stats fetched", userService.getGeoStats()));
    }

    @PostMapping("/bulk-action")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity<ApiResponse<Void>> bulkAction(@Valid @RequestBody BulkActionRequest request) {
        userService.bulkAction(request);
        return ResponseEntity.ok(ApiResponse.success("Bulk action applied successfully", null));
    }
}
