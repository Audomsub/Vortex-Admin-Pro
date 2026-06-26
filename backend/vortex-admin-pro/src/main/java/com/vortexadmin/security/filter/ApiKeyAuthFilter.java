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

public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    private final ApiKeyRepository apiKeyRepository;
    private final ApiRateLimitService rateLimitService;

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository, ApiRateLimitService rateLimitService) {
        this.apiKeyRepository = apiKeyRepository;
        this.rateLimitService = rateLimitService;
    }

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
