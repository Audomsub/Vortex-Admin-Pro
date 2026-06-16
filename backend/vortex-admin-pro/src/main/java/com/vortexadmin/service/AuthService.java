package com.vortexadmin.service;

import com.vortexadmin.dto.request.LoginRequest;
import com.vortexadmin.dto.request.RegisterRequest;
import com.vortexadmin.dto.request.TokenRefreshRequest;
import com.vortexadmin.dto.response.JwtResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest request);
    JwtResponse authenticateWithGoogle(String idToken, HttpServletRequest request);
    void registerUser(RegisterRequest registerRequest);
    JwtResponse refreshToken(TokenRefreshRequest request);
    void logout(String tokenIdentifier);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
