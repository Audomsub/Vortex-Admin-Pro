package com.vortexadmin.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security entry point invoked whenever an unauthenticated request attempts
 * to access a protected resource.
 *
 * <p>Implements {@link AuthenticationEntryPoint} and is registered in
 * {@link com.vortexadmin.security.config.SecurityConfig} as the handler for
 * {@code exceptionHandling().authenticationEntryPoint(...)}.
 *
 * <p>Instead of redirecting to a login page (inappropriate for a stateless REST API),
 * this class immediately writes an HTTP {@code 401 Unauthorized} response, signalling
 * to the API client that valid credentials must be provided.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthEntryPoint.class);

    /**
     * Called by Spring Security when an {@link AuthenticationException} is thrown due
     * to a missing or invalid credential on a secured endpoint.
     *
     * <p>The method logs the reason for rejection and sends an HTTP {@code 401} error
     * response with the message {@code "Error: Unauthorized"}. No redirect is performed
     * because the API is fully stateless (no session, no login page).
     *
     * @param request       the incoming HTTP request that triggered the exception.
     * @param response      the HTTP response to which the 401 status is written.
     * @param authException the exception describing why authentication failed
     *                      (e.g., missing token, expired credential).
     * @throws IOException      if writing to the response stream fails.
     * @throws ServletException if a servlet-level error occurs.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        logger.error("Unauthorized error: {}", authException.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }
}
