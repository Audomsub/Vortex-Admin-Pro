package com.vortexadmin.repository;

import com.vortexadmin.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link AuditLog} entities, providing standard CRUD operations
 * and custom queries for paginated log retrieval with eagerly loaded user associations.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Returns a paginated view of all audit log entries sorted by creation date descending.
     *
     * @param pageable the pagination and sorting parameters
     * @return a {@link Page} of audit logs ordered from newest to oldest
     */
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Returns up to 500 audit log entries with the associated {@code user} eagerly loaded
     * via LEFT JOIN FETCH to avoid N+1 queries.  The {@code pageable} parameter is used
     * as a limit hint rather than for offset-based pagination.
     *
     * @param pageable the pagination parameters (used here to apply a row limit)
     * @return a list of up to 500 audit log entries with user data fully loaded, newest first
     */
    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user ORDER BY a.createdAt DESC")
    List<AuditLog> findTop500WithUser(Pageable pageable);

    /**
     * Returns up to 5 audit log entries with the associated {@code user} eagerly loaded
     * via LEFT JOIN FETCH.  Used for recent-activity widgets on the dashboard.
     *
     * @param pageable the pagination parameters (used here to apply a row limit of 5)
     * @return a list of up to 5 audit log entries with user data fully loaded, newest first
     */
    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user ORDER BY a.createdAt DESC")
    List<AuditLog> findTop5WithUser(Pageable pageable);

    /**
     * Returns the 100 most recent audit log entries for a specific user, ordered newest first.
     *
     * @param userId the primary key of the user whose audit trail should be fetched
     * @return a list of up to 100 audit log entries for the given user
     */
    List<AuditLog> findTop100ByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Returns all audit log entries for a specific user, ordered by creation date descending.
     *
     * @param userId the primary key of the user whose complete audit trail is requested
     * @return all audit log entries for the given user, newest first
     */
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
