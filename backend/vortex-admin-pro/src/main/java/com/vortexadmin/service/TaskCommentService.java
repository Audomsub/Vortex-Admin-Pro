package com.vortexadmin.service;

import com.vortexadmin.dto.request.TaskCommentRequest;
import com.vortexadmin.dto.response.TaskCommentResponse;

import java.util.List;

public interface TaskCommentService {
    List<TaskCommentResponse> getCommentsByTaskId(Long taskId);
    TaskCommentResponse addComment(Long taskId, TaskCommentRequest request, String username);
}
