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

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('notification.read')")
    public SseEmitter streamNotifications() {
        return sseEmitterService.subscribe(SecurityUtils.getCurrentUserId());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('notification.read')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched", notificationService.getMyNotifications()));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('notification.read')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success("Unread count fetched", notificationService.getUnreadCount()));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAuthority('notification.read')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }
}
