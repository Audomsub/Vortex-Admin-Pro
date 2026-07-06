package com.vortexadmin.repository;

import com.vortexadmin.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link TaskComment} entities, providing standard CRUD operations
 * and custom methods for task-scoped comment retrieval and bulk deletion.
 */
@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    /**
     * Returns all comments belonging to the specified task, ordered chronologically
     * from oldest to newest.
     *
     * @param taskId the primary key of the parent task
     * @return a list of comments for the given task in ascending creation order
     */
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    /**
     * Deletes all comments associated with the specified task.
     * Typically called before deleting the parent task to maintain referential integrity.
     *
     * @param taskId the primary key of the task whose comments should be removed
     */
    void deleteByTaskId(Long taskId);
}
