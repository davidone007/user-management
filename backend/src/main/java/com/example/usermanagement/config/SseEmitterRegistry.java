package com.example.usermanagement.config;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Registry for managing Server-Sent Events (SSE) emitters.
 * 
 * This component maintains a thread-safe collection of active SSE connections
 * used for real-time notifications to admin clients. When user-related events occur
 * (e.g., user deletion, password reset), all connected admin clients are notified
 * to refresh their user lists.
 * 
 * Features:
 * 
 *   Thread-safe storage using {@link CopyOnWriteArraySet}
 *   Automatic cleanup when emitters complete or timeout
 *   Used by {@link com.example.usermanagement.controller.AdminController} for event broadcasting
 * 
 * 
 * SSE connections are established through the {@code /api/admin/events} endpoint
 * and remain open until the client disconnects or the connection times out.
 * 
 * @author User Management System
 * @version 1.0
 */
@Component
public class SseEmitterRegistry {
    /** Thread-safe set of active SSE emitters */
    private final Set<SseEmitter> emitters = new CopyOnWriteArraySet<>();

    /**
     * Creates a new SSE emitter and registers it for event broadcasting.
     * 
     * The emitter is automatically removed from the registry when it completes
     * or times out, ensuring no memory leaks from stale connections.
     * 
     * @return A new SseEmitter instance registered for notifications
     */
    public SseEmitter create() {
        SseEmitter e = new SseEmitter(0L);
        emitters.add(e);
        e.onCompletion(() -> emitters.remove(e));
        e.onTimeout(() -> emitters.remove(e));
        return e;
    }

    /**
     * Returns all active SSE emitters.
     * 
     * @return A set of all currently registered emitters
     */
    public Set<SseEmitter> getEmitters() { return emitters; }
}
