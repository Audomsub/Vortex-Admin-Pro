package com.vortexadmin.security.filter;

import com.vortexadmin.entity.ApiKey;
import com.vortexadmin.repository.ApiKeyRepository;
import com.vortexadmin.security.config.UserDetailsImpl;
import com.vortexadmin.util.ApiKeyUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String rawKey = request.getHeader(API_KEY_HEADER);
            if (StringUtils.hasText(rawKey) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<ApiKey> keyOpt = apiKeyRepository.findByKeyHash(ApiKeyUtils.hash(rawKey));
                if (keyOpt.isPresent()) {
                    ApiKey apiKey = keyOpt.get();
                    boolean expired = apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(LocalDateTime.now());
                    if (!apiKey.isRevoked() && !expired && apiKey.getUser().getDeletedAt() == null) {
                        UserDetailsImpl userDetails = UserDetailsImpl.build(apiKey.getUser());
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
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
