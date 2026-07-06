package com.vortexadmin.repository;

import com.vortexadmin.entity.UserSession;
import com.vortexadmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserSession} entities, providing standard CRUD operations
 * and custom methods for session lookup, active-session detection, and geographic aggregation.
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * Returns all sessions associated with the specified user.
     *
     * @param user the owner of the sessions
     * @return a list of sessions belonging to the given user
     */
    List<UserSession> findByUser(User user);

    /**
     * Returns all sessions for the specified user, ordered by login timestamp descending
     * so the most recent sessions appear first.
     *
     * @param user the owner of the sessions
     * @return a list of the user's sessions, newest first
     */
    List<UserSession> findByUserOrderByLoginAtDesc(User user);

    /**
     * Finds the user's most recent session that has not yet been logged out
     * (i.e., {@code logoutAt} is null), ordered by login timestamp descending.
     * Used to retrieve the currently active session for a user.
     *
     * @param user the owner of the session
     * @return an {@link Optional} containing the active session if one exists, otherwise empty
     */
    Optional<UserSession> findFirstByUserAndLogoutAtIsNullOrderByLoginAtDesc(User user);

    /**
     * Finds a session by its primary key and owning user, ensuring that a user can only
     * access their own sessions.
     *
     * @param id   the primary key of the session record
     * @param user the expected owner of the session
     * @return an {@link Optional} containing the matching session, or empty if not found
     *         or if the session belongs to a different user
     */
    Optional<UserSession> findByIdAndUser(Long id, User user);

    /**
     * Returns all sessions whose {@code loginAt} timestamp is after the specified cutoff.
     * Used in analytics to count recent login activity.
     *
     * @param cutoff the exclusive lower boundary for the login timestamp
     * @return a list of sessions that started strictly after {@code cutoff}
     */
    List<UserSession> findByLoginAtAfter(java.time.LocalDateTime cutoff);

    /**
     * Aggregates sessions by country, returning each non-null country value together with its
     * session count, ordered by count descending.  Used to render geographic login statistics.
     *
     * @return a list of {@code Object[]} pairs where index 0 is the country name ({@link String})
     *         and index 1 is the session count ({@link Long})
     */
    @Query("SELECT s.country, COUNT(s) FROM UserSession s WHERE s.country IS NOT NULL GROUP BY s.country ORDER BY COUNT(s) DESC")
    List<Object[]> countByCountry();
}
