package com.vortexadmin.controller;

import com.vortexadmin.dto.request.TaskCommentRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.TaskCommentResponse;
import com.vortexadmin.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getComments(@PathVariable Long taskId) {
        List<TaskCommentResponse> comments = taskCommentService.getCommentsByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.success("Comments fetched successfully", comments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCommentRequest request,
            Authentication authentication) {
        
        TaskCommentResponse comment = taskCommentService.addComment(taskId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", comment));
    }
}
