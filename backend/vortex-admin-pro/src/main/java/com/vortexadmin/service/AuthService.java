package com.vortexadmin.service;

import com.vortexadmin.dto.request.LoginRequest;
import com.vortexadmin.dto.request.RegisterRequest;
import com.vortexadmin.dto.request.TokenRefreshRequest;
import com.vortexadmin.dto.response.JwtResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service contract for all authentication operations including login, registration,
 * token refresh, logout, and password reset flows.
 */
public interface AuthService {

    /**
     * Authenticates a user with username/password credentials and issues a JWT access token
     * and refresh token.  A new {@link com.vortexadmin.entity.UserSession} record is created
     * using metadata extracted from the incoming HTTP request (IP address, user-agent).
     *
     * @param loginRequest the login credentials (username and password)
     * @param request      the HTTP request used to capture session metadata such as IP address
     * @return a {@link JwtResponse} containing the access token, refresh token, and user details
     */
    JwtResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest request);

    /**
     * Authenticates a user via a Google OAuth2 ID token.  If the account does not yet exist,
     * a new user record is created automatically.  A session record is created from the HTTP
     * request metadata.
     *
     * @param idToken the Google-issued ID token from the client-side OAuth2 sign-in flow
     * @param request the HTTP request used to capture session metadata
     * @return a {@link JwtResponse} containing the access token, refresh token, and user details
     */
    JwtResponse authenticateWithGoogle(String idToken, HttpServletRequest request);

    /**
     * Registers a new user account after validating the request and checking for duplicate
     * username or email.  The password is encoded before persistence.
     *
     * @param registerRequest the registration details (username, email, password, etc.)
     * @throws com.vortexadmin.exception.ApiException if the username or email is already taken
     */
    void registerUser(RegisterRequest registerRequest);

    /**
     * Issues a new JWT access token in exchange for a valid, non-expired refresh token.
     *
     * @param request the request payload containing the refresh token string
     * @return a {@link JwtResponse} with the newly issued access token and the existing refresh token
     * @throws com.vortexadmin.exception.ApiException if the refresh token is invalid or expired
     */
    JwtResponse refreshToken(TokenRefreshRequest request);

    /**
     * Logs out a user by deleting the refresh token identified by the given token value or
     * username.  The associated session record is updated with a logout timestamp.
     *
     * @param tokenIdentifier the raw refresh token value or username used to locate the session
     */
    void logout(String tokenIdentifier);

    /**
     * Initiates the password-reset flow by generating a one-time token and sending a reset
     * email to the address associated with the given email if an account exists.
     *
     * @param email the email address of the account requesting a password reset
     */
    void forgotPassword(String email);

    /**
     * Completes the password-reset flow by validating the token and updating the user's
     * password to the new value (after encoding).  The token is invalidated upon success.
     *
     * @param token       the one-time password-reset token from the reset email link
     * @param newPassword the new plain-text password to set for the account
     * @throws com.vortexadmin.exception.ApiException if the token is invalid or expired
     */
    void resetPassword(String token, String newPassword);
}
