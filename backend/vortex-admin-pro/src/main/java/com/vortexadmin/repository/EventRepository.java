package com.vortexadmin.repository;

import com.vortexadmin.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
