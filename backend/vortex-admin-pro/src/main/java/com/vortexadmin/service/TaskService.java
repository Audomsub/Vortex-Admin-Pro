package com.vortexadmin.service;

import com.vortexadmin.dto.request.TaskCommentRequest;
import com.vortexadmin.dto.request.TaskRequest;
import com.vortexadmin.dto.response.TaskCommentResponse;
import com.vortexadmin.dto.response.TaskResponse;

import java.util.List;

public interface TaskService {
    List<TaskResponse> getAllTasks();
    List<TaskResponse> getTasksByTeam(Long teamId);
    List<TaskResponse> getTasksByAssignee(Long userId);
    TaskResponse getTaskById(Long id);
    TaskResponse createTask(TaskRequest request);
    void updateTask(Long id, TaskRequest request);
    void deleteTask(Long id);
    
    // Comments
    List<TaskCommentResponse> getCommentsByTask(Long taskId);
    TaskCommentResponse addComment(Long taskId, TaskCommentRequest request);
}
