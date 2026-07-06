package com.vortexadmin.security.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vortexadmin.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security {@link UserDetails} implementation that adapts a Vortex Admin Pro
 * {@link User} entity into the principal object stored in the
 * {@link org.springframework.security.core.context.SecurityContextHolder}.
 *
 * <p>Instances of this class are created exclusively via the static factory method
 * {@link #build(User)} and are subsequently placed inside
 * {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}
 * objects by {@link com.vortexadmin.security.filter.JwtAuthFilter} and
 * {@link com.vortexadmin.security.filter.ApiKeyAuthFilter}.
 *
 * <p><strong>Authority model:</strong> The authorities collection is built in {@link #build(User)}
 * by combining two sources:
 * <ul>
 *   <li>Each permission code from the user's role (e.g., {@code user.read},
 *       {@code dashboard.view}) is added as a {@link SimpleGrantedAuthority}.</li>
 *   <li>The role name itself is prefixed with {@code ROLE_} (e.g., {@code ROLE_ADMIN})
 *       to satisfy Spring Security's convention for role-based checks
 *       ({@code hasRole("ADMIN")}).</li>
 * </ul>
 *
 * <p>The {@code password} field is annotated with {@code @JsonIgnore} to prevent
 * accidental serialization of the hashed password in API responses.
 */
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private String status;

    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs a fully initialized {@code UserDetailsImpl} instance.
     *
     * <p>This constructor is called only by {@link #build(User)}. Direct instantiation
     * from other classes should be avoided; use the factory method instead.
     *
     * @param id          the database primary key of the user.
     * @param username    the unique username used for authentication.
     * @param email       the user's email address.
     * @param password    the BCrypt-hashed password (never the plaintext value).
     * @param status      the account status string (e.g., {@code "Active"}, {@code "Suspended"}).
     * @param authorities the collection of granted authorities (roles and permissions).
     */
    public UserDetailsImpl(Long id, String username, String email, String password, String status,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
        this.authorities = authorities;
    }

    /**
     * Factory method that constructs a {@code UserDetailsImpl} from a {@link User} entity.
     *
     * <p>The authority list is built as follows:
     * <ol>
     *   <li>If the user has a role and that role has permissions, each permission's
     *       {@code code} field (e.g., {@code "task.read"}) is added as a
     *       {@link SimpleGrantedAuthority}.</li>
     *   <li>The role name is added with the {@code ROLE_} prefix (e.g.,
     *       {@code "ROLE_MANAGER"}) to enable Spring Security's {@code hasRole()} checks.</li>
     * </ol>
     *
     * @param user the {@link User} entity to adapt; must not be {@code null}.
     * @return a populated {@code UserDetailsImpl} instance.
     */
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getRole() != null) {
            if (user.getRole().getPermissions() != null) {
                user.getRole().getPermissions().stream()
                        .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
                        .forEach(authorities::add);
            }
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase()));
        }

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getStatus(),
                authorities);
    }

    /**
     * Returns the collection of authorities granted to this user.
     *
     * <p>The collection includes both fine-grained permission codes (e.g.,
     * {@code "user.read"}) and the prefixed role name (e.g., {@code "ROLE_ADMIN"}).
     *
     * @return an immutable-by-convention collection of {@link GrantedAuthority} objects.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the internal database ID of the authenticated user.
     *
     * <p>This value is embedded in the JWT as the {@code userId} claim and can be
     * retrieved without a database lookup from any component that has access to the
     * security context.
     *
     * @return the user's primary key.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the email address of the authenticated user.
     *
     * @return the user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the BCrypt-hashed password used for credential verification.
     *
     * <p>This field is annotated with {@code @JsonIgnore} and will not appear in
     * any JSON serialization of this object.
     *
     * @return the hashed password string.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user.
     *
     * @return the user's unique username.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has expired.
     *
     * <p>Vortex Admin Pro does not implement account expiry at the entity level;
     * this method always returns {@code true}.
     *
     * @return {@code true} always.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user's account is locked.
     *
     * <p>Account locking is not modelled as a separate flag in the current schema;
     * this method always returns {@code true}. Suspension is handled separately
     * via the {@link #isEnabled()} check.
     *
     * @return {@code true} always.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) have expired.
     *
     * <p>Credential expiry is not enforced in the current implementation;
     * this method always returns {@code true}.
     *
     * @return {@code true} always.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user account is enabled and allowed to authenticate.
     *
     * <p>Returns {@code false} when the user's {@code status} is exactly
     * {@code "Suspended"} (case-insensitive), preventing suspended users from
     * obtaining a valid authentication token. Returns {@code true} for all
     * other status values, including {@code null}.
     *
     * @return {@code false} if the account is suspended; {@code true} otherwise.
     */
    @Override
    public boolean isEnabled() {
        return status == null || !"Suspended".equalsIgnoreCase(status);
    }
}
