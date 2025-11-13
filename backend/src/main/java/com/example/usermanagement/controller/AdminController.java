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

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {
    private final UserRepository userRepository;
    private final SseEmitterRegistry emitterRegistry;

    public AdminController(UserRepository userRepository, SseEmitterRegistry emitterRegistry) {
        this.userRepository = userRepository;
        this.emitterRegistry = emitterRegistry;
    }

    public static class UserDto {
        public Long id;
        public String username;
        public UserDto() {}
        public UserDto(Long id, String username) { this.id = id; this.username = username; }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        List<UserDto> usernames = userRepository.findAll().stream().map(u -> new UserDto(u.getId(), u.getUsername())).collect(Collectors.toList());
        return ResponseEntity.ok(usernames);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        notifyEmitters();
        return ResponseEntity.ok().body(java.util.Collections.singletonMap("deleted", id));
    }

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

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events() {
        return emitterRegistry.create();
    }

    private void notifyEmitters() {
        for (SseEmitter e : emitterRegistry.getEmitters()) {
            try { e.send(SseEmitter.event().name("users-changed").data("refresh")); } catch (Exception ex) { }
        }
    }
}
