package com.vortexadmin.repository;

import com.vortexadmin.entity.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTeamId(Long teamId);
    List<Task> findByAssignedToId(Long userId);
    List<Task> findByCreatedAtAfter(java.time.LocalDateTime cutoff);
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Task> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
