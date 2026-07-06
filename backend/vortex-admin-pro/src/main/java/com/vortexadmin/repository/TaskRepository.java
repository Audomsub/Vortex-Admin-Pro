package com.vortexadmin.repository;

import com.vortexadmin.entity.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Task} entities, providing standard CRUD operations
 * and custom queries for team/assignee-scoped lookups, creation-date filtering, and
 * paginated retrieval with eagerly loaded associations.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Returns all tasks belonging to the specified team.
     *
     * @param teamId the primary key of the team
     * @return a list of tasks associated with the given team
     */
    List<Task> findByTeamId(Long teamId);

    /**
     * Returns all tasks assigned to the specified user.
     *
     * @param userId the primary key of the assignee user
     * @return a list of tasks assigned to the given user
     */
    List<Task> findByAssignedToId(Long userId);

    /**
     * Returns all tasks created after the given cutoff timestamp.
     *
     * @param cutoff the exclusive lower boundary for the creation timestamp
     * @return a list of tasks created strictly after {@code cutoff}
     */
    List<Task> findByCreatedAtAfter(java.time.LocalDateTime cutoff);

    /**
     * Counts tasks whose {@code createdAt} falls within the given time window.
     *
     * @param start the inclusive start of the time range
     * @param end   the inclusive end of the time range
     * @return the number of tasks created in the specified interval
     */
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    /**
     * Returns a paginated list of all tasks ordered by creation date descending.
     *
     * @param pageable the pagination and sorting parameters
     * @return a list of tasks ordered from newest to oldest, limited by {@code pageable}
     */
    List<Task> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Returns a paginated list of all tasks with their {@code assignedTo} user and {@code team}
     * eagerly fetched via LEFT JOIN FETCH, ordered by creation date descending.
     * This avoids N+1 queries when rendering task lists that display assignee and team names.
     *
     * @param pageable the pagination parameters (used here as a limit hint)
     * @return a list of tasks with associated user and team data fully loaded
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedTo LEFT JOIN FETCH t.team ORDER BY t.createdAt DESC")
    List<Task> findAllWithDetailsOrderByCreatedAtDesc(Pageable pageable);
}
