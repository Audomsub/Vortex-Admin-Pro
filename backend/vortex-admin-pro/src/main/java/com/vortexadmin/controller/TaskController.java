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

/**
 * Handles HTTP requests for task management, including CRUD operations and
 * filtering by team or assignee, delegating business logic to TaskService.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Retrieves all tasks visible to the authenticated user within their tenant.
     *
     * @return a list of {@link TaskResponse} objects for all accessible tasks
     */
    @GetMapping
    @PreAuthorize("hasAuthority('task.read')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllTasks() {
        return ResponseEntity.ok(ApiResponse.success("Tasks fetched successfully", taskService.getAllTasks()));
    }

    /**
     * Retrieves all tasks belonging to a specific team.
     *
     * @param teamId the unique ID of the team whose tasks are being requested
     * @return a list of {@link TaskResponse} objects assigned to the specified team
     */
    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasAuthority('task.read') or hasAuthority('task.read.team')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success("Team tasks fetched", taskService.getTasksByTeam(teamId)));
    }

    /**
     * Retrieves all tasks assigned to a specific user.
     *
     * @param userId the unique ID of the user whose assigned tasks are being requested
     * @return a list of {@link TaskResponse} objects assigned to the specified user
     */
    @GetMapping("/assignee/{userId}")
    @PreAuthorize("hasAuthority('task.read') or hasAuthority('task.read.own')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByAssignee(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Assigned tasks fetched", taskService.getTasksByAssignee(userId)));
    }

    /**
     * Retrieves a single task by its unique identifier.
     *
     * @param id the unique ID of the task to retrieve
     * @return the {@link TaskResponse} for the specified task
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('task.read') or hasAuthority('task.read.own')")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Task fetched successfully", taskService.getTaskById(id)));
    }

    /**
     * Creates a new task with the provided details.
     *
     * @param request the task creation payload containing title, description, assignee, due date, etc.
     * @return the created {@link TaskResponse} reflecting the persisted task
     */
    @PostMapping
    @PreAuthorize("hasAuthority('task.create')")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task created successfully", taskService.createTask(request)));
    }

    /**
     * Updates an existing task by its unique identifier.
     *
     * @param id      the unique ID of the task to update
     * @param request the update payload containing the fields to change
     * @return a success response with no data payload upon successful update
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('task.update') or hasAuthority('task.update.own')")
    public ResponseEntity<ApiResponse<Void>> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", null));
    }

    /**
     * Deletes a task by its unique identifier.
     *
     * @param id the unique ID of the task to delete
     * @return a success response with no data payload upon successful deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('task.delete') or hasAuthority('task.delete.own')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }
}
