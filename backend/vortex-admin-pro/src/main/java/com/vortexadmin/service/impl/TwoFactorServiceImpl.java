package com.vortexadmin.service.impl;

import com.vortexadmin.dto.response.TwoFactorSetupResponse;
import com.vortexadmin.dto.response.TwoFactorStatusResponse;
import com.vortexadmin.entity.User;
import com.vortexadmin.entity.UserTwoFactor;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.repository.UserTwoFactorRepository;
import com.vortexadmin.service.TwoFactorService;
import com.vortexadmin.util.SecurityUtils;
import com.vortexadmin.util.TotpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TwoFactorServiceImpl implements TwoFactorService {

    private static final String ISSUER = "Vortex Admin Pro";
    private static final int BACKUP_CODE_COUNT = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserTwoFactorRepository twoFactorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private int countBackupCodes(UserTwoFactor tfa) {
        if (tfa.getBackupCodes() == null || tfa.getBackupCodes().isBlank()) return 0;
        return tfa.getBackupCodes().split(",").length;
    }

    @Override
    public TwoFactorStatusResponse getStatus() {
        return twoFactorRepository.findByUserId(SecurityUtils.getCurrentUserId())
                .map(tfa -> TwoFactorStatusResponse.builder()
                        .enabled(Boolean.TRUE.equals(tfa.getEnabled()))
                        .remainingBackupCodes(countBackupCodes(tfa))
                        .build())
                .orElse(TwoFactorStatusResponse.builder().enabled(false).build());
    }

    @Override
    @Transactional
    public TwoFactorSetupResponse setup() {
        User user = getCurrentUser();

        UserTwoFactor tfa = twoFactorRepository.findByUserId(user.getId()).orElse(null);
        if (tfa != null && Boolean.TRUE.equals(tfa.getEnabled())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Two-factor authentication is already enabled");
        }

        String secret = TotpUtil.generateSecret();
        if (tfa == null) {
            tfa = UserTwoFactor.builder()
                    .user(user)
                    .secretKey(secret)
                    .enabled(false)
                    .build();
        } else {
            tfa.setSecretKey(secret);
            tfa.setEnabled(false);
            tfa.setBackupCodes(null);
        }
        twoFactorRepository.save(tfa);

        return TwoFactorSetupResponse.builder()
                .secret(secret)
                .otpAuthUrl(TotpUtil.buildOtpAuthUrl(ISSUER, user.getEmail(), secret))
                .build();
    }

    @Override
    @Transactional
    public TwoFactorStatusResponse verifyAndEnable(String code) {
        UserTwoFactor tfa = twoFactorRepository.findByUserId(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Run 2FA setup first"));

        if (Boolean.TRUE.equals(tfa.getEnabled())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Two-factor authentication is already enabled");
        }
        if (!TotpUtil.verifyCode(tfa.getSecretKey(), code)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid verification code");
        }

        List<String> plainBackupCodes = new ArrayList<>();
        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            plainBackupCodes.add(String.format("%08d", RANDOM.nextInt(100_000_000)));
        }
        String hashed = plainBackupCodes.stream()
                .map(passwordEncoder::encode)
                .collect(Collectors.joining(","));

        tfa.setEnabled(true);
        tfa.setBackupCodes(hashed);
        twoFactorRepository.save(tfa);

        return TwoFactorStatusResponse.builder()
                .enabled(true)
                .remainingBackupCodes(plainBackupCodes.size())
                .backupCodes(plainBackupCodes)
                .build();
    }

    @Override
    @Transactional
    public void disable(String code) {
        UserTwoFactor tfa = twoFactorRepository.findByUserId(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Two-factor authentication is not enabled"));

        if (!Boolean.TRUE.equals(tfa.getEnabled())) {
            twoFactorRepository.delete(tfa);
            return;
        }

        boolean valid = TotpUtil.verifyCode(tfa.getSecretKey(), code);
        if (!valid && tfa.getBackupCodes() != null && !tfa.getBackupCodes().isBlank()) {
            valid = Arrays.stream(tfa.getBackupCodes().split(","))
                    .anyMatch(hash -> passwordEncoder.matches(code, hash));
        }
        if (!valid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid verification code");
        }

        twoFactorRepository.delete(tfa);
    }
}
