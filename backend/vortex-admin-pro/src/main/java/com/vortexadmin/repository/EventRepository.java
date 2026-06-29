package com.vortexadmin.repository;

import com.vortexadmin.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.attendees a " +
           "WHERE e.createdBy.id = :userId OR a.id = :userId " +
           "ORDER BY e.startDate ASC")
    List<Event> findByCreatedByIdOrAttendeeId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
