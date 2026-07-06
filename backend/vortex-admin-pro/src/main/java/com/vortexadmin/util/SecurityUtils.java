package com.vortexadmin.util;

import com.vortexadmin.exception.ApiException;
import com.vortexadmin.security.config.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Static utility class providing convenience methods for accessing and interrogating
 * the Spring Security context within the Vortex Admin Pro service layer.
 *
 * <p>All methods are stateless and operate solely on the thread-local
 * {@link SecurityContextHolder}. They are intended to be called from service
 * implementations ({@code service.impl} package) and other non-web components where
 * injecting the full {@link Authentication} object would add unnecessary boilerplate.
 *
 * <p>This class is not instantiable; all members are {@code static}.
 */
public class SecurityUtils {

    /**
     * Returns the database ID of the currently authenticated user.
     *
     * <p>Reads the principal from the active {@link SecurityContextHolder} and casts
     * it to {@link UserDetailsImpl} to extract the numeric user ID. This ID is used
     * by service methods that need to scope operations to the calling user (e.g.,
     * "fetch only my tasks").
     *
     * @return the {@code Long} primary key of the authenticated user.
     * @throws ApiException with HTTP {@code 401 UNAUTHORIZED} if there is no active
     *                      authentication or the principal is not a {@link UserDetailsImpl}.
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    /**
     * Checks whether the currently authenticated user possesses the specified authority.
     *
     * <p>The authority string can be either a permission code (e.g., {@code "user.delete"})
     * or a prefixed role name (e.g., {@code "ROLE_ADMIN"}).  The check is performed
     * against all granted authorities in the current authentication object.
     *
     * @param authority the authority string to test for.
     * @return {@code true} if the current authentication is non-null and contains the
     *         given authority; {@code false} otherwise.
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    /**
     * Asserts that the currently authenticated user possesses the specified authority,
     * throwing a {@link ApiException} with HTTP {@code 403 FORBIDDEN} if they do not.
     *
     * <p>Delegates to {@link #hasAuthority(String)} and throws with the provided
     * {@code message} if the check fails. Intended as a concise guard in service methods
     * that need inline permission enforcement beyond what method-security annotations provide.
     *
     * @param authority the required authority string (permission code or prefixed role name).
     * @param message   the error message to include in the {@link ApiException} when the
     *                  authority check fails.
     * @throws ApiException with HTTP {@code 403 FORBIDDEN} if the current user does not
     *                      hold the specified authority.
     */
    public static void requireAuthority(String authority, String message) {
        if (!hasAuthority(authority)) {
            throw new ApiException(HttpStatus.FORBIDDEN, message);
        }
    }
}
