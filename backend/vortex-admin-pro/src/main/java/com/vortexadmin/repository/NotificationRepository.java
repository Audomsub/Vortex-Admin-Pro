package com.vortexadmin.repository;

import com.vortexadmin.entity.Notification;
import com.vortexadmin.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Notification} entities, providing standard CRUD operations
 * and custom methods for user-scoped notification retrieval and unread-count aggregation.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Returns a paginated list of notifications for the specified user, ordered by creation date
     * descending so the most recent notifications appear first.
     *
     * @param user     the recipient user
     * @param pageable the pagination parameters
     * @return a list of notifications for the given user, newest first
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Counts unread notifications (where {@code isRead} is {@code false}) for the specified user.
     *
     * @param user the recipient user
     * @return the number of unread notifications belonging to the user
     */
    long countByUserAndIsReadFalse(User user);

    /**
     * Counts notifications for the specified user whose {@code createdAt} falls within the given
     * time window.  Used in dashboard analytics to measure notification activity over a period.
     *
     * @param user  the recipient user
     * @param start the inclusive start of the time range
     * @param end   the inclusive end of the time range
     * @return the number of notifications sent to the user within the specified interval
     */
    long countByUserAndCreatedAtBetween(User user, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
