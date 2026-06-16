package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.ChangePasswordRequest;
import com.vortexadmin.dto.request.UserCreateRequest;
import com.vortexadmin.dto.request.UserUpdateRequest;
import com.vortexadmin.dto.request.UpdateMyProfileRequest;
import com.vortexadmin.dto.response.UserProfileResponse;
import com.vortexadmin.entity.Role;
import com.vortexadmin.entity.User;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.RoleRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.UserService;
import com.vortexadmin.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
}
