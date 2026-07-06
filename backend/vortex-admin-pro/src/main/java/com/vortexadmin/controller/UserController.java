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

/**
 * Handles HTTP requests for user management operations, including profile access,
 * CRUD actions, bulk operations, and data import/export, delegating business logic to UserService.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ExportService exportService;

    // --- Profile Operations ---

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @return the {@link UserProfileResponse} for the logged-in user
     */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('profile.view')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", userService.getMyProfile()));
    }

    /**
     * Updates the profile information of the currently authenticated user.
     *
     * @param request the profile update payload containing fields to change
     * @return a success response with no data payload upon successful update
     */
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('profile.update')")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(@Valid @RequestBody UpdateMyProfileRequest request) {
        userService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", null));
    }

    /**
     * Changes the password of the currently authenticated user.
     *
     * @param request the change-password payload containing the current and new passwords
     * @return a success response with no data payload upon successful password change
     */
    @PostMapping("/me/change-password")
    @PreAuthorize("hasAuthority('password.change')")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changeMyPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", null));
    }

    // --- Tenant Scoped CRUD (Requires Admin role theoretically) ---
    // PreAuthorize checks can be added later when we refine roles.

    /**
     * Retrieves all users belonging to the authenticated user's company/tenant.
     *
     * @return a list of {@link UserProfileResponse} for all users in the tenant
     */
    @GetMapping
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", userService.getAllUsersInMyCompany()));
    }

    /**
     * Searches for users by a query string within the authenticated user's tenant.
     *
     * @param q the optional search query string matched against usernames, emails, or names
     * @return a list of matching {@link UserProfileResponse} objects
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> searchUsers(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", userService.searchUsers(q)));
    }

    /**
     * Retrieves a single user by their unique identifier.
     *
     * @param id the unique ID of the user to retrieve
     * @return the {@link UserProfileResponse} for the specified user
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User fetched", userService.getUserById(id)));
    }

    /**
     * Creates a new user within the authenticated user's tenant.
     *
     * @param request the user creation payload containing username, email, role, etc.
     * @return a success response with no data payload upon successful creation
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user.create')")
    public ResponseEntity<ApiResponse<Void>> createUser(@Valid @RequestBody UserCreateRequest request) {
        userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", null));
    }

    /**
     * Updates an existing user's information by their unique identifier.
     *
     * @param id      the unique ID of the user to update
     * @param request the update payload containing fields to change
     * @return a success response with no data payload upon successful update
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity<ApiResponse<Void>> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", null));
    }

    /**
     * Deletes a user by their unique identifier.
     *
     * @param id the unique ID of the user to delete
     * @return a success response with no data payload upon successful deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    /**
     * Exports all users in the tenant as a downloadable file in the specified format.
     *
     * @param format the output format, either {@code "excel"} (default) or {@code "csv"}
     * @return a byte array response with appropriate content-type and content-disposition headers
     * @throws Exception if file generation fails
     */
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

    /**
     * Imports users from an uploaded CSV file into the tenant.
     *
     * @param file the multipart CSV file containing user records to import
     * @return a map with {@code importedCount} indicating how many users were successfully imported
     * @throws Exception if parsing or importing the file fails
     */
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('user.create')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> importUsers(@RequestParam("file") MultipartFile file) throws Exception {
        int count = userService.importUsersFromCsv(file);
        Map<String, Integer> result = new HashMap<>();
        result.put("importedCount", count);
        return ResponseEntity.ok(ApiResponse.success("Users imported successfully", result));
    }

    /**
     * Retrieves the recent activity log for a specific user.
     *
     * @param id the unique ID of the user whose activity is being requested
     * @return a {@link UserActivityResponse} containing recent actions performed by the user
     */
    @GetMapping("/{id}/activity")
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<ApiResponse<UserActivityResponse>> getUserActivity(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Activity fetched", userService.getUserActivity(id)));
    }

    /**
     * Retrieves aggregated geographic statistics for users in the tenant.
     *
     * @return a map of country/region names to user counts
     */
    @GetMapping("/geo-stats")
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getGeoStats() {
        return ResponseEntity.ok(ApiResponse.success("Geo stats fetched", userService.getGeoStats()));
    }

    /**
     * Applies a bulk action (e.g., activate, deactivate, delete) to multiple users at once.
     *
     * @param request the bulk action payload containing the action type and list of target user IDs
     * @return a success response with no data payload upon successful execution
     */
    @PostMapping("/bulk-action")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity<ApiResponse<Void>> bulkAction(@Valid @RequestBody BulkActionRequest request) {
        userService.bulkAction(request);
        return ResponseEntity.ok(ApiResponse.success("Bulk action applied successfully", null));
    }
}
