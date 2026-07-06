package com.vortexadmin.security.filter;

import com.vortexadmin.entity.ApiKey;
import com.vortexadmin.repository.ApiKeyRepository;
import com.vortexadmin.security.config.UserDetailsImpl;
import com.vortexadmin.service.ApiRateLimitService;
import com.vortexadmin.util.ApiKeyUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servlet filter that authenticates HTTP requests using an API key supplied in the
 * {@value #API_KEY_HEADER} request header.
 *
 * <p>Extends {@link OncePerRequestFilter} to guarantee a single execution per request.
 * The filter is registered in
 * {@link com.vortexadmin.security.config.SecurityConfig} and runs
 * <em>before</em> {@link JwtAuthFilter} so that machine-to-machine clients carrying
 * only an API key are authenticated without requiring a Bearer token.
 *
 * <p>Authentication flow:
 * <ol>
 *   <li>Read the raw API key from the {@value #API_KEY_HEADER} header.</li>
 *   <li>Hash the raw key with SHA-256 (via {@link ApiKeyUtils#hash(String)}) and look it
 *       up in the database — the plaintext key is never stored.</li>
 *   <li>Verify that the key is not revoked, not expired, and that its owning user has not
 *       been soft-deleted.</li>
 *   <li>Enforce per-minute and per-hour rate limits via {@link ApiRateLimitService}; return
 *       HTTP {@code 429} if the limit is exceeded.</li>
 *   <li>Intersect the user's full authority set with the key's declared scopes, restricting
 *       access to read-only operations ({@code .read}, {@code .view}, {@code .export}).</li>
 *   <li>Store a {@link UsernamePasswordAuthenticationToken} in the
 *       {@link SecurityContextHolder} with the scoped authorities.</li>
 *   <li>Update the key's {@code lastUsedAt} timestamp.</li>
 * </ol>
 *
 * <p>If the {@link SecurityContextHolder} already contains an authentication (e.g., a JWT
 * was processed by a preceding filter), this filter takes no action and passes through.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    /** Name of the HTTP request header expected to carry the raw API key. */
    public static final String API_KEY_HEADER = "X-API-Key";
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    private final ApiKeyRepository apiKeyRepository;
    private final ApiRateLimitService rateLimitService;

    /**
     * Constructs an {@code ApiKeyAuthFilter} with its required collaborators.
     *
     * <p>This filter is not a Spring-managed component ({@code @Component} is omitted
     * intentionally) because it requires constructor arguments that are supplied
     * programmatically from {@link com.vortexadmin.security.config.SecurityConfig#apiKeyAuthFilter()}.
     *
     * @param apiKeyRepository the JPA repository used to look up hashed API keys.
     * @param rateLimitService the service that tracks and enforces per-key request rate limits.
     */
    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository, ApiRateLimitService rateLimitService) {
        this.apiKeyRepository = apiKeyRepository;
        this.rateLimitService = rateLimitService;
    }

    /**
     * Core filter logic executed once per request.
     *
     * <p>Reads the {@value #API_KEY_HEADER} header, validates the key, enforces rate limits,
     * and populates the {@link SecurityContextHolder} with a scope-restricted authentication
     * token. If the header is absent, or the key is invalid/revoked/expired, the method
     * delegates immediately to the next filter without setting any authentication — downstream
     * security rules will reject unauthorized requests accordingly.
     *
     * <p>On rate-limit violation, the method writes an HTTP {@code 429} JSON error response
     * and returns early, preventing further filter-chain execution.
     *
     * @param request     the incoming HTTP request.
     * @param response    the outgoing HTTP response.
     * @param filterChain the remaining filter chain.
     * @throws ServletException if a servlet-level error occurs.
     * @throws IOException      if an I/O error occurs while writing the rate-limit error response.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String rawKey = request.getHeader(API_KEY_HEADER);
            if (StringUtils.hasText(rawKey) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String keyHash = ApiKeyUtils.hash(rawKey);
                Optional<ApiKey> keyOpt = apiKeyRepository.findByKeyHash(keyHash);

                if (keyOpt.isPresent()) {
                    ApiKey apiKey = keyOpt.get();
                    boolean expired = apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(LocalDateTime.now());

                    if (!apiKey.isRevoked() && !expired && apiKey.getUser().getDeletedAt() == null) {
                        if (!rateLimitService.isAllowed(keyHash, apiKey.getRateLimitPerMinute(), apiKey.getRateLimitPerHour())) {
                            response.setStatus(429);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"Rate limit exceeded for this API key\"}");
                            return;
                        }

                        UserDetailsImpl userDetails = UserDetailsImpl.build(apiKey.getUser());

                        List<GrantedAuthority> scopedAuthorities = userDetails.getAuthorities().stream()
                                .filter(auth -> {
                                    String authority = auth.getAuthority();
                                    return apiKey.getScopes() != null &&
                                           apiKey.getScopes().contains(authority) &&
                                           (authority.endsWith(".read") || authority.endsWith(".view") || authority.endsWith(".export"));
                                })
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, scopedAuthorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        apiKey.setLastUsedAt(LocalDateTime.now());
                        apiKeyRepository.save(apiKey);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot authenticate via API key: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
