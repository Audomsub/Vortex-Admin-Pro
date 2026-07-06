package com.vortexadmin.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.vortexadmin.dto.request.ChangePasswordRequest;
import com.vortexadmin.dto.request.UpdateMyProfileRequest;
import com.vortexadmin.dto.request.UserCreateRequest;
import com.vortexadmin.dto.request.UserUpdateRequest;
import com.vortexadmin.dto.response.UserActivityResponse;
import com.vortexadmin.dto.response.UserProfileResponse;

/**
 * Service contract for user management operations including self-service profile updates,
 * tenant-scoped CRUD, CSV bulk import, activity reporting, and geographic statistics.
 */
public interface UserService {

    /**
     * Returns the profile of the currently authenticated user.
     *
     * @return the profile of the calling user
     */
    UserProfileResponse getMyProfile();

    /**
     * Updates the profile fields (e.g., first name, last name, phone) of the currently
     * authenticated user.
     *
     * @param request the new profile data to apply
     */
    void updateMyProfile(UpdateMyProfileRequest request);

    /**
     * Changes the password of the currently authenticated user after verifying the current
     * password and enforcing the configured password policy.
     *
     * @param request contains the current password and the desired new password
     * @throws com.vortexadmin.exception.ApiException if the current password is wrong or
     *         the new password violates the configured policy
     */
    void changeMyPassword(ChangePasswordRequest request);

    /**
     * Sets the avatar URL for the currently authenticated user.
     *
     * @param avatarUrl the URL of the newly uploaded avatar image
     */
    void updateMyAvatar(String avatarUrl);

    /**
     * Returns all non-deleted users that belong to the same company/tenant as the calling user.
     *
     * @return a list of user profiles within the caller's tenant scope
     */
    List<UserProfileResponse> getAllUsersInMyCompany();

    /**
     * Returns the profile of a specific user by their primary key.
     *
     * @param id the primary key of the user to retrieve
     * @return the matching user's profile
     * @throws com.vortexadmin.exception.ApiException if no user with the given ID exists
     */
    UserProfileResponse getUserById(Long id);

    /**
     * Creates a new user account using the provided details, assigning the specified role.
     *
     * @param request the user creation payload including username, email, password, and role
     * @throws com.vortexadmin.exception.ApiException if the username or email is already taken
     */
    void createUser(UserCreateRequest request);

    /**
     * Updates an existing user's profile and role assignment.
     *
     * @param id      the primary key of the user to update
     * @param request the updated user data
     * @throws com.vortexadmin.exception.ApiException if the user is not found
     */
    void updateUser(Long id, UserUpdateRequest request);

    /**
     * Soft-deletes a user by setting their {@code deletedAt} timestamp.
     *
     * @param id the primary key of the user to delete
     * @throws com.vortexadmin.exception.ApiException if the user is not found
     */
    void deleteUser(Long id);

    /**
     * Parses a CSV file and imports the contained user records.  Rows with missing required
     * fields are skipped; the header row is always ignored.
     *
     * @param file the uploaded CSV file containing user data
     * @return the number of user records successfully imported
     * @throws com.vortexadmin.exception.ApiException if the file cannot be read or parsed
     */
    int importUsersFromCsv(MultipartFile file);

    /**
     * Returns an activity summary for the specified user, including audit log entries and
     * session history.
     *
     * @param userId the primary key of the user whose activity is requested
     * @return the activity summary for the given user
     */
    UserActivityResponse getUserActivity(Long userId);

    /**
     * Returns a map of country names to session counts, aggregated from all recorded user
     * sessions.  Used to render geographic login statistics on the dashboard.
     *
     * @return a map where each key is a country name and each value is the corresponding
     *         session count
     */
    java.util.Map<String, Long> getGeoStats();

    /**
     * Performs a bulk action (e.g., bulk delete or bulk status change) on the set of user IDs
     * specified in the request.
     *
     * @param request the bulk-action payload containing the action type and target user IDs
     */
    void bulkAction(com.vortexadmin.dto.request.BulkActionRequest request);

    /**
     * Performs a case-insensitive keyword search across user username, email, first name, and
     * last name fields, returning only non-deleted users.
     *
     * @param q the search keyword
     * @return a list of user profiles that match the search term
     */
    List<UserProfileResponse> searchUsers(String q);
}
