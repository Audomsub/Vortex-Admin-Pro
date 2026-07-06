package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.TaskRequest;
import com.vortexadmin.dto.response.TaskResponse;
import com.vortexadmin.entity.Task;
import com.vortexadmin.entity.Team;
import com.vortexadmin.entity.User;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.TaskCommentRepository;
import com.vortexadmin.repository.TaskRepository;
import com.vortexadmin.repository.TeamRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.NotificationService;
import com.vortexadmin.service.TaskService;
import com.vortexadmin.service.WebhookService;
import com.vortexadmin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles task management business logic including ownership-scoped access control,
 * task creation with assignee notifications, status-change webhook events, and
 * cascading comment deletion on task removal.
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final int MAX_TASKS = 1000;

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final WebhookService webhookService;
    private final NotificationService notificationService;

    /**
     * Builds the webhook event payload for a task, including its id, title, status,
     * priority, assigned username, and team name.
     *
     * @param task the task entity to serialize into a payload map
     * @return a map containing the task's key fields for webhook delivery
     */
    private Map<String, Object> taskEventPayload(Task task) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", task.getId());
        payload.put("title", task.getTitle());
        payload.put("status", task.getStatus());
        payload.put("priority", task.getPriority());
        payload.put("assignedTo", task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null);
        payload.put("team", task.getTeam() != null ? task.getTeam().getName() : null);
        return payload;
    }

    /**
     * Maps a {@link Task} entity to a {@link TaskResponse} DTO, including nullable
     * assignee and team references.
     *
     * @param task the task entity to map
     * @return the corresponding task response DTO
     */
    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                .assignedToUsername(task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null)
                .teamId(task.getTeam() != null ? task.getTeam().getId() : null)
                .teamName(task.getTeam() != null ? task.getTeam().getName() : null)
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    /**
     * Checks whether the given task is assigned to the currently authenticated user.
     *
     * @param task the task to check
     * @return {@code true} if the task's assignee matches the current user; {@code false} otherwise
     */
    private boolean isAssignedToCurrentUser(Task task) {
        return task.getAssignedTo() != null
                && task.getAssignedTo().getId().equals(SecurityUtils.getCurrentUserId());
    }

    /**
     * Loads a task by id and enforces ownership-scoped access control.
     * Callers that hold the {@code fullAuthority} permission may access any task;
     * callers with only the {@code .own} variant are restricted to tasks assigned to them.
     * This method is used internally for read, update, and delete operations.
     *
     * @param id            the id of the task to load
     * @param fullAuthority the full (non-scoped) authority string (e.g. {@code task.read})
     * @return the {@link Task} entity if the caller has access
     * @throws ApiException with {@code 404} if the task does not exist,
     *                      or {@code 403} if the caller does not have access
     */
    private Task getTaskCheckingAccess(Long id, String fullAuthority) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        if (!SecurityUtils.hasAuthority(fullAuthority) && !isAssignedToCurrentUser(task)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only access tasks assigned to you");
        }
        return task;
    }

    /**
     * Returns up to {@value #MAX_TASKS} tasks ordered by creation date descending,
     * using a single join query to eagerly load assignee and team references.
     *
     * @return a list of task response DTOs
     */
    @Override
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAllWithDetailsOrderByCreatedAtDesc(PageRequest.of(0, MAX_TASKS)).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns all tasks belonging to a specific team. Callers that hold the full
     * {@code task.read} authority see all tasks in the team; callers with only the
     * own-scoped variant see only tasks assigned to themselves within the team.
     *
     * @param teamId the id of the team whose tasks are requested
     * @return a list of task response DTOs visible to the current caller
     */
    @Override
    public List<TaskResponse> getTasksByTeam(Long teamId) {
        // No team-membership model exists, so own-only callers see just their own tasks in the team
        boolean hasFullRead = SecurityUtils.hasAuthority("task.read");
        return taskRepository.findByTeamId(teamId).stream()
                .filter(task -> hasFullRead || isAssignedToCurrentUser(task))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns all tasks assigned to a specific user. Callers without the full
     * {@code task.read} authority may only retrieve tasks assigned to themselves.
     *
     * @param userId the id of the user whose tasks are requested
     * @return a list of task response DTOs assigned to the specified user
     * @throws ApiException with {@code 403} if the caller is requesting tasks for
     *                      another user without the {@code task.read} authority
     */
    @Override
    public List<TaskResponse> getTasksByAssignee(Long userId) {
        if (!SecurityUtils.hasAuthority("task.read") && !userId.equals(SecurityUtils.getCurrentUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        return taskRepository.findByAssignedToId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single task by id, enforcing ownership-scoped access control.
     *
     * @param id the id of the task to retrieve
     * @return the task response DTO for the requested task
     * @throws ApiException with {@code 404} if the task does not exist,
     *                      or {@code 403} if the caller does not have read access
     */
    @Override
    public TaskResponse getTaskById(Long id) {
        return mapToResponse(getTaskCheckingAccess(id, "task.read"));
    }

    /**
     * Creates a new task with the given details, optional assignee, and optional team.
     * Fires a {@code task.created} webhook after persisting the task. If an assignee is
     * specified, a notification is created for them; notification failures are silently
     * suppressed so they cannot block the task creation flow.
     *
     * @param request the task creation payload (title, description, status, priority, assignee id, team id, due date)
     * @return the persisted task as a {@link TaskResponse} DTO
     * @throws ApiException with {@code 404} if the specified assignee or team does not exist
     */
    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        User assignee = null;
        if (request.getAssignedTo() != null) {
            assignee = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignee not found"));
        }

        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "TODO")
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .assignedTo(assignee)
                .team(team)
                .dueDate(request.getDueDate())
                .build();

        Task saved = taskRepository.save(task);
        webhookService.triggerEvent("task.created", taskEventPayload(saved));

        if (assignee != null) {
            try {
                notificationService.createNotification(
                    assignee.getId(),
                    "New Task Assigned",
                    "You have been assigned the task: " + saved.getTitle()
                );
            } catch (Exception e) {
                // ignore notification failure to prevent blocking task flow
            }
        }

        return mapToResponse(saved);
    }

    /**
     * Updates an existing task's fields, assignee, and team, enforcing ownership-scoped
     * access. After saving, fires {@code task.updated} and, if the status transitioned to
     * DONE, an additional {@code task.completed} webhook. Sends an in-app notification to
     * the (new) assignee: "New Task Assigned" when the assignee changes, or "Task Updated"
     * when the assignee remains the same. Notification failures are silently suppressed.
     *
     * @param id      the id of the task to update
     * @param request the update payload (title, description, status, priority, assignee id, team id, due date)
     * @throws ApiException with {@code 404} if the task, assignee, or team does not exist,
     *                      or {@code 403} if the caller does not have update access
     */
    @Override
    @Transactional
    public void updateTask(Long id, TaskRequest request) {
        Task task = getTaskCheckingAccess(id, "task.update");

        String previousStatus = task.getStatus();
        User previousAssignee = task.getAssignedTo();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        User newAssignee = null;
        if (request.getAssignedTo() != null) {
            newAssignee = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignee not found"));
            task.setAssignedTo(newAssignee);
        } else {
            task.setAssignedTo(null);
        }

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
            task.setTeam(team);
        } else {
            task.setTeam(null);
        }

        taskRepository.save(task);

        webhookService.triggerEvent("task.updated", taskEventPayload(task));
        if ("DONE".equalsIgnoreCase(task.getStatus()) && !"DONE".equalsIgnoreCase(previousStatus)) {
            webhookService.triggerEvent("task.completed", taskEventPayload(task));
        }

        if (newAssignee != null) {
            try {
                if (previousAssignee == null || !previousAssignee.getId().equals(newAssignee.getId())) {
                    notificationService.createNotification(
                        newAssignee.getId(),
                        "New Task Assigned",
                        "You have been assigned the task: " + task.getTitle()
                    );
                } else {
                    notificationService.createNotification(
                        newAssignee.getId(),
                        "Task Updated",
                        "The task '" + task.getTitle() + "' assigned to you has been updated."
                    );
                }
            } catch (Exception e) {
                // ignore notification failure
            }
        }
    }

    /**
     * Deletes a task and all its associated comments, enforcing ownership-scoped access.
     * Comments are deleted first to satisfy foreign key constraints before the task itself
     * is removed.
     *
     * @param id the id of the task to delete
     * @throws ApiException with {@code 404} if the task does not exist,
     *                      or {@code 403} if the caller does not have delete access
     */
    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = getTaskCheckingAccess(id, "task.delete");
        taskCommentRepository.deleteByTaskId(id);
        taskRepository.delete(task);
    }
}
