package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.NotificationResponse;
import com.vortexadmin.service.NotificationService;
import com.vortexadmin.service.SseEmitterService;
import com.vortexadmin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Handles HTTP requests for the notification system, supporting real-time Server-Sent Events
 * streaming and standard notification CRUD operations, delegating to NotificationService and SseEmitterService.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    /**
     * Opens a Server-Sent Events (SSE) stream for the authenticated user to receive real-time notifications.
     *
     * @return an {@link SseEmitter} that pushes notification events to the connected client
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('notification.read')")
    public SseEmitter streamNotifications() {
        return sseEmitterService.subscribe(SecurityUtils.getCurrentUserId());
    }

    /**
     * Retrieves all notifications for the authenticated user.
     *
     * @return a list of {@link NotificationResponse} objects representing the user's notifications
     */
    @GetMapping
    @PreAuthorize("hasAuthority('notification.read')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched", notificationService.getMyNotifications()));
    }

    /**
     * Returns the count of unread notifications for the authenticated user.
     *
     * @return the number of unread notifications as a {@code Long}
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('notification.read')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success("Unread count fetched", notificationService.getUnreadCount()));
    }

    /**
     * Marks a specific notification as read by its unique identifier.
     *
     * @param id the unique ID of the notification to mark as read
     * @return a success response with no data payload upon successful update
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAuthority('notification.read')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }
}
