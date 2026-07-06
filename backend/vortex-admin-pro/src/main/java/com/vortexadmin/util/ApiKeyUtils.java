package com.vortexadmin.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Static utility class for generating, hashing, and displaying API keys in
 * Vortex Admin Pro.
 *
 * <p>API keys follow the format {@code vrx_<40 hex characters>}, giving a total of
 * 44 printable characters with approximately 160 bits of entropy. Only the SHA-256
 * hash of the key is persisted in the database; the plaintext is shown to the user
 * exactly once at creation time and is never stored.
 *
 * <p>This class is not instantiable; all members are {@code static}.
 */
public class ApiKeyUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String HEX = "0123456789abcdef";

    /** Fixed prefix prepended to every generated key for visual identification. */
    private static final String KEY_PREFIX = "vrx_";

    /** Number of random hex characters appended after the prefix. */
    private static final int KEY_LENGTH = 40;

    /**
     * Number of characters from the full key exposed as a display prefix (e.g.,
     * {@code "vrx_a3f1..."}) so that users can identify keys in a list without
     * seeing the full secret.
     */
    private static final int DISPLAY_PREFIX_LENGTH = 12;

    /**
     * Generates a cryptographically secure API key.
     *
     * <p>The key is assembled as {@code "vrx_"} followed by {@value #KEY_LENGTH}
     * randomly selected lowercase hexadecimal characters, sourced from a
     * {@link SecureRandom} instance. The total length is {@code 4 + 40 = 44}
     * characters.
     *
     * @return a new, unique API key string in {@code vrx_<hex>} format.
     */
    public static String generateKey() {
        StringBuilder sb = new StringBuilder(KEY_PREFIX);
        for (int i = 0; i < KEY_LENGTH; i++) {
            sb.append(HEX.charAt(RANDOM.nextInt(HEX.length())));
        }
        return sb.toString();
    }

    /**
     * Extracts a short display prefix from the full API key for safe listing in the UI.
     *
     * <p>Returns the first {@value #DISPLAY_PREFIX_LENGTH} characters of the key
     * (e.g., {@code "vrx_a3f1b2c9"}). This prefix is non-secret because the space of
     * valid keys with a given prefix is still astronomically large.
     *
     * @param fullKey the complete API key string; must not be {@code null}.
     * @return a string of at most {@value #DISPLAY_PREFIX_LENGTH} characters taken from
     *         the beginning of {@code fullKey}.
     */
    public static String extractPrefix(String fullKey) {
        return fullKey.substring(0, Math.min(DISPLAY_PREFIX_LENGTH, fullKey.length()));
    }

    /**
     * Computes the SHA-256 hash of the given API key and returns it as a lowercase
     * hexadecimal string.
     *
     * <p>The hash is used as the storage key in the database so that even a full
     * database dump does not expose any usable API key material. When a request
     * arrives with a raw key in the {@code X-API-Key} header, this method is called
     * to derive the hash before the database lookup.
     *
     * @param fullKey the plaintext API key to hash; must not be {@code null}.
     * @return the 64-character lowercase hex representation of the SHA-256 digest.
     * @throws IllegalStateException if the JVM does not support the SHA-256 algorithm
     *                               (this should never occur on any standard JVM).
     */
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
