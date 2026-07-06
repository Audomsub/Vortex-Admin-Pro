package com.vortexadmin.repository;

import com.vortexadmin.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Team} entities, providing standard CRUD operations
 * and an aggregation method for counting teams created within a given time range.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * Counts teams whose {@code createdAt} timestamp falls within the specified time window.
     * Used in dashboard analytics to measure team-creation velocity over a period.
     *
     * @param start the inclusive start of the time range
     * @param end   the inclusive end of the time range
     * @return the number of teams created within the interval
     */
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
