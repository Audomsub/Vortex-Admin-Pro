package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.LoginRequest;
import com.vortexadmin.dto.request.RegisterRequest;
import com.vortexadmin.dto.request.TokenRefreshRequest;
import com.vortexadmin.dto.response.JwtResponse;
import com.vortexadmin.entity.PasswordHistory;
import com.vortexadmin.entity.PasswordResetToken;
import com.vortexadmin.entity.RefreshToken;
import com.vortexadmin.entity.Role;
import com.vortexadmin.entity.User;
import com.vortexadmin.entity.UserSession;
import com.vortexadmin.entity.UserTwoFactor;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.PasswordHistoryRepository;
import com.vortexadmin.repository.PasswordResetTokenRepository;
import com.vortexadmin.repository.RefreshTokenRepository;
import com.vortexadmin.repository.RoleRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.repository.UserSessionRepository;
import com.vortexadmin.repository.UserTwoFactorRepository;
import com.vortexadmin.security.config.UserDetailsImpl;
import com.vortexadmin.security.jwt.JwtUtils;
import com.vortexadmin.service.AuthService;
import com.vortexadmin.service.AuditLogService;
import com.vortexadmin.service.MailService;
import com.vortexadmin.service.WebhookService;
import com.vortexadmin.util.TotpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserTwoFactorRepository userTwoFactorRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final MailService mailService;
    private final WebhookService webhookService;
    private final AuditLogService auditLogService;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final com.vortexadmin.service.PasswordPolicyService passwordPolicyService;
    private final com.vortexadmin.service.GeoLocationService geoLocationService;

    @Value("${vortex.app.frontendUrl}")
    private String frontendUrl;

    @Override
    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(LocalDateTime.now())) {
                throw new ApiException(HttpStatus.LOCKED, "Account is temporarily locked due to too many failed attempts.");
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Two-Factor Authentication check (password is already verified at this point)
            Optional<UserTwoFactor> twoFactorOpt = userTwoFactorRepository.findByUserId(userDetails.getId());
            if (twoFactorOpt.isPresent() && Boolean.TRUE.equals(twoFactorOpt.get().getEnabled())) {
                String code = loginRequest.getTwoFactorCode();
                if (code == null || code.isBlank()) {
                    // Tell the client to prompt for an OTP, without issuing tokens
                    return JwtResponse.builder()
                            .twoFactorRequired(true)
                            .username(userDetails.getUsername())
                            .build();
                }
                if (!verifyTwoFactorCode(twoFactorOpt.get(), code)) {
                    throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid two-factor authentication code");
                }
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setFailedLoginAttempts(0);
                user.setLockoutUntil(null);
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);

                // Create UserSession log
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");
                String[] geo = geoLocationService.lookupCountry(ipAddress);

                UserSession session = UserSession.builder()
                        .user(user)
                        .ipAddress(ipAddress)
                        .country(geo[0])
                        .countryCode(geo[1])
                        .userAgent(userAgent != null ? userAgent : "Unknown")
                        .loginAt(LocalDateTime.now())
                        .build();
                userSessionRepository.save(session);

                auditLogService.logAction("LOGIN", "User", user.getId(), "User logged into the system via credentials", ipAddress);
                
                // Refresh Token Logic
                refreshTokenRepository.deleteByUser(user);
                refreshTokenRepository.flush();
                
                RefreshToken refreshToken = RefreshToken.builder()
                        .user(user)
                        .token(UUID.randomUUID().toString())
                        .expiryDate(LocalDateTime.now().plusDays(7)) // 7 days expiry
                        .build();
                refreshTokenRepository.save(refreshToken);

                return JwtResponse.builder()
                        .token(jwt)
                        .refreshToken(refreshToken.getToken())
                        .id(userDetails.getId())
                        .username(userDetails.getUsername())
                        .email(userDetails.getEmail())
                        .roles(roles)
                        .build();
            }
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User not found after auth.");

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
                user.setFailedLoginAttempts(attempts + 1);
                if (user.getFailedLoginAttempts() >= 5) {
                    user.setLockoutUntil(LocalDateTime.now().plusMinutes(15));
                }
                userRepository.save(user);
            }
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
    }

    @Override
    @Transactional
    public JwtResponse authenticateWithGoogle(String token, HttpServletRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        String googleUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(googleUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
            }
            
            Map<String, Object> payload = response.getBody();
            String email = (String) payload.get("email");
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String googleId = (String) payload.get("sub");
            
            if (email == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Email not found in Google token");
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            User user;
            if (userOpt.isPresent()) {
                user = userOpt.get();
                if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(LocalDateTime.now())) {
                    throw new ApiException(HttpStatus.LOCKED, "Account is temporarily locked.");
                }
            } else {
                // Register user dynamically
                Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
                    Role role = Role.builder()
                            .name("USER")
                            .description("Standard User")
                            .permissions(new HashSet<>())
                            .build();
                    return roleRepository.save(role);
                });

                String generatedPassword = UUID.randomUUID().toString();
                String username = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 4);

                user = User.builder()
                        .username(username)
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .password(encoder.encode(generatedPassword))
                        .role(userRole)
                        .status("Active")
                        .failedLoginAttempts(0)
                        .build();
                user = userRepository.save(user);

                webhookService.triggerEvent("user.created", java.util.Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail()));
            }

            // Create Authentication object for Spring Security context
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateJwtToken(authentication);

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            user.setFailedLoginAttempts(0);
            user.setLockoutUntil(null);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Create UserSession log
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String[] geo = geoLocationService.lookupCountry(ipAddress);

            UserSession session = UserSession.builder()
                    .user(user)
                    .ipAddress(ipAddress)
                    .country(geo[0])
                    .countryCode(geo[1])
                    .userAgent(userAgent != null ? userAgent : "Unknown")
                    .loginAt(LocalDateTime.now())
                    .build();
            userSessionRepository.save(session);

            auditLogService.logAction("LOGIN", "User", user.getId(), "User logged into the system via Google OAuth", ipAddress);
            
            // Refresh Token Logic
            refreshTokenRepository.deleteByUser(user);
            refreshTokenRepository.flush();
            
            RefreshToken refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(LocalDateTime.now().plusDays(7)) // 7 days expiry
                    .build();
            refreshTokenRepository.save(refreshToken);

            return JwtResponse.builder()
                    .token(jwt)
                    .refreshToken(refreshToken.getToken())
                    .id(userDetails.getId())
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .roles(roles)
                    .build();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Failed to authenticate with Google: " + e.getMessage());
        }
    }

    /**
     * Accepts either a live TOTP code or an unused backup code.
     * Used backup codes are consumed so they cannot be replayed.
     */
    private boolean verifyTwoFactorCode(UserTwoFactor twoFactor, String code) {
        if (TotpUtil.verifyCode(twoFactor.getSecretKey(), code)) {
            return true;
        }
        if (twoFactor.getBackupCodes() != null && !twoFactor.getBackupCodes().isBlank()) {
            List<String> hashes = new java.util.ArrayList<>(List.of(twoFactor.getBackupCodes().split(",")));
            java.util.Iterator<String> it = hashes.iterator();
            while (it.hasNext()) {
                String hash = it.next();
                if (encoder.matches(code, hash)) {
                    it.remove();
                    twoFactor.setBackupCodes(String.join(",", hashes));
                    userTwoFactorRepository.save(twoFactor);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Email is already in use!");
        }

        passwordPolicyService.validate(signUpRequest.getPassword());

        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role role = Role.builder()
                    .name("USER")
                    .description("Standard User")
                    .permissions(new HashSet<>())
                    .build();
            return roleRepository.save(role);
        });

        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .role(userRole)
                .status("Active")
                .failedLoginAttempts(0)
                .build();
        userRepository.save(user);

        webhookService.triggerEvent("user.created", java.util.Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()));
    }

    @Override
    @Transactional
    public JwtResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Refresh token is not in database!"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new ApiException(HttpStatus.FORBIDDEN, "Refresh token was expired. Please make a new signin request");
        }

        User user = refreshToken.getUser();
        if (user == null) {
            refreshTokenRepository.delete(refreshToken);
            throw new ApiException(HttpStatus.FORBIDDEN, "Refresh token has no associated user");
        }
        
        // Create new JWT
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                UserDetailsImpl.build(user), null, UserDetailsImpl.build(user).getAuthorities());
                
        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roles = user.getRole() != null ? List.of(user.getRole().getName()) : List.of();

        return JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken.getToken())
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr != null && !refreshTokenStr.isEmpty()) {
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshTokenStr);
            if (tokenOpt.isPresent()) {
                RefreshToken token = tokenOpt.get();
                User user = token.getUser();
                
                // Delete refresh token
                refreshTokenRepository.delete(token);
                
                // Close the latest active session for this user
                userSessionRepository.findFirstByUserAndLogoutAtIsNullOrderByLoginAtDesc(user)
                        .ifPresent(session -> {
                            session.setLogoutAt(LocalDateTime.now());
                            userSessionRepository.save(session);
                        });
            }
        }
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        // Always respond success to the caller to avoid leaking which emails exist
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || userOpt.get().getDeletedAt() != null) {
            return;
        }
        User user = userOpt.get();

        passwordResetTokenRepository.deleteByUser(user);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + resetToken.getToken();
        mailService.send(user.getEmail(), "Reset your Vortex Admin Pro password",
                "Hi " + user.getUsername() + ",\n\n"
                        + "We received a request to reset your password. Click the link below to choose a new one:\n\n"
                        + resetLink + "\n\n"
                        + "This link expires in 1 hour. If you didn't request this, you can safely ignore this email.");
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token");
        }

        User user = resetToken.getUser();

        // Prevent reusing one of the last 5 passwords
        List<PasswordHistory> recentPasswords = passwordHistoryRepository.findTop5ByUserOrderByChangedAtDesc(user);
        for (PasswordHistory history : recentPasswords) {
            if (encoder.matches(newPassword, history.getPasswordHash())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "New password must not match any of your last 5 passwords");
            }
        }
        if (encoder.matches(newPassword, user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "New password must not match your current password");
        }

        passwordHistoryRepository.save(PasswordHistory.builder()
                .user(user)
                .passwordHash(user.getPassword())
                .build());

        user.setPassword(encoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Invalidate existing refresh tokens so old sessions must re-login
        refreshTokenRepository.deleteByUser(user);
    }
}
