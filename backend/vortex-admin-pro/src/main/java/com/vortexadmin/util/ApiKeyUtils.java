package com.vortexadmin.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class ApiKeyUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String HEX = "0123456789abcdef";
    private static final String KEY_PREFIX = "vrx_";
    private static final int KEY_LENGTH = 40;
    private static final int DISPLAY_PREFIX_LENGTH = 12;

    public static String generateKey() {
        StringBuilder sb = new StringBuilder(KEY_PREFIX);
        for (int i = 0; i < KEY_LENGTH; i++) {
            sb.append(HEX.charAt(RANDOM.nextInt(HEX.length())));
        }
        return sb.toString();
    }

    public static String extractPrefix(String fullKey) {
        return fullKey.substring(0, Math.min(DISPLAY_PREFIX_LENGTH, fullKey.length()));
    }

    public static String hash(String fullKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(fullKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
