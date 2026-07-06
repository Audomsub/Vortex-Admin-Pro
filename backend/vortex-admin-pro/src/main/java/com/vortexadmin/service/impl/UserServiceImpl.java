package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.BulkActionRequest;
import com.vortexadmin.dto.request.ChangePasswordRequest;
import com.vortexadmin.dto.request.UserCreateRequest;
import com.vortexadmin.dto.request.UserUpdateRequest;
import com.vortexadmin.dto.request.UpdateMyProfileRequest;
import com.vortexadmin.dto.response.UserActivityResponse;
import com.vortexadmin.dto.response.UserProfileResponse;
import com.vortexadmin.entity.AuditLog;
import com.vortexadmin.entity.PasswordHistory;
import com.vortexadmin.entity.Role;
import com.vortexadmin.entity.User;
import com.vortexadmin.entity.UserSession;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.AuditLogRepository;
import com.vortexadmin.repository.PasswordHistoryRepository;
import com.vortexadmin.repository.RoleRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.repository.UserSessionRepository;
import com.vortexadmin.service.UserService;
import com.vortexadmin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.vortexadmin.service.PasswordPolicyService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles user management business logic including profile management, password changes,
 * avatar updates, admin CRUD operations, CSV bulk import, activity reporting,
 * geo-statistics, and bulk status/role actions with privilege escalation guards.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final AuditLogRepository auditLogRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;

    /**
     * Maps a {@link User} entity to a {@link UserProfileResponse} DTO, exposing only
     * safe, non-sensitive fields.
     *
     * @param user the user entity to map
     * @return the corresponding profile response DTO
     */
    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }

    /**
     * Returns the profile of the currently authenticated user.
     *
     * @return the profile DTO for the current user
     * @throws ApiException with {@code 404} if the user record no longer exists
     */
    @Override
    public UserProfileResponse getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return mapToResponse(user);
    }

    /**
     * Updates the profile fields (first name, last name, email) of the currently
     * authenticated user. Validates that the requested email is not already taken by
     * another account before applying the change.
     *
     * @param request the updated profile fields
     * @throws ApiException with {@code 404} if the user is not found, or {@code 400}
     *                      if the requested email is already in use by another account
     */
    @Override
    @Transactional
    public void updateMyProfile(UpdateMyProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        userRepository.save(user);
    }

    /**
     * Changes the password of the currently authenticated user after verifying the old
     * password, enforcing the configured password policy, and checking the new password
     * against the last 5 historical passwords. The old password hash is archived in the
     * password history before the new hash is saved.
     *
     * @param request the change-password request containing old and new passwords
     * @throws ApiException with {@code 400} if the old password is incorrect, the new
     *                      password violates the policy, or it matches a recent password
     */
    @Override
    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Incorrect old password");
        }

        passwordPolicyService.validate(request.getNewPassword());

        List<PasswordHistory> recent = passwordHistoryRepository.findTop5ByUserOrderByChangedAtDesc(user);
        for (PasswordHistory h : recent) {
            if (passwordEncoder.matches(request.getNewPassword(), h.getPasswordHash())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "New password must not match any of your last 5 passwords");
            }
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "New password must not match your current password");
        }

        passwordHistoryRepository.save(PasswordHistory.builder()
                .user(user)
                .passwordHash(user.getPassword())
                .build());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Updates the avatar URL of the currently authenticated user.
     *
     * @param avatarUrl the new avatar URL to persist
     * @throws ApiException with {@code 404} if the user record does not exist
     */
    @Override
    @Transactional
    public void updateMyAvatar(String avatarUrl) {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    /**
     * Returns the profile DTOs for all non-deleted users in the system.
     *
     * @return a list of profile responses for all active (non-soft-deleted) users
     */
    @Override
    public List<UserProfileResponse> getAllUsersInMyCompany() {
        return userRepository.findByDeletedAtIsNull()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Returns the profile of a specific non-deleted user by their id.
     *
     * @param id the id of the user to retrieve
     * @return the profile response DTO for the requested user
     * @throws ApiException with {@code 404} if no non-deleted user with that id exists
     */
    @Override
    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return mapToResponse(user);
    }

    /**
     * Creates a new user account with the specified role, encoded password, and
     * Active status. Validates that the username and email are not already taken.
     *
     * @param request the creation request containing username, email, password, name, and optional role id
     * @throws ApiException with {@code 400} if the username or email already exists,
     *                      or {@code 404} if the specified role id does not exist
     */
    @Override
    @Transactional
    public void createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername()) || userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username or Email already exists");
        }

        Role role = null;
        if (request.getRoleId() != null) {
            role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .status("Active")
                .role(role)
                .failedLoginAttempts(0)
                .build();

        userRepository.save(user);
    }

    /**
     * Updates mutable fields (first name, last name, status) of an existing user.
     * Role assignment is treated as a privilege escalation vector: if the role is being
     * changed, the caller must additionally hold the {@code role.update} authority.
     *
     * @param id      the id of the user to update
     * @param request the update payload containing name fields, optional status, and optional role id
     * @throws ApiException with {@code 404} if the user or specified role is not found,
     *                      or {@code 403} if the caller lacks {@code role.update} when changing the role
     */
    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
            boolean roleChanged = user.getRole() == null || !role.getId().equals(user.getRole().getId());
            // Role assignment is a privilege escalation vector — user.update alone is not enough
            if (roleChanged) {
                SecurityUtils.requireAuthority("role.update", "You do not have permission to change user roles");
                user.setRole(role);
            }
        }

        userRepository.save(user);
    }

    /**
     * Soft-deletes a user by setting their {@code deletedAt} timestamp to the current time.
     * The user record is retained in the database but excluded from normal queries.
     *
     * @param id the id of the user to soft-delete
     * @throws ApiException with {@code 404} if the user does not exist or is already deleted
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Imports users from an uploaded CSV file (RFC-4180 compliant, quoted fields supported).
     * The first row is treated as a header and skipped. Each subsequent row must contain at
     * least four columns in the order: Username, Email, FirstName, LastName. Rows with blank
     * username or email, or with an invalid email format, are silently skipped. Users whose
     * username or email already exist are also skipped. Newly created users receive a
     * cryptographically random password and the default USER role.
     *
     * @param file the uploaded CSV file
     * @return the number of users successfully imported
     * @throws ApiException with {@code 400} if the file is not a CSV or cannot be parsed
     */
    @Override
    @Transactional
    public int importUsersFromCsv(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean looksLikeCsv = (contentType != null && (contentType.startsWith("text/csv") || contentType.equals("text/plain") || contentType.equals("application/vnd.ms-excel")))
                || originalFilename.endsWith(".csv");
        if (!looksLikeCsv) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only CSV files are accepted");
        }

        Role defaultRole = roleRepository.findByName("USER").orElse(null);

        int importedCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                // RFC-4180 compliant CSV split: handle quoted fields containing commas
                String[] data = parseCsvLine(line);
                if (data.length >= 4) { // Required columns: Username, Email, FirstName, LastName (minimum 4 fields)
                    String username = data[0].trim();
                    String email = data[1].trim();
                    String firstName = data[2].trim();
                    String lastName = data[3].trim();

                    if (username.isEmpty() || email.isEmpty()) continue;
                    if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) continue;

                    if (!userRepository.existsByUsername(username) && !userRepository.existsByEmail(email)) {
                        User user = User.builder()
                                .username(username)
                                .email(email)
                                .password(passwordEncoder.encode(generateSecurePassword()))
                                .firstName(firstName)
                                .lastName(lastName)
                                .status("Active")
                                .role(defaultRole)
                                .failedLoginAttempts(0)
                                .build();
                        userRepository.save(user);
                        importedCount++;
                    }
                }
            }
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Failed to parse CSV file: " + e.getMessage());
        }
        return importedCount;
    }

    /**
     * Parses a single CSV line into an array of field strings, correctly handling
     * RFC-4180 quoted fields that may contain embedded commas and escaped double-quotes
     * ({@code ""} inside a quoted field represents a literal {@code "}).
     *
     * @param line the raw CSV line to parse
     * @return an array of field values with quotes stripped
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Returns the audit log timeline and session history for a specific user,
     * providing administrators with a full activity report.
     * The timeline contains up to the 100 most recent audit log entries for the user,
     * and the sessions list contains all recorded login sessions ordered by most recent first.
     *
     * @param userId the id of the user whose activity is requested
     * @return a {@link UserActivityResponse} containing the audit timeline and session list
     * @throws ApiException with {@code 404} if the user does not exist
     */
    @Override
    public UserActivityResponse getUserActivity(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserActivityResponse.ActivityItem> timeline = auditLogRepository
                .findTop100ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(log -> UserActivityResponse.ActivityItem.builder()
                        .id(log.getId())
                        .action(log.getAction())
                        .entityType(log.getEntityType())
                        .details(log.getDetails())
                        .ipAddress(log.getIpAddress())
                        .createdAt(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<UserActivityResponse.SessionItem> sessions = userSessionRepository
                .findByUserOrderByLoginAtDesc(user)
                .stream()
                .map(s -> UserActivityResponse.SessionItem.builder()
                        .id(s.getId())
                        .ipAddress(s.getIpAddress())
                        .country(s.getCountry())
                        .countryCode(s.getCountryCode())
                        .userAgent(s.getUserAgent())
                        .loginAt(s.getLoginAt())
                        .logoutAt(s.getLogoutAt())
                        .build())
                .collect(Collectors.toList());

        return UserActivityResponse.builder()
                .timeline(timeline)
                .sessions(sessions)
                .build();
    }

    /**
     * Returns login session counts grouped by country code, useful for rendering a
     * geographic distribution map in the dashboard.
     *
     * @return a map of country name to login session count, preserving insertion order
     */
    @Override
    public Map<String, Long> getGeoStats() {
        List<Object[]> rows = userSessionRepository.countByCountry();
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }

    /**
     * Applies a bulk action to a list of users identified by their ids.
     * Supported actions: SUSPEND (sets status to Suspended), ACTIVATE (sets status to Active),
     * DELETE (soft-deletes, requires {@code user.delete} authority, skips the caller),
     * and CHANGE_ROLE (updates the role, requires {@code role.update} authority, skips the caller).
     * DELETE and CHANGE_ROLE are escalation-guarded even though the controller only requires
     * {@code user.update}, so the stronger authority is enforced at the service layer.
     *
     * @param request the bulk action request containing the target user ids, action name, and optional role id
     * @throws ApiException with {@code 400} if no users are found, the action is unknown, or
     *                      {@code roleId} is missing for CHANGE_ROLE; with {@code 403} if the
     *                      caller lacks the required authority for DELETE or CHANGE_ROLE
     */
    @Override
    @Transactional
    public void bulkAction(BulkActionRequest request) {
        List<User> users = userRepository.findAllById(request.getUserIds());
        if (users.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No users found for the given IDs");
        }
        Long selfId = SecurityUtils.getCurrentUserId();
        switch (request.getAction().toUpperCase()) {
            case "SUSPEND" -> users.forEach(u -> u.setStatus("Suspended"));
            case "ACTIVATE" -> users.forEach(u -> u.setStatus("Active"));
            // DELETE and CHANGE_ROLE are escalation vectors — the controller only requires
            // user.update, so the stronger authority must be enforced here
            case "DELETE" -> {
                SecurityUtils.requireAuthority("user.delete", "You do not have permission to delete users");
                users.forEach(u -> {
                    if (!u.getId().equals(selfId)) {
                        u.setDeletedAt(LocalDateTime.now());
                    }
                });
            }
            case "CHANGE_ROLE" -> {
                SecurityUtils.requireAuthority("role.update", "You do not have permission to change user roles");
                if (request.getRoleId() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "roleId is required for CHANGE_ROLE action");
                }
                Role role = roleRepository.findById(request.getRoleId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
                users.forEach(u -> {
                    if (!u.getId().equals(selfId)) {
                        u.setRole(role);
                    }
                });
            }
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown action: " + request.getAction());
        }
        userRepository.saveAll(users);
    }

    /**
     * Searches for non-deleted users whose username, email, or name fields contain the
     * given keyword (case-insensitive). Returns all non-deleted users if the query is
     * null or blank.
     *
     * @param q the search keyword; may be {@code null} or blank to return all users
     * @return a list of matching user profile DTOs
     */
    @Override
    public List<UserProfileResponse> searchUsers(String q) {
        if (q == null || q.isBlank()) return userRepository.findByDeletedAtIsNull()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
        return userRepository.searchByKeyword(q.trim())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Generates a cryptographically secure random password encoded as a URL-safe Base64
     * string. Used when creating accounts via CSV import where no password is provided.
     *
     * @return a 24-character URL-safe Base64-encoded random password string
     */
    private String generateSecurePassword() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
