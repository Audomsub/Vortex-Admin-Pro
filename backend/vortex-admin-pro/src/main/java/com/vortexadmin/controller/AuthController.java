package com.vortexadmin.controller;

import com.vortexadmin.dto.request.ForgotPasswordRequest;
import com.vortexadmin.dto.request.LoginRequest;
import com.vortexadmin.dto.request.RegisterRequest;
import com.vortexadmin.dto.request.ResetPasswordRequest;
import com.vortexadmin.dto.request.TokenRefreshRequest;
import com.vortexadmin.dto.request.GoogleLoginRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.JwtResponse;
import com.vortexadmin.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles HTTP requests for authentication, including login, registration,
 * token refresh, logout, and password management, delegating all business logic to AuthService.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user with username/email and password credentials.
     *
     * @param loginRequest the login credentials (username/email and password)
     * @param request      the HTTP servlet request used to capture client IP for audit logging
     * @return a {@link JwtResponse} containing the access token and refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest, request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
    }

    /**
     * Authenticates a user via a Google OAuth2 ID token.
     *
     * @param request     the Google login request containing the ID token
     * @param httpRequest the HTTP servlet request used to capture client IP for audit logging
     * @return a {@link JwtResponse} containing the access token and refresh token
     */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateWithGoogle(@Valid @RequestBody GoogleLoginRequest request, HttpServletRequest httpRequest) {
        JwtResponse jwtResponse = authService.authenticateWithGoogle(request.getIdToken(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Google Login successful", jwtResponse));
    }

    /**
     * Registers a new user account in the system.
     *
     * @param signUpRequest the registration details (username, email, password, etc.)
     * @return a success response with no data payload upon successful registration
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", null));
    }

    /**
     * Issues a new access token using a valid refresh token.
     *
     * @param request the token refresh request containing the refresh token
     * @return a new {@link JwtResponse} with a fresh access token and refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        JwtResponse jwtResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", jwtResponse));
    }

    /**
     * Logs out the authenticated user by invalidating the provided refresh token.
     *
     * @param request the token refresh request whose refresh token will be revoked
     * @return a success response with no data payload upon successful logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    /**
     * Initiates the forgot-password flow by sending a reset link to the provided email address.
     *
     * @param request the forgot-password request containing the user's email address
     * @return a success response regardless of whether the email exists, to prevent user enumeration
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("If the email exists, a reset link has been sent", null));
    }

    /**
     * Resets a user's password using a valid password-reset token.
     *
     * @param request the reset-password request containing the reset token and the new password
     * @return a success response with no data payload upon successful password reset
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully", null));
    }
}
