package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.TaskCommentRequest;
import com.vortexadmin.dto.response.TaskCommentResponse;
import com.vortexadmin.entity.Task;
import com.vortexadmin.entity.TaskComment;
import com.vortexadmin.entity.User;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.TaskCommentRepository;
import com.vortexadmin.repository.TaskRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.TaskCommentService;
import com.vortexadmin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles task comment business logic including ownership-scoped access control,
 * comment retrieval in chronological order, and comment creation restricted to
 * users who have read or update access on the parent task.
 */
@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Maps a {@link TaskComment} entity to a {@link TaskCommentResponse} DTO,
     * including the author's user id and username.
     *
     * @param comment the comment entity to map
     * @return the corresponding comment response DTO
     */
    private TaskCommentResponse mapToResponse(TaskComment comment) {
        return TaskCommentResponse.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    /**
     * Loads a task by id and enforces ownership-scoped access control.
     * Callers holding the {@code fullAuthority} permission may access any task;
     * callers with only the {@code .own} variant are restricted to tasks assigned to them.
     *
     * @param taskId        the id of the task to load and validate access for
     * @param fullAuthority the full (non-scoped) authority string (e.g. {@code task.read})
     * @return the {@link Task} entity if the caller has access
     * @throws ApiException with {@code 404} if the task does not exist,
     *                      or {@code 403} if the caller does not have access
     */
    private Task getTaskCheckingAccess(Long taskId, String fullAuthority) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        boolean assignedToMe = task.getAssignedTo() != null
                && task.getAssignedTo().getId().equals(SecurityUtils.getCurrentUserId());
        if (!SecurityUtils.hasAuthority(fullAuthority) && !assignedToMe) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only access tasks assigned to you");
        }
        return task;
    }

    /**
     * Returns all comments for a task in ascending chronological order.
     * Access is checked first: the caller must either hold {@code task.read} or
     * be the assignee of the task.
     *
     * @param taskId the id of the task whose comments are requested
     * @return a list of comment response DTOs ordered from oldest to newest
     * @throws ApiException with {@code 404} if the task does not exist,
     *                      or {@code 403} if the caller does not have read access
     */
    @Override
    public List<TaskCommentResponse> getCommentsByTaskId(Long taskId) {
        getTaskCheckingAccess(taskId, "task.read");
        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Adds a new comment to a task on behalf of the authenticated user identified by
     * {@code username}. The task is access-checked using the {@code task.update} authority
     * before the comment is persisted, ensuring only authorised users can post.
     *
     * @param taskId   the id of the task to comment on
     * @param request  the comment request containing the comment text
     * @param username the username of the authenticated author posting the comment
     * @return the persisted comment as a {@link TaskCommentResponse} DTO
     * @throws ApiException with {@code 404} if the task or user is not found,
     *                      or {@code 403} if the caller does not have update access on the task
     */
    @Override
    public TaskCommentResponse addComment(Long taskId, TaskCommentRequest request, String username) {
        Task task = getTaskCheckingAccess(taskId, "task.update");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        TaskComment comment = TaskComment.builder()
                .task(task)
                .user(user)
                .comment(request.getComment())
                .build();

        return mapToResponse(taskCommentRepository.save(comment));
    }
}
