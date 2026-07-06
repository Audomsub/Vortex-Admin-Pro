package com.vortexadmin.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * RFC 6238 TOTP implementation (HMAC-SHA1, 6 digits, 30-second time step).
 * Compatible with Google Authenticator, Authy, 1Password, etc.
 *
 * <p>This utility provides the three operations required to support Time-based
 * One-Time Password (TOTP) two-factor authentication:
 * <ol>
 *   <li><strong>Secret generation</strong> – produces a cryptographically random
 *       Base32-encoded shared secret that is stored (encrypted) for the user and
 *       scanned by their authenticator app.</li>
 *   <li><strong>Code verification</strong> – validates a 6-digit code supplied by
 *       the user against the current time step, with a ±1-step clock-drift window
 *       to tolerate slight time discrepancies between server and device.</li>
 *   <li><strong>OTP-Auth URL generation</strong> – builds a {@code otpauth://totp/...}
 *       URI that is typically encoded as a QR code for the user to scan.</li>
 * </ol>
 *
 * <p>This class is {@code final} and not instantiable; all members are {@code static}.
 */
public final class TotpUtil {

    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;

    /**
     * Number of 30-second time steps (before and after the current step) that are
     * accepted as valid. A value of {@code 1} means codes from the previous and next
     * 30-second windows are also accepted, accommodating clock drift of up to 30 seconds.
     */
    private static final int ALLOWED_TIME_DRIFT_STEPS = 1;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Private constructor — prevents instantiation of this static utility class.
     */
    private TotpUtil() {
    }

    /**
     * Generates a cryptographically random 20-byte TOTP shared secret encoded in Base32.
     *
     * <p>The resulting string (160 bits of entropy) is suitable for use as the shared
     * secret in an {@code otpauth://} URI and for storage in the database. It must be
     * kept confidential; anyone with the secret can generate valid TOTP codes.
     *
     * @return a Base32-encoded TOTP secret string, typically 32 characters long.
     */
    public static String generateSecret() {
        byte[] bytes = new byte[20];
        RANDOM.nextBytes(bytes);
        return base32Encode(bytes);
    }

    /**
     * Verifies a user-supplied TOTP code against the given Base32-encoded shared secret.
     *
     * <p>The method accepts codes from the current 30-second time step as well as
     * {@value #ALLOWED_TIME_DRIFT_STEPS} step(s) before and after it to tolerate minor
     * clock differences between the server and the user's authenticator device.
     *
     * <p>If {@code code} is {@code null} or does not consist of exactly
     * {@value #CODE_DIGITS} decimal digits, the method returns {@code false} immediately
     * without performing any cryptographic operations.
     *
     * @param base32Secret the Base32-encoded TOTP shared secret stored for the user.
     * @param code         the 6-digit one-time code entered by the user.
     * @return {@code true} if the code matches any acceptable time step; {@code false} otherwise.
     */
    public static boolean verifyCode(String base32Secret, String code) {
        if (code == null || !code.matches("\\d{" + CODE_DIGITS + "}")) {
            return false;
        }
        long currentStep = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        for (int drift = -ALLOWED_TIME_DRIFT_STEPS; drift <= ALLOWED_TIME_DRIFT_STEPS; drift++) {
            if (generateCode(base32Secret, currentStep + drift).equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds an {@code otpauth://totp/} URI that encodes all parameters required for
     * an authenticator app to configure a TOTP entry by scanning a QR code.
     *
     * <p>The URI follows the
     * <a href="https://github.com/google/google-authenticator/wiki/Key-Uri-Format">
     * Google Authenticator Key URI format</a>:
     * <pre>
     *   otpauth://totp/&lt;issuer&gt;:&lt;account&gt;?secret=&lt;secret&gt;&amp;issuer=&lt;issuer&gt;&amp;algorithm=SHA1&amp;digits=6&amp;period=30
     * </pre>
     * Both {@code issuer} and {@code accountName} are URL-encoded to handle special
     * characters in organisation or user names.
     *
     * @param issuer      the name of the service displayed in the authenticator app
     *                    (e.g., {@code "Vortex Admin Pro"}).
     * @param accountName the user-specific label, typically the user's email address.
     * @param base32Secret the Base32-encoded TOTP shared secret.
     * @return a fully formed {@code otpauth://totp/...} URI string ready to be
     *         encoded into a QR code image.
     */
    public static String buildOtpAuthUrl(String issuer, String accountName, String base32Secret) {
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String encodedAccount = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
        return "otpauth://totp/" + encodedIssuer + ":" + encodedAccount
                + "?secret=" + base32Secret
                + "&issuer=" + encodedIssuer
                + "&algorithm=SHA1&digits=" + CODE_DIGITS
                + "&period=" + TIME_STEP_SECONDS;
    }

    /**
     * Computes the TOTP code for the given Base32-encoded secret and time step counter.
     *
     * <p>Implements the HOTP algorithm (RFC 4226) with the time step as the counter:
     * <ol>
     *   <li>Decodes the Base32 secret into raw bytes.</li>
     *   <li>Encodes the 64-bit time-step counter as a big-endian byte array.</li>
     *   <li>Computes an HMAC-SHA1 MAC over the counter bytes using the secret key.</li>
     *   <li>Applies dynamic truncation: reads a 4-byte integer starting at an offset
     *       determined by the last nibble of the HMAC output.</li>
     *   <li>Takes the integer modulo 10^{@value #CODE_DIGITS} and zero-pads the result
     *       to {@value #CODE_DIGITS} digits.</li>
     * </ol>
     *
     * @param base32Secret the Base32-encoded TOTP shared secret.
     * @param timeStep     the current time step counter (Unix epoch seconds / 30).
     * @return a zero-padded {@value #CODE_DIGITS}-digit TOTP code string.
     * @throws IllegalStateException if HMAC-SHA1 computation fails for any reason.
     */
    private static String generateCode(String base32Secret, long timeStep) {
        try {
            byte[] key = base32Decode(base32Secret);
            byte[] data = new byte[8];
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (timeStep & 0xFF);
                timeStep >>= 8;
            }

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", otp);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate TOTP code", e);
        }
    }

    /**
     * Encodes a raw byte array into a Base32 string using the RFC 4648 alphabet.
     *
     * <p>Processes input bytes in 5-bit groups. If the total bit count is not a
     * multiple of 5, a final character is appended using the remaining bits
     * left-shifted to fill a full 5-bit group (no padding characters are added).
     *
     * @param data the raw bytes to encode; must not be {@code null}.
     * @return the Base32-encoded string representation of {@code data}.
     */
    private static String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                sb.append(BASE32_ALPHABET.charAt((buffer >> (bitsLeft - 5)) & 0x1F));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            sb.append(BASE32_ALPHABET.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        }
        return sb.toString();
    }

    /**
     * Decodes a Base32-encoded string back into raw bytes.
     *
     * <p>The input is normalised (trimmed, spaces and padding characters removed,
     * converted to upper-case) before decoding. Each character is mapped to its
     * 5-bit value in the RFC 4648 Base32 alphabet; characters not found in that
     * alphabet cause an {@link IllegalArgumentException}.
     *
     * @param encoded the Base32-encoded string to decode; must not be {@code null}.
     * @return the decoded byte array.
     * @throws IllegalArgumentException if the string contains characters outside
     *                                  the Base32 alphabet.
     */
    private static byte[] base32Decode(String encoded) {
        String cleaned = encoded.trim().replace(" ", "").replace("=", "").toUpperCase();
        int buffer = 0;
        int bitsLeft = 0;
        byte[] result = new byte[cleaned.length() * 5 / 8];
        int index = 0;
        for (char c : cleaned.toCharArray()) {
            int value = BASE32_ALPHABET.indexOf(c);
            if (value < 0) {
                throw new IllegalArgumentException("Invalid Base32 character: " + c);
            }
            buffer = (buffer << 5) | value;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                result[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return result;
    }
}
