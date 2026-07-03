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
import com.vortexadmin.service.AuditLogService;
import com.vortexadmin.service.AuthService;
import com.vortexadmin.service.GeoLocationService;
import com.vortexadmin.service.MailService;
import com.vortexadmin.service.PasswordPolicyService;
import com.vortexadmin.service.WebhookService;
import com.vortexadmin.util.TotpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int REFRESH_TOKEN_DAYS = 7;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;
    private static final int PASSWORD_HISTORY_LIMIT = 5;

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
    private final PasswordPolicyService passwordPolicyService;
    private final GeoLocationService geoLocationService;
    private final RestTemplate restTemplate;

    @Value("${vortex.app.frontendUrl}")
    private String frontendUrl;

    @Override
    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        userOpt.ifPresent(this::validateUserActive);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            Optional<UserTwoFactor> twoFactorOpt = userTwoFactorRepository.findByUserId(userDetails.getId());
            if (twoFactorOpt.isPresent() && Boolean.TRUE.equals(twoFactorOpt.get().getEnabled())) {
                String code = loginRequest.getTwoFactorCode();
                if (code == null || code.isBlank()) {
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

            User user = userOpt.get();
            user.setFailedLoginAttempts(0);
            user.setLockoutUntil(null);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            return buildSessionAndJwtResponse(user, userDetails, request, "credentials");

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            userOpt.ifPresent(user -> {
                int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
                user.setFailedLoginAttempts(attempts + 1);
                if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                    user.setLockoutUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
                }
                userRepository.save(user);
            });
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
    }

    @Override
    @Transactional
    public JwtResponse authenticateWithGoogle(String token, HttpServletRequest request) {
        String googleUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(googleUrl, HttpMethod.GET, entity, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
            }

            Map<String, Object> payload = response.getBody();
            String email = (String) payload.get("email");
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");

            Object emailVerified = payload.get("email_verified");
            if (!Boolean.TRUE.equals(emailVerified) && !"true".equals(String.valueOf(emailVerified))) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Google account email is not verified");
            }

            if (email == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Email not found in Google token");
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            User user;
            if (userOpt.isPresent()) {
                user = userOpt.get();
                validateUserActive(user);
            } else {
                user = createOAuthUser(email, firstName, lastName);
                notifyUserCreated(user);
            }

            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            user.setFailedLoginAttempts(0);
            user.setLockoutUntil(null);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            return buildSessionAndJwtResponse(user, userDetails, request, "Google OAuth");

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Failed to authenticate with Google: " + e.getMessage());
        }
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

        Role userRole = findOrCreateUserRole();

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
        notifyUserCreated(user);
    }

    @Override
    @Transactional
    public JwtResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
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

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String jwt = jwtUtils.generateJwtToken(authentication);
        List<String> roles = extractAuthorities(userDetails);

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
        if (refreshTokenStr == null || refreshTokenStr.isEmpty()) return;

        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
            User user = token.getUser();
            refreshTokenRepository.delete(token);
            userSessionRepository.findFirstByUserAndLogoutAtIsNullOrderByLoginAtDesc(user)
                    .ifPresent(session -> {
                        session.setLogoutAt(LocalDateTime.now());
                        userSessionRepository.save(session);
                    });
        });
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || userOpt.get().getDeletedAt() != null) {
            return; // Always respond success to avoid leaking which emails exist
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
        passwordPolicyService.validate(newPassword);
        validatePasswordNotReused(user, newPassword);

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

        refreshTokenRepository.deleteByUser(user);
    }

    // --- Private helpers ---

    private void validateUserActive(User user) {
        if (user.getDeletedAt() != null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        if ("Suspended".equalsIgnoreCase(user.getStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your account has been suspended. Please contact support.");
        }
        if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.LOCKED, "Account is temporarily locked due to too many failed attempts.");
        }
    }

    private JwtResponse buildSessionAndJwtResponse(User user, UserDetailsImpl userDetails,
                                                    HttpServletRequest request, String loginMethod) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String[] geo = geoLocationService.lookupCountry(ipAddress);

        userSessionRepository.save(UserSession.builder()
                .user(user)
                .ipAddress(ipAddress)
                .country(geo[0])
                .countryCode(geo[1])
                .userAgent(userAgent != null ? userAgent : "Unknown")
                .loginAt(LocalDateTime.now())
                .build());

        auditLogService.logAction("LOGIN", "User", user.getId(),
                "User logged into the system via " + loginMethod, ipAddress);

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);

        String jwt = jwtUtils.generateJwtToken(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        return JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken.getToken())
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(extractAuthorities(userDetails))
                .build();
    }

    private List<String> extractAuthorities(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private Role findOrCreateUserRole() {
        return roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(
                Role.builder()
                        .name("USER")
                        .description("Standard User")
                        .permissions(new HashSet<>())
                        .build()));
    }

    private User createOAuthUser(String email, String firstName, String lastName) {
        String username = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 4);
        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(encoder.encode(UUID.randomUUID().toString()))
                .role(findOrCreateUserRole())
                .status("Active")
                .failedLoginAttempts(0)
                .build());
    }

    private void notifyUserCreated(User user) {
        webhookService.triggerEvent("user.created", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()));
    }

    private void validatePasswordNotReused(User user, String newPassword) {
        if (encoder.matches(newPassword, user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "New password must not match your current password");
        }
        List<PasswordHistory> recent = passwordHistoryRepository.findTop5ByUserOrderByChangedAtDesc(user);
        for (PasswordHistory history : recent) {
            if (encoder.matches(newPassword, history.getPasswordHash())) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "New password must not match any of your last " + PASSWORD_HISTORY_LIMIT + " passwords");
            }
        }
    }

    private boolean verifyTwoFactorCode(UserTwoFactor twoFactor, String code) {
        if (TotpUtil.verifyCode(twoFactor.getSecretKey(), code)) {
            // Prevent replay within the same 30-second TOTP window
            long windowStart = (System.currentTimeMillis() / 1000L / 30L) * 30L;
            LocalDateTime windowStartDt = Instant.ofEpochSecond(windowStart)
                    .atZone(ZoneOffset.UTC).toLocalDateTime();
            if (twoFactor.getLastUsedTotpAt() != null
                    && !twoFactor.getLastUsedTotpAt().isBefore(windowStartDt)) {
                return false;
            }
            twoFactor.setLastUsedTotpAt(windowStartDt);
            userTwoFactorRepository.save(twoFactor);
            return true;
        }
        if (twoFactor.getBackupCodes() != null && !twoFactor.getBackupCodes().isBlank()) {
            List<String> hashes = new ArrayList<>(List.of(twoFactor.getBackupCodes().split(",")));
            Iterator<String> it = hashes.iterator();
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
}
