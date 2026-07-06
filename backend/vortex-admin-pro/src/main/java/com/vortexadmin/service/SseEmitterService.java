package com.vortexadmin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service responsible for managing Server-Sent Events (SSE) connections, enabling real-time
 * push notifications from the server to connected browser clients.  Multiple concurrent
 * connections per user are supported via a thread-safe emitter registry.
 */
@Service
public class SseEmitterService {

    private static final Logger logger = LoggerFactory.getLogger(SseEmitterService.class);
    private static final long TIMEOUT_MS = 30 * 60 * 1000L; // 30 minutes; client auto-reconnects

    private final Map<Long, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    /**
     * Creates and registers a new {@link SseEmitter} for the specified user with a 30-minute
     * timeout.  An initial {@code connected} event is sent immediately to confirm the connection.
     * Completion, timeout, and error callbacks automatically deregister the emitter.
     *
     * @param userId the primary key of the user establishing the SSE connection
     * @return the newly created and registered {@link SseEmitter}
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emittersByUser.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }
        return emitter;
    }

    /**
     * Sends a named SSE event with the provided data to all active connections belonging to
     * the specified user.  Dead emitters that throw on send are automatically removed.
     *
     * @param userId    the primary key of the target user
     * @param eventName the SSE event name (e.g., "notification", "ping")
     * @param data      the event payload; serialised to JSON by the SSE framework
     */
    public void sendToUser(Long userId, String eventName, Object data) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                logger.debug("Removing dead SSE emitter for user {}", userId);
                removeEmitter(userId, emitter);
            }
        }
    }

    /**
     * Broadcasts a named SSE event to all currently connected users.
     * Dead emitters encountered during broadcast are automatically removed.
     *
     * @param eventName the SSE event name
     * @param data      the event payload; serialised to JSON by the SSE framework
     */
    public void broadcast(String eventName, Object data) {
        emittersByUser.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name(eventName).data(data));
                } catch (Exception e) {
                    removeEmitter(userId, emitter);
                }
            }
        });
    }

    /**
     * Sends a {@code ping} heartbeat event to all connected clients every 20 seconds to keep
     * SSE connections alive through proxies and load balancers that close idle HTTP connections.
     * Scheduled automatically by Spring's task scheduler.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 20000)
    public void sendHeartbeat() {
        broadcast("ping", "heartbeat");
    }

    /**
     * Removes the specified emitter from the registry for the given user.  If the user's
     * emitter list becomes empty after removal, the user entry is also removed to avoid
     * memory leaks.
     *
     * @param userId  the primary key of the user whose emitter should be removed
     * @param emitter the specific emitter instance to deregister
     */
    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                emittersByUser.remove(userId);
            }
        }
    }
}
