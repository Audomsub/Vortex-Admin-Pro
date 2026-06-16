package com.vortexadmin.service;

import com.vortexadmin.dto.request.ChangePasswordRequest;
import com.vortexadmin.dto.request.UserCreateRequest;
import com.vortexadmin.dto.request.UserUpdateRequest;
import com.vortexadmin.dto.response.UserProfileResponse;
import com.vortexadmin.dto.request.UpdateMyProfileRequest;

import java.util.List;

public interface UserService {
    UserProfileResponse getMyProfile();
    void updateMyProfile(UpdateMyProfileRequest request);
    void changeMyPassword(ChangePasswordRequest request);
    void updateMyAvatar(String avatarUrl);

    // Tenant Scoped CRUD
    List<UserProfileResponse> getAllUsersInMyCompany();
    UserProfileResponse getUserById(Long id);
    void createUser(UserCreateRequest request);
    void updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id); // Soft delete
}
