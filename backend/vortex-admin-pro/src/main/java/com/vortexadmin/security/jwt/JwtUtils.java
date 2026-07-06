package com.vortexadmin.security.jwt;

import com.vortexadmin.security.config.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;

/**
 * Utility component responsible for all JWT (JSON Web Token) operations within
 * the Vortex Admin Pro security layer.
 *
 * <p>This class handles three core concerns:
 * <ul>
 *   <li><strong>Generation</strong> – builds and signs a compact JWT from a successfully
 *       authenticated {@link Authentication} object, embedding the username, user ID, and
 *       roles as claims.</li>
 *   <li><strong>Parsing</strong> – extracts the subject (username) from a verified token.</li>
 *   <li><strong>Validation</strong> – verifies the HMAC-SHA256 signature and checks for
 *       expiry, malformation, unsupported format, and empty claims.</li>
 * </ul>
 *
 * <p>The signing secret is injected via the {@code vortex.app.jwtSecret} property, which
 * must be supplied as a Base64-encoded value of at least 256 bits (32 bytes). Failing to
 * meet this requirement causes the application to refuse startup via {@link #validateSecret()}.
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${vortex.app.jwtSecret}")
    private String jwtSecret;

    /**
     * Validates the JWT secret immediately after Spring injects property values.
     *
     * <p>Called automatically by Spring during context initialization ({@code @PostConstruct}).
     * The method enforces two invariants:
     * <ol>
     *   <li>The secret must not be null or blank.</li>
     *   <li>When decoded from Base64, the secret must be at least 32 bytes (256 bits) to
     *       satisfy HMAC-SHA256 minimum key-size requirements.</li>
     * </ol>
     *
     * @throws IllegalStateException if the secret is missing, blank, or too short.
     */
    @PostConstruct
    public void validateSecret() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("vortex.app.jwtSecret must be set via environment variable JWT_SECRET");
        }
        byte[] decoded = Decoders.BASE64.decode(jwtSecret);
        if (decoded.length < 32) {
            throw new IllegalStateException("vortex.app.jwtSecret must decode to at least 256 bits (32 bytes)");
        }
    }

    @Value("${vortex.app.jwtExpirationMs:86400000}")
    private int jwtExpirationMs; // 24 hours

    /**
     * Generates a signed JWT access token for the given authenticated principal.
     *
     * <p>The token is built with the following claims:
     * <ul>
     *   <li>{@code sub} – the username of the authenticated user.</li>
     *   <li>{@code userId} – the internal database ID of the user.</li>
     *   <li>{@code roles} – a list of authority strings (role names and permission codes)
     *       derived from the authentication object.</li>
     *   <li>{@code iat} / {@code exp} – issued-at and expiration timestamps, controlled by
     *       {@code vortex.app.jwtExpirationMs} (default 86 400 000 ms / 24 hours).</li>
     * </ul>
     *
     * <p>The token is signed using HMAC-SHA256 with the secret key returned by {@link #key()}.
     *
     * @param authentication the successfully authenticated Spring Security principal; its
     *                       principal must be an instance of {@link UserDetailsImpl}.
     * @return a compact, URL-safe JWT string.
     */
    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject((userPrincipal.getUsername()))
                .claim("userId", userPrincipal.getId())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    /**
     * Derives the HMAC-SHA256 {@link SecretKey} from the Base64-encoded {@code jwtSecret}
     * property.
     *
     * <p>This key is used for both signing new tokens and verifying incoming ones.
     * It is reconstructed on each call; callers should not cache the result across requests.
     *
     * @return a {@link SecretKey} suitable for HMAC-SHA256 JWT operations.
     */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Extracts the username (JWT {@code sub} claim) from a validated token string.
     *
     * <p>This method does not perform validity checks beyond signature verification.
     * Callers should first call {@link #validateJwtToken(String)} to ensure the token
     * is well-formed and unexpired before invoking this method.
     *
     * @param token the compact JWT string to parse.
     * @return the username stored in the token's {@code sub} claim.
     * @throws JwtException if the token cannot be parsed or its signature is invalid.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(key()).build()
               .parseSignedClaims(token).getPayload().getSubject();
    }

    /**
     * Validates a JWT token by verifying its HMAC-SHA256 signature and checking for
     * common error conditions.
     *
     * <p>Returns {@code false} and logs an error (without rethrowing) for the following
     * failure modes:
     * <ul>
     *   <li>{@link MalformedJwtException} – the token structure is invalid.</li>
     *   <li>{@link ExpiredJwtException} – the token's {@code exp} claim is in the past.</li>
     *   <li>{@link UnsupportedJwtException} – the token uses an unsupported algorithm or format.</li>
     *   <li>{@link IllegalArgumentException} – the claims string is empty or null.</li>
     * </ul>
     *
     * @param authToken the compact JWT string to validate.
     * @return {@code true} if the token is valid and unexpired; {@code false} otherwise.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
