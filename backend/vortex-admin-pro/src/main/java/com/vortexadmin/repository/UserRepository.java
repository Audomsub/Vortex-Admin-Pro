package com.vortexadmin.repository;

import com.vortexadmin.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities, providing standard CRUD operations
 * and custom queries for user lookup, soft-delete filtering, dashboard aggregations, and search.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the matching user, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the matching user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given username exists.
     *
     * @param username the username to check
     * @return {@code true} if a user with that username exists, otherwise {@code false}
     */
    Boolean existsByUsername(String username);

    /**
     * Checks whether a user with the given email address exists.
     *
     * @param email the email address to check
     * @return {@code true} if a user with that email exists, otherwise {@code false}
     */
    Boolean existsByEmail(String email);

    /**
     * Retrieves all users that have not been soft-deleted (i.e., {@code deletedAt} is null).
     *
     * @return a list of active (non-deleted) users
     */
    List<User> findByDeletedAtIsNull();

    /**
     * Finds a non-deleted user by their primary key.
     *
     * @param id the user ID to look up
     * @return an {@link Optional} containing the user if found and not deleted, otherwise empty
     */
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Counts all non-deleted users.
     *
     * @return the total number of active users
     */
    long countByDeletedAtIsNull();

    /**
     * Counts non-deleted users whose status matches the given value (case-insensitive).
     *
     * @param status the status string to filter by (e.g., "Active", "Inactive")
     * @return the number of matching active users
     */
    long countByStatusIgnoreCaseAndDeletedAtIsNull(String status);

    /**
     * Counts non-deleted users whose {@code createdAt} timestamp is on or before the given end time.
     * Used for point-in-time user totals in dashboard analytics.
     *
     * @param end the upper boundary timestamp (inclusive)
     * @return the count of non-deleted users created up to and including {@code end}
     */
    long countByDeletedAtIsNullAndCreatedAtLessThanEqual(LocalDateTime end);

    /**
     * Counts non-deleted users with the given status whose {@code createdAt} is on or before the given end time.
     *
     * @param status the status string to filter by (case-insensitive)
     * @param end    the upper boundary timestamp (inclusive)
     * @return the count of matching non-deleted users
     */
    long countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtLessThanEqual(String status, LocalDateTime end);

    /**
     * Counts non-deleted users created within the given time window.
     *
     * @param start the inclusive start of the time range
     * @param end   the inclusive end of the time range
     * @return the number of non-deleted users created in the specified interval
     */
    long countByDeletedAtIsNullAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Counts non-deleted users with the given status created within the given time window.
     *
     * @param status the status string to filter by (case-insensitive)
     * @param start  the inclusive start of the time range
     * @param end    the inclusive end of the time range
     * @return the number of matching non-deleted users in the specified interval
     */
    long countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);

    /**
     * Finds a non-deleted user by username, eagerly loading the associated {@code role} and its
     * {@code permissions} to avoid lazy-loading in security filters.
     *
     * @param username the username to look up
     * @return an {@link Optional} containing the fully-loaded user, or empty if not found or deleted
     */
    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    /**
     * Returns the four most recently created non-deleted users, ordered by creation date descending.
     * Used on the dashboard to display new-user activity.
     *
     * @return a list of up to four recent active users
     */
    List<User> findTop4ByDeletedAtIsNullOrderByCreatedAtDesc();

    /**
     * Aggregates non-deleted users by their assigned role, returning each role name alongside
     * its user count.  Uses a LEFT JOIN so users without a role appear under the label "Unknown".
     *
     * @return a list of {@code Object[]} pairs where index 0 is the role name ({@link String})
     *         and index 1 is the user count ({@link Long})
     */
    @Query("SELECT COALESCE(r.name, 'Unknown'), COUNT(u) FROM User u LEFT JOIN u.role r WHERE u.deletedAt IS NULL GROUP BY r.name")
    List<Object[]> countUsersByRole();

    /**
     * Performs a case-insensitive keyword search across {@code username}, {@code email},
     * {@code firstName}, and {@code lastName} fields, returning only non-deleted users.
     *
     * @param q the search keyword
     * @return a list of matching non-deleted users
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<User> searchByKeyword(@org.springframework.data.repository.query.Param("q") String q);
}
