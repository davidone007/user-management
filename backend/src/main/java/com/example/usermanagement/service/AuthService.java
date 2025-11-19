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

/**
 * Service class for authentication and user registration operations.
 * 
 * This service handles the core authentication logic:
 * 
 *   User login with credential validation
 *   User registration with password hashing
 *   JWT token generation for authenticated users
 *   Login audit logging
 * 
 * 
 * Security features:
 * 
 *   Password verification using PBKDF2 with user-specific salts
 *   JWT token generation with username and role claims
 *   IP address tracking for audit logs
 *   Last login timestamp updates
 * 
 * 
 * Error handling:
 * 
 *   Throws {@link IllegalArgumentException} with user-facing messages (Spanish) on validation failures
 *   Generic "Credenciales inválidas" message for invalid credentials (prevents username enumeration)
 * 
 * 
 * @author User Management System
 * @version 1.0
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final LoginAuditRepository auditRepo;

    /**
     * Constructs a new AuthService.
     * 
     * @param userRepository The repository for user data access
     * @param jwtUtil The utility for JWT token generation
     * @param auditRepo The repository for login audit records
     */
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, LoginAuditRepository auditRepo) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.auditRepo = auditRepo;
    }

    /**
     * Authenticates a user and generates a JWT access token.
     * 
     * This method:
     * <ol>
     *   Retrieves the user by username
     *   Verifies the password using PBKDF2 with the user's salt
     *   Updates the user's last login timestamp
     *   Creates a login audit record with IP address
     *   Generates a JWT access token with username and role
     *   Returns the token along with forcePasswordReset flag
     * </ol>
     * 
     * If credentials are invalid, throws IllegalArgumentException with
     * "Credenciales inválidas" message (generic to prevent username enumeration).
     * 
     * @param req The authentication request containing username and password
     * @param ip The IP address from which the login originated (for audit logging)
     * @return AuthResponse containing the JWT token and forcePasswordReset flag
     * @throws IllegalArgumentException If the user doesn't exist or password is incorrect
     */
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

    /**
     * Registers a new user in the system.
     * 
     * This method:
     * <ol>
     *   Checks if the username is already taken
     *   Generates a unique salt for the password
     *   Hashes the password using PBKDF2 with the generated salt
     *   Creates a new user with the USER role
     *   Saves the user to the database
     * </ol>
     * 
     * Note: New users are created with the USER role by default.
     * The ADMIN role must be assigned manually or through database scripts.
     * 
     * @param req The registration request containing username and password
     * @throws IllegalArgumentException If the username is already taken
     */
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
     * Creates a new JWT access token for a user by username.
     * 
     * This helper method is used by the token refresh flow to generate a new
     * access token after validating a refresh token. The token includes the
     * user's current role from the database.
     * 
     * @param username The username for which to generate the token
     * @return A new JWT access token string
     * @throws IllegalArgumentException If the user does not exist
     */
    public String createAccessTokenForUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    /**
     * Extracts the username from a JWT access token.
     * 
     * This helper method parses the JWT token and extracts the subject (username).
     * If the token is invalid or cannot be parsed, returns null instead of throwing
     * an exception, allowing for graceful error handling.
     * 
     * @param token The JWT token string
     * @return The username from the token, or null if the token is invalid
     */
    public String getUsernameFromToken(String token) {
        try {
            return jwtUtil.extractUsername(token);
        } catch (Exception ex) { return null; }
    }
}
