package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.TaskCommentRequest;
import com.vortexadmin.dto.request.TaskRequest;
import com.vortexadmin.dto.response.TaskCommentResponse;
import com.vortexadmin.dto.response.TaskResponse;
import com.vortexadmin.entity.Task;
import com.vortexadmin.entity.TaskComment;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final WebhookService webhookService;
    private final NotificationService notificationService;

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
    
    private TaskCommentResponse mapCommentToResponse(TaskComment comment) {
        return TaskCommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTask().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .avatarUrl(comment.getUser().getAvatarUrl())
                .comment(comment.getComment())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Override
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getTasksByTeam(Long teamId) {
        return taskRepository.findByTeamId(teamId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getTasksByAssignee(Long userId) {
        return taskRepository.findByAssignedToId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        return mapToResponse(task);
    }

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

    @Override
    @Transactional
    public void updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));

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

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        taskRepository.delete(task);
    }

    @Override
    public List<TaskCommentResponse> getCommentsByTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Task not found");
        }
        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(this::mapCommentToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskCommentResponse addComment(Long taskId, TaskCommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
                
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not authenticated"));

        TaskComment comment = TaskComment.builder()
                .task(task)
                .user(currentUser)
                .comment(request.getComment())
                .build();
                
        return mapCommentToResponse(taskCommentRepository.save(comment));
    }
}
