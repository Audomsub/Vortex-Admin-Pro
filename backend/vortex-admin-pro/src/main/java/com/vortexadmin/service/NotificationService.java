package com.vortexadmin.service;

import com.vortexadmin.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMyNotifications();
    long getUnreadCount();
    void markAsRead(Long id);
    void createNotification(Long userId, String title, String message);
}
