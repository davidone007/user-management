package com.example.usermanagement.service;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import com.example.usermanagement.dto.AuthRequest;
import com.example.usermanagement.dto.AuthResponse;
import com.example.usermanagement.dto.RegisterRequest;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.LoginAuditRepository;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.security.JwtUtil;
import com.example.usermanagement.security.Pbkdf2Password;

@Service
/**
 * Authentication service responsible for login, registration and small helpers used by the
 * authentication/refresh flows. Public methods throw IllegalArgumentException with
 * user-facing messages (currently Spanish) when the operation cannot be completed.
 *
 * This class validates credentials using PBKDF2 (via Pbkdf2Password) and issues JWTs
 * using JwtUtil.
 */
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final LoginAuditRepository auditRepo;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, LoginAuditRepository auditRepo) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.auditRepo = auditRepo;
    }

    public AuthResponse login(AuthRequest req, String ip) {
        // Find user and validate credentials. Throws with a user-facing message in Spanish when invalid.
        User user = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
        if (user.getPasswordHash() == null || user.getSalt() == null || !Pbkdf2Password.verify(req.getPassword().toCharArray(), user.getSalt(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        user.setLastLogin(OffsetDateTime.now());
        userRepository.save(user);

        // audit
        com.example.usermanagement.model.LoginAudit la = new com.example.usermanagement.model.LoginAudit();
        la.setUsername(user.getUsername());
        la.setIp(ip);
        la.setTimestamp(OffsetDateTime.now());
        auditRepo.save(la);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        AuthResponse resp = new AuthResponse(token);
        resp.setForcePasswordReset(user.isForcePasswordReset());
        return resp;
    }

    public void register(RegisterRequest req) {
    if (userRepository.existsByUsername(req.getUsername())) throw new IllegalArgumentException("Nombre de usuario en uso");
        User u = new User();
        u.setUsername(req.getUsername());
        String salt = Pbkdf2Password.generateSalt();
        u.setSalt(salt);
        u.setPasswordHash(Pbkdf2Password.hash(req.getPassword().toCharArray(), salt));
        u.setRole(User.Role.USER);
        userRepository.save(u);
    }

    // Helper used by refresh flow to build a new access token for a username
    /**
     * Helper used by refresh flow to build a new access token for a username.
     * Throws an IllegalArgumentException with a Spanish message if the user does not exist.
     */
    public String createAccessTokenForUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    // Extract username from access token (JWT). Returns null if invalid.
    public String getUsernameFromToken(String token) {
        try {
            return jwtUtil.extractUsername(token);
        } catch (Exception ex) { return null; }
    }
}
