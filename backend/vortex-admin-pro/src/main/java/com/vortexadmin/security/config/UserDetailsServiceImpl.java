package com.vortexadmin.security.config;

import com.vortexadmin.entity.User;
import com.vortexadmin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security {@link UserDetailsService} implementation that loads user-specific
 * authentication data from the Vortex Admin Pro database.
 *
 * <p>This service acts as the bridge between Spring Security's authentication
 * infrastructure and the application's {@link UserRepository}. It is consumed by:
 * <ul>
 *   <li>{@link com.vortexadmin.security.config.SecurityConfig#authenticationProvider()} –
 *       wired into the {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}
 *       so that username/password logins resolve the user from the database.</li>
 *   <li>{@link com.vortexadmin.security.filter.JwtAuthFilter} – used to reload
 *       full {@link UserDetails} (including the latest authorities) after a JWT is
 *       validated on each request.</li>
 * </ul>
 *
 * <p>Soft-deleted users (those with a non-null {@code deletedAt} field) are treated as
 * non-existent and will cause a {@link UsernameNotFoundException} to be thrown, preventing
 * them from authenticating even if their username is still present in the database.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads the {@link UserDetails} for the given username.
     *
     * <p>Queries the database via {@link UserRepository#findByUsernameAndDeletedAtIsNull(String)},
     * which automatically excludes soft-deleted users. The resulting {@link User} entity is then
     * adapted into a {@link UserDetailsImpl} via {@link UserDetailsImpl#build(User)}, populating
     * the principal with the user's ID, email, hashed password, status, and the full set of
     * role-derived authorities.
     *
     * <p>This method is annotated with {@code @Transactional} to ensure that lazy-loaded
     * associations on the {@link User} entity (such as {@code role.permissions}) are fetched
     * within an active transaction context.
     *
     * @param username the username to look up; must not be {@code null}.
     * @return a fully populated {@link UserDetailsImpl} representing the authenticated principal.
     * @throws UsernameNotFoundException if no active (non-deleted) user exists with the
     *                                   given username.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }
}
