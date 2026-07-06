package com.vortexadmin.repository;

import com.vortexadmin.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Event} entities, providing standard CRUD operations
 * and custom queries for date-range counting and user-scoped event retrieval including attendee
 * membership.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Counts events whose {@code createdAt} timestamp falls within the specified time window.
     * Used in dashboard analytics to measure event-creation activity over a period.
     *
     * @param start the inclusive start of the time range
     * @param end   the inclusive end of the time range
     * @return the number of events created in the specified interval
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Returns all events that are either created by the specified user or have the user listed
     * as an attendee.  Uses a LEFT JOIN on the {@code attendees} collection and DISTINCT to
     * avoid duplicate results when a user appears in both roles.  Results are sorted by
     * {@code startDate} ascending.
     *
     * @param userId the primary key of the user (as creator or attendee)
     * @return a deduplicated list of events relevant to the given user, ordered by start date
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.attendees a " +
           "WHERE e.createdBy.id = :userId OR a.id = :userId " +
           "ORDER BY e.startDate ASC")
    List<Event> findByCreatedByIdOrAttendeeId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
