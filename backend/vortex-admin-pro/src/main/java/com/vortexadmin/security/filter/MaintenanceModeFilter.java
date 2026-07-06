package com.vortexadmin.security.filter;

import com.vortexadmin.repository.SystemSettingRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Servlet filter that enforces system-wide maintenance mode for the Vortex Admin Pro API.
 *
 * <p>When maintenance mode is enabled (controlled by the {@code maintenance_mode} key in the
 * {@code system_settings} table), all non-admin API requests are rejected with an HTTP
 * {@code 503 Service Unavailable} response containing a JSON error body. Users holding
 * the {@code ROLE_SUPER_ADMIN} or {@code SUPER_ADMIN} authority are always permitted to
 * pass through, allowing administrators to perform maintenance operations uninterrupted.
 *
 * <p>The filter extends {@link OncePerRequestFilter} to guarantee a single invocation per
 * request. It is registered in
 * {@link com.vortexadmin.security.config.SecurityConfig} to run <em>after</em>
 * {@link JwtAuthFilter} so that the authentication context is already populated before
 * the super-admin check is performed.
 *
 * <p><strong>Performance:</strong> Rather than querying the database on every request, the
 * maintenance flag is cached in memory using atomic variables. The cache has a 30-second
 * TTL ({@code CACHE_TTL_MS}), meaning a toggle applied via the settings API takes effect
 * within 30 seconds across all in-flight requests.
 *
 * <p><strong>Always-permitted paths:</strong> Auth endpoints ({@code /api/auth/**}),
 * OpenAPI docs ({@code /v3/api-docs/**}), and Swagger UI ({@code /swagger-ui/**}) bypass
 * the maintenance check entirely.
 */
@Component
@RequiredArgsConstructor
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private final SystemSettingRepository settingRepository;

    private final AtomicBoolean cachedValue = new AtomicBoolean(false);
    private final AtomicLong cacheExpiry = new AtomicLong(0);

    /** Duration in milliseconds for which the maintenance flag is cached in memory. */
    private static final long CACHE_TTL_MS = 30_000;

    /**
     * Reads the current maintenance mode flag, using an in-memory cache to avoid
     * a database round-trip on every request.
     *
     * <p>If the cached value has expired (i.e., {@code now > cacheExpiry}), the
     * method fetches the {@code maintenance_mode} system setting from the database,
     * updates the cached value and expiry timestamp atomically, and then returns
     * the fresh result.
     *
     * @return {@code true} if maintenance mode is currently enabled; {@code false} otherwise.
     */
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

    /**
     * Core filter logic executed once per request.
     *
     * <p>The method first checks whether the request path targets an always-permitted
     * endpoint (auth, Swagger). If so, it continues the chain immediately. Otherwise,
     * it consults {@link #isMaintenanceMode()} and, if maintenance is active, inspects
     * the current {@link SecurityContextHolder} authentication to determine whether the
     * caller is a super-admin. Non-super-admin callers receive an HTTP {@code 503} JSON
     * response and the filter chain is terminated.
     *
     * @param request     the incoming HTTP request.
     * @param response    the outgoing HTTP response.
     * @param filterChain the remaining filter chain to invoke when the request is permitted.
     * @throws ServletException if a servlet-level error occurs.
     * @throws IOException      if an I/O error occurs while writing the 503 error response.
     */
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
