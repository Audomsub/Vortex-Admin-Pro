package com.vortexadmin.service.impl;

import com.vortexadmin.dto.response.NotificationResponse;
import com.vortexadmin.entity.Notification;
import com.vortexadmin.entity.User;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.NotificationRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.NotificationService;
import com.vortexadmin.service.SseEmitterService;
import com.vortexadmin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles in-app notification business logic including retrieval of the current user's
 * notifications, unread count queries, marking notifications as read with ownership
 * verification, and creating notifications with real-time SSE push delivery.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseEmitterService sseEmitterService;

    /**
     * Maps a {@link Notification} entity to a {@link NotificationResponse} DTO.
     *
     * @param notification the notification entity to map
     * @return the corresponding notification response DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    /**
     * Returns the current user's 200 most recent notifications ordered by creation time
     * descending.
     *
     * @return a list of up to 200 notification response DTOs for the current user
     * @throws ApiException with {@code 404} if the current user record does not exist
     */
    @Override
    public List<NotificationResponse> getMyNotifications() {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, 200)).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns the count of unread notifications for the currently authenticated user.
     *
     * @return the number of unread notifications belonging to the current user
     * @throws ApiException with {@code 404} if the current user record does not exist
     */
    @Override
    public long getUnreadCount() {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Marks a specific notification as read. Verifies that the notification belongs to
     * the currently authenticated user before updating it to prevent cross-user access.
     *
     * @param id the id of the notification to mark as read
     * @throws ApiException with {@code 404} if the notification is not found,
     *                      or {@code 403} if the notification does not belong to the current user
     */
    @Override
    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not your notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Creates a new in-app notification for the specified user and pushes it to any
     * connected SSE clients in real time. The SSE payload includes the full notification
     * DTO and the updated total unread count so the client bell icon updates immediately
     * without requiring a separate API call.
     *
     * @param userId  the id of the user to notify
     * @param title   the notification title displayed in the bell dropdown
     * @param message the full notification message body
     * @throws ApiException with {@code 404} if the target user does not exist
     */
    @Override
    @Transactional
    public void createNotification(Long userId, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .build();
        notificationRepository.save(notification);

        // Push to connected SSE clients so the bell updates in real time
        long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);
        sseEmitterService.sendToUser(userId, "notification", java.util.Map.of(
                "notification", mapToResponse(notification),
                "unreadCount", unreadCount));
    }
}
