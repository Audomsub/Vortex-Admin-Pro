package com.vortexadmin.security.filter;

import com.vortexadmin.repository.SystemSettingRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private final SystemSettingRepository settingRepository;

    // BUG-028: cache result for 30 seconds to avoid a DB round-trip on every request
    private final AtomicBoolean cachedValue = new AtomicBoolean(false);
    private final AtomicLong cacheExpiry = new AtomicLong(0);
    private static final long CACHE_TTL_MS = 30_000;

    private boolean isMaintenanceMode() {
        long now = System.currentTimeMillis();
        if (now > cacheExpiry.get()) {
            boolean fresh = settingRepository.findBySettingKey("maintenance_mode")
                    .map(s -> "true".equalsIgnoreCase(s.getSettingValue()))
                    .orElse(false);
            cachedValue.set(fresh);
            cacheExpiry.set(now + CACHE_TTL_MS);
        }
        return cachedValue.get();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // Never block auth endpoints or Swagger
        if (uri.startsWith("/api/auth") || uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean maintenance = isMaintenanceMode();

        if (maintenance) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("SUPER_ADMIN"));

            if (!isSuperAdmin) {
                response.setStatus(503);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"The system is currently under maintenance. Please try again later.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
