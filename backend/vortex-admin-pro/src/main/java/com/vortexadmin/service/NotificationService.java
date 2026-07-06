package com.vortexadmin.service;

import com.vortexadmin.dto.response.NotificationResponse;

import java.util.List;

/**
 * Service contract for in-app notification operations including retrieval, unread counting,
 * read-status updates, and programmatic creation of notifications for users.
 */
public interface NotificationService {

    /**
     * Returns the most recent notifications for the currently authenticated user.
     *
     * @return a list of notification responses for the calling user, newest first
     */
    List<NotificationResponse> getMyNotifications();

    /**
     * Returns the number of unread notifications for the currently authenticated user.
     *
     * @return the count of unread notifications
     */
    long getUnreadCount();

    /**
     * Marks the specified notification as read.
     *
     * @param id the primary key of the notification to mark as read
     * @throws com.vortexadmin.exception.ApiException if the notification is not found or does
     *         not belong to the currently authenticated user
     */
    void markAsRead(Long id);

    /**
     * Creates and persists a new in-app notification for the specified user and optionally
     * pushes it to the user's active SSE connection.
     *
     * @param userId  the primary key of the recipient user
     * @param title   the short notification title
     * @param message the full notification message body
     */
    void createNotification(Long userId, String title, String message);
}
