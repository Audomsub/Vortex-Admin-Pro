package com.vortexadmin.security.config;

import com.vortexadmin.repository.ApiKeyRepository;
import com.vortexadmin.security.filter.ApiKeyAuthFilter;
import com.vortexadmin.security.filter.MaintenanceModeFilter;
import com.vortexadmin.service.ApiRateLimitService;
import com.vortexadmin.security.filter.JwtAuthFilter;
import com.vortexadmin.security.jwt.JwtAuthEntryPoint;
import com.vortexadmin.security.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

/**
 * Central Spring Security configuration class for Vortex Admin Pro.
 *
 * <p>Annotated with {@code @Configuration} and {@code @EnableMethodSecurity}, this class
 * defines the entire security filter chain, authentication provider, password encoder,
 * CORS policy, and bean wiring for all security-related components.
 *
 * <p>Key security decisions made here:
 * <ul>
 *   <li><strong>Stateless sessions</strong> – {@link SessionCreationPolicy#STATELESS} ensures
 *       no HTTP session is created or used; all authentication state lives in the JWT or
 *       API key header.</li>
 *   <li><strong>CSRF disabled</strong> – Safe for a pure REST API that never serves
 *       browser-rendered forms.</li>
 *   <li><strong>Filter order</strong> – {@link ApiKeyAuthFilter} runs first (before
 *       {@link JwtAuthFilter}), followed by JWT authentication, and then
 *       {@link MaintenanceModeFilter} after authentication is resolved.</li>
 *   <li><strong>OAuth2 login</strong> – Delegates OAuth2 user info processing to
 *       {@link CustomOAuth2UserService}.</li>
 *   <li><strong>Actuator protection</strong> – {@code /actuator/**} endpoints are restricted
 *       to users with the {@code SUPER_ADMIN} role.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${vortex.cors.allowedOrigins:http://localhost:5173}")
    private String allowedOriginsRaw;

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthEntryPoint unauthorizedHandler;
    private final ApiKeyRepository apiKeyRepository;
    private final JwtAuthFilter jwtAuthFilter;
    private final ApiRateLimitService apiRateLimitService;
    private final MaintenanceModeFilter maintenanceModeFilter;

    /**
     * Creates and registers the {@link ApiKeyAuthFilter} bean.
     *
     * <p>The filter is not a Spring-managed component itself (to avoid double-registration
     * by Spring Boot's auto-configuration), so it is explicitly instantiated here with its
     * required dependencies.
     *
     * @return a configured {@link ApiKeyAuthFilter} ready for use in the security filter chain.
     */
    @Bean
    public ApiKeyAuthFilter apiKeyAuthFilter() {
        return new ApiKeyAuthFilter(apiKeyRepository, apiRateLimitService);
    }

    /**
     * Exposes the Spring Security {@link AuthenticationManager} as a bean so that it can
     * be injected into authentication controllers (e.g., the login endpoint).
     *
     * @param authConfig the auto-configured {@link AuthenticationConfiguration}.
     * @return the application's primary {@link AuthenticationManager}.
     * @throws Exception if the authentication manager cannot be retrieved.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Provides the application-wide {@link PasswordEncoder} bean.
     *
     * <p>Uses {@link BCryptPasswordEncoder} (default strength 10), which is the recommended
     * adaptive hashing algorithm for password storage in Spring Security applications.
     *
     * @return a {@link BCryptPasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures and returns a {@link DaoAuthenticationProvider} that delegates user
     * lookup to {@link UserDetailsServiceImpl} and password verification to the
     * {@link BCryptPasswordEncoder}.
     *
     * <p>This provider is used by the {@link AuthenticationManager} when processing
     * username/password login requests.
     *
     * @return a fully configured {@link DaoAuthenticationProvider}.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Defines the main {@link SecurityFilterChain} that governs all HTTP request security.
     *
     * <p>The chain is configured as follows:
     * <ul>
     *   <li>CORS is applied using the source returned by {@link #corsConfigurationSource()}.</li>
     *   <li>CSRF protection is disabled (stateless REST API).</li>
     *   <li>Unauthenticated requests to protected endpoints are handled by
     *       {@link JwtAuthEntryPoint} (returns HTTP 401).</li>
     *   <li>Public paths: {@code /api/auth/**}, {@code /api/public/**},
     *       {@code /v3/api-docs/**}, {@code /swagger-ui/**}.</li>
     *   <li>Actuator endpoints require the {@code SUPER_ADMIN} role.</li>
     *   <li>All other requests require authentication.</li>
     *   <li>OAuth2 login delegates user-info loading to {@link CustomOAuth2UserService}.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security.
     * @return the built and configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during the security configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                auth.requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                    .requestMatchers("/actuator/**").hasRole("SUPER_ADMIN")
                    .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
            );


        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(apiKeyAuthFilter(), JwtAuthFilter.class);
        http.addFilterAfter(maintenanceModeFilter, JwtAuthFilter.class);

        return http.build();
    }

    /**
     * Builds and returns the {@link CorsConfigurationSource} used by the security filter chain.
     *
     * <p>Allowed origins are read from the {@code vortex.cors.allowedOrigins} property
     * (comma-separated list, defaulting to {@code http://localhost:5173}). All standard
     * HTTP methods and all headers are permitted, and credentials (cookies, Authorization
     * headers) are allowed so that the frontend SPA can authenticate correctly.
     *
     * @return a {@link CorsConfigurationSource} applied to all URL patterns ({@code /**}).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOriginsRaw.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
