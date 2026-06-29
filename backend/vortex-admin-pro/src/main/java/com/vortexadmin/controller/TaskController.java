package com.vortexadmin.controller;

import com.vortexadmin.dto.request.TaskCommentRequest;
import com.vortexadmin.dto.request.TaskRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.TaskCommentResponse;
import com.vortexadmin.dto.response.TaskResponse;
import com.vortexadmin.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @PreAuthorize("hasAuthority('task.read')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllTasks() {
        return ResponseEntity.ok(ApiResponse.success("Tasks fetched successfully", taskService.getAllTasks()));
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasAuthority('task.read') or hasAuthority('task.read.team')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success("Team tasks fetched", taskService.getTasksByTeam(teamId)));
    }

    @GetMapping("/assignee/{userId}")
    @PreAuthorize("hasAuthority('task.read') or hasAuthority('task.read.own')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByAssignee(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Assigned tasks fetched", taskService.getTasksByAssignee(userId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('task.read') or hasAuthority('task.read.own')")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Task fetched successfully", taskService.getTaskById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('task.create')")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task created successfully", taskService.createTask(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('task.update') or hasAuthority('task.update.own')")
    public ResponseEntity<ApiResponse<Void>> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('task.delete') or hasAuthority('task.delete.own')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }
}
