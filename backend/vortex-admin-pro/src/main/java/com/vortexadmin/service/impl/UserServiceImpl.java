package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.BulkActionRequest;
import com.vortexadmin.dto.request.ChangePasswordRequest;
import com.vortexadmin.dto.request.UserCreateRequest;
import com.vortexadmin.dto.request.UserUpdateRequest;
import com.vortexadmin.dto.request.UpdateMyProfileRequest;
import com.vortexadmin.dto.response.UserActivityResponse;
import com.vortexadmin.dto.response.UserProfileResponse;
import com.vortexadmin.entity.AuditLog;
import com.vortexadmin.entity.Role;
import com.vortexadmin.entity.User;
import com.vortexadmin.entity.UserSession;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.AuditLogRepository;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.vortexadmin.service.PasswordPolicyService passwordPolicyService;
    private final AuditLogRepository auditLogRepository;
    private final UserSessionRepository userSessionRepository;

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

    @Override
    public UserProfileResponse getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return mapToResponse(user);
    }
    @Override
    @Transactional
    public void updateMyProfile(UpdateMyProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Optional: Check if email is already taken by another user
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        userRepository.save(user);
    }
    @Override
    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Incorrect old password");
        }

        passwordPolicyService.validate(request.getNewPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateMyAvatar(String avatarUrl) {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    @Override
    public List<UserProfileResponse> getAllUsersInMyCompany() {
        return userRepository.findByDeletedAtIsNull()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return mapToResponse(user);
    }

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
            user.setRole(role);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        // Soft Delete
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public int importUsersFromCsv(MultipartFile file) {
        int importedCount = 0;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // skip header
                }
                String[] data = line.split(",");
                if (data.length >= 3) { // Expecting: Username, Email, FirstName, LastName
                    String username = data[0].trim();
                    String email = data[1].trim();
                    String firstName = data[2].trim();
                    String lastName = data.length > 3 ? data[3].trim() : "";

                    if (!userRepository.existsByUsername(username) && !userRepository.existsByEmail(email)) {
                        User user = User.builder()
                                .username(username)
                                .email(email)
                                .password(passwordEncoder.encode(generateSecurePassword()))
                                .firstName(firstName)
                                .lastName(lastName)
                                .status("Active")
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

    @Override
    public UserActivityResponse getUserActivity(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserActivityResponse.ActivityItem> timeline = auditLogRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
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

    @Override
    public Map<String, Long> getGeoStats() {
        List<Object[]> rows = userSessionRepository.countByCountry();
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }

    @Override
    @Transactional
    public void bulkAction(BulkActionRequest request) {
        List<User> users = userRepository.findAllById(request.getUserIds());
        if (users.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No users found for the given IDs");
        }
        switch (request.getAction().toUpperCase()) {
            case "SUSPEND" -> users.forEach(u -> u.setStatus("Suspended"));
            case "ACTIVATE" -> users.forEach(u -> u.setStatus("Active"));
            case "DELETE" -> users.forEach(u -> u.setDeletedAt(LocalDateTime.now()));
            case "CHANGE_ROLE" -> {
                if (request.getRoleId() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "roleId is required for CHANGE_ROLE action");
                }
                Role role = roleRepository.findById(request.getRoleId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
                users.forEach(u -> u.setRole(role));
            }
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown action: " + request.getAction());
        }
        userRepository.saveAll(users);
    }

    private String generateSecurePassword() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
