package com.example.usermanagement.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.usermanagement.config.SseEmitterRegistry;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.security.Pbkdf2Password;

/**
 * REST controller for administrative operations.
 * 
 * This controller provides endpoints for user management and real-time notifications.
 * All endpoints require ADMIN role authorization.
 * 
 * Available operations:
 * 
 *   List all users in the system
 *   Delete users by ID
 *   Reset user passwords (generates temporary password)
 *   Subscribe to real-time user change events via Server-Sent Events (SSE)
 * 
 * 
 * Real-time updates:
 * 
 *   When a user is deleted or password is reset, all connected admin clients
 *       are notified via SSE to refresh their user lists
 *   SSE connections are managed by {@link SseEmitterRegistry}
 * 
 * 
 * Security:
 * 
 *   All endpoints require ADMIN role (enforced by {@code @PreAuthorize})
 *   JWT token must be included in Authorization header
 * 
 * 
 * @author User Management System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {
    private final UserRepository userRepository;
    private final SseEmitterRegistry emitterRegistry;

    /**
     * Constructs a new AdminController.
     * 
     * @param userRepository The repository for user data access
     * @param emitterRegistry The registry for managing SSE connections
     */
    public AdminController(UserRepository userRepository, SseEmitterRegistry emitterRegistry) {
        this.userRepository = userRepository;
        this.emitterRegistry = emitterRegistry;
    }

    /**
     * Data Transfer Object for user information in admin endpoints.
     * 
     * This DTO contains only the essential user information needed for
     * the admin panel (ID and username). Sensitive information like password
     * hashes and salts are excluded.
     */
    public static class UserDto {
        public Long id;
        public String username;
        
        /**
         * Default constructor for UserDto.
         */
        public UserDto() {}
        
        /**
         * Constructs a new UserDto with the specified ID and username.
         * 
         * @param id The user's ID
         * @param username The user's username
         */
        public UserDto(Long id, String username) { this.id = id; this.username = username; }
    }

    /**
     * Retrieves a list of all users in the system.
     * 
     * Returns a simplified view of users containing only ID and username.
     * This endpoint is used by the admin panel to display the user list.
     * 
     * @return ResponseEntity containing a list of UserDto objects
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        List<UserDto> usernames = userRepository.findAll().stream().map(u -> new UserDto(u.getId(), u.getUsername())).collect(Collectors.toList());
        return ResponseEntity.ok(usernames);
    }

    /**
     * Deletes a user from the system by ID.
     * 
     * This operation:
     * <ol>
     *   Checks if the user exists
     *   Deletes the user from the database
     *   Notifies all connected admin clients via SSE to refresh their user lists
     * </ol>
     * 
     * Note: This is a permanent operation. Associated refresh tokens and audit logs
     * may remain in the database depending on cascade settings.
     * 
     * @param id The ID of the user to delete
     * @return ResponseEntity with status 200 OK and deleted user ID, or 404 if user not found
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        notifyEmitters();
        return ResponseEntity.ok().body(java.util.Collections.singletonMap("deleted", id));
    }

    /**
     * Resets a user's password to a randomly generated temporary password.
     * 
     * This operation:
     * <ol>
     *   Generates a secure, readable temporary password (12 characters)
     *   Creates a new salt for the user
     *   Hashes the temporary password with PBKDF2
     *   Sets the forcePasswordReset flag to true
     *   Updates the user in the database
     *   Notifies all connected admin clients via SSE
     * </ol>
     * 
     * The temporary password is returned in the response so the admin can
     * communicate it to the user. The user will be forced to change their password
     * on next login.
     * 
     * @param id The ID of the user whose password should be reset
     * @return ResponseEntity containing the temporary password, or 404 if user not found
     */
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable("id") Long id) {
    User u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        String temp = com.example.usermanagement.security.SecurityUtil.generateReadablePassword(12);
        String salt = Pbkdf2Password.generateSalt();
        u.setSalt(salt);
        u.setPasswordHash(Pbkdf2Password.hash(temp.toCharArray(), salt));
        u.setForcePasswordReset(true);
        userRepository.save(u);
        notifyEmitters();
        return ResponseEntity.ok().body(java.util.Collections.singletonMap("tempPassword", temp));
    }

    /**
     * Establishes a Server-Sent Events (SSE) connection for real-time notifications.
     * 
     * This endpoint creates a long-lived HTTP connection that allows the server
     * to push events to the admin client. When user-related changes occur (deletion,
     * password reset), all connected clients receive a "users-changed" event.
     * 
     * The connection remains open until:
     * 
     *   The client closes the connection
     *   The connection times out
     *   The server sends a completion event
     * 
     * 
     * Usage: The frontend should create an EventSource pointing to this endpoint
     * and listen for "users-changed" events to refresh the user list.
     * 
     * @return A new SseEmitter instance registered for event broadcasting
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events() {
        return emitterRegistry.create();
    }

    /**
     * Notifies all connected SSE clients about user list changes.
     * 
     * This private method is called after user deletion or password reset operations
     * to broadcast a "users-changed" event to all connected admin clients. Clients
     * should refresh their user lists upon receiving this event.
     * 
     * Errors during event sending are silently ignored to prevent one failed
     * connection from affecting others.
     */
    private void notifyEmitters() {
        for (SseEmitter e : emitterRegistry.getEmitters()) {
            try { e.send(SseEmitter.event().name("users-changed").data("refresh")); } catch (Exception ex) { }
        }
    }
}
