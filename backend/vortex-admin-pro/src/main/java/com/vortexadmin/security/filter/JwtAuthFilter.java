package com.vortexadmin.security.filter;

import com.vortexadmin.security.config.UserDetailsServiceImpl;
import com.vortexadmin.security.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that authenticates HTTP requests carrying a Bearer JWT in the
 * {@code Authorization} header.
 *
 * <p>Extends {@link OncePerRequestFilter} to guarantee exactly one execution per
 * request, regardless of servlet dispatch type. The filter is positioned
 * <em>before</em> Spring Security's {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}
 * in the filter chain (configured in
 * {@link com.vortexadmin.security.config.SecurityConfig}).
 *
 * <p>Authentication flow per request:
 * <ol>
 *   <li>Extract the JWT from the {@code Authorization: Bearer &lt;token&gt;} header.</li>
 *   <li>Validate the token's signature and expiry via {@link JwtUtils#validateJwtToken(String)}.</li>
 *   <li>Load the corresponding {@link UserDetails} from the database.</li>
 *   <li>Populate the {@link SecurityContextHolder} with a
 *       {@link UsernamePasswordAuthenticationToken}, making the user's identity
 *       and authorities available to downstream controllers and method-security checks.</li>
 * </ol>
 *
 * <p>If the token is absent, invalid, or expired, the filter simply continues the
 * chain without setting an authentication — Spring Security will then reject the
 * request via {@link com.vortexadmin.security.jwt.JwtAuthEntryPoint}.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    /**
     * Core filter logic executed once per request.
     *
     * <p>Attempts to parse and validate the JWT from the request. On success, builds a
     * fully authenticated {@link UsernamePasswordAuthenticationToken} and stores it in
     * the {@link SecurityContextHolder}. Any exception during this process is caught and
     * logged, allowing the filter chain to continue so that Spring Security's access
     * control layer can reject the request with a proper error response.
     *
     * @param request     the incoming HTTP request.
     * @param response    the outgoing HTTP response.
     * @param filterChain the remaining filter chain to invoke after this filter completes.
     * @throws ServletException if the filter chain throws a servlet-level error.
     * @throws IOException      if an I/O error occurs during request/response processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw JWT string from the HTTP {@code Authorization} header.
     *
     * <p>Only the {@code Bearer} scheme is accepted. Tokens passed as URL query
     * parameters are intentionally rejected because they appear in server access logs
     * and browser history, which would be a security risk.
     *
     * @param request the HTTP request whose headers are inspected.
     * @return the raw JWT string (without the {@code "Bearer "} prefix), or
     *         {@code null} if the header is absent or does not use the Bearer scheme.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        // Tokens in URL query params appear in server logs and browser history — Authorization header only.
        return null;
    }
}
