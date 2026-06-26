package com.vortexadmin.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.vortexadmin.dto.request.ChangePasswordRequest;
import com.vortexadmin.dto.request.UpdateMyProfileRequest;
import com.vortexadmin.dto.request.UserCreateRequest;
import com.vortexadmin.dto.request.UserUpdateRequest;
import com.vortexadmin.dto.response.UserActivityResponse;
import com.vortexadmin.dto.response.UserProfileResponse;

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
    void deleteUser(Long id);
    int importUsersFromCsv(MultipartFile file);
    UserActivityResponse getUserActivity(Long userId);
    java.util.Map<String, Long> getGeoStats();
    void bulkAction(com.vortexadmin.dto.request.BulkActionRequest request);
}
