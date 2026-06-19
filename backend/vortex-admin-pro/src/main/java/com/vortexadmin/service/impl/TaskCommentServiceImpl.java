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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public List<TaskCommentResponse> getCommentsByTaskId(Long taskId) {
        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream().map(c ->
            TaskCommentResponse.builder()
                .id(c.getId())
                .comment(c.getComment())
                .userId(c.getUser().getId())
                .username(c.getUser().getUsername())
                .createdAt(c.getCreatedAt())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public TaskCommentResponse addComment(Long taskId, TaskCommentRequest request, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        TaskComment comment = TaskComment.builder()
                .task(task)
                .user(user)
                .comment(request.getComment())
                .build();

        comment = taskCommentRepository.save(comment);

        return TaskCommentResponse.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
