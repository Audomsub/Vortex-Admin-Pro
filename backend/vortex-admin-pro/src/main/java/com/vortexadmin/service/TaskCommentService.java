package com.vortexadmin.service;

import com.vortexadmin.dto.request.TaskCommentRequest;
import com.vortexadmin.dto.response.TaskCommentResponse;

import java.util.List;

/**
 * Service contract for task comment operations, providing retrieval and creation of comments
 * on tasks within the project management module.
 */
public interface TaskCommentService {

    /**
     * Returns all comments for the specified task in chronological order (oldest first).
     *
     * @param taskId the primary key of the parent task
     * @return a list of comment responses for the given task
     */
    List<TaskCommentResponse> getCommentsByTaskId(Long taskId);

    /**
     * Adds a new comment to the specified task on behalf of the given user.
     *
     * @param taskId   the primary key of the task being commented on
     * @param request  the comment content
     * @param username the username of the user posting the comment
     * @return the newly created comment response
     * @throws com.vortexadmin.exception.ApiException if the task or user is not found
     */
    TaskCommentResponse addComment(Long taskId, TaskCommentRequest request, String username);
}
