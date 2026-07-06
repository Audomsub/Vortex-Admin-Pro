package com.vortexadmin.controller;

import com.vortexadmin.dto.request.TaskCommentRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.TaskCommentResponse;
import com.vortexadmin.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests for task comment operations, allowing users to retrieve
 * and add comments on tasks, delegating business logic to TaskCommentService.
 */
@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    /**
     * Retrieves all comments for a specific task.
     *
     * @param taskId the unique ID of the task whose comments are being requested
     * @return a list of {@link TaskCommentResponse} objects for the specified task
     */
    @GetMapping
    @PreAuthorize("hasAuthority('task.read') or hasAuthority('task.read.own')")
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getComments(@PathVariable Long taskId) {
        List<TaskCommentResponse> comments = taskCommentService.getCommentsByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.success("Comments fetched successfully", comments));
    }

    /**
     * Adds a new comment to a specific task on behalf of the authenticated user.
     *
     * @param taskId         the unique ID of the task to comment on
     * @param request        the comment payload containing the comment body
     * @param authentication the Spring Security authentication object used to resolve the commenter's username
     * @return the created {@link TaskCommentResponse} reflecting the persisted comment
     */
    @PostMapping
    @PreAuthorize("hasAuthority('task.update') or hasAuthority('task.update.own')")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCommentRequest request,
            Authentication authentication) {

        TaskCommentResponse comment = taskCommentService.addComment(taskId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", comment));
    }
}
