package com.example.usermanagement.controller;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.usermanagement.dto.AuthRequest;
import com.example.usermanagement.dto.AuthResponse;
import com.example.usermanagement.dto.ChangePasswordRequest;
import com.example.usermanagement.dto.RegisterRequest;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.security.Pbkdf2Password;
import com.example.usermanagement.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * REST controller for authentication and user account management endpoints.
 * 
 * This controller handles:
 * 
 *   User login and registration
 *   JWT token refresh using refresh tokens
 *   User logout (token revocation)
 *   Password change operations
 *   Retrieval of user's last login timestamp
 * 
 * 
 * Security features:
 * 
 *   Cookies are used for refresh tokens (HttpOnly, Secure, SameSite=Lax)
 *   Access tokens are returned in the response body and also set as cookies
 *   IP address tracking for login audit logs
 *   Password reset enforcement flag handling
 * 
 * 
 * Error handling:
 * 
 *   Validation errors are handled by {@link com.example.usermanagement.config.GlobalExceptionHandler}
 *   User-facing error messages are returned in Spanish
 *   Invalid credentials return generic "Credenciales inválidas" message
 * 
 * 
 * @author User Management System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final com.example.usermanagement.service.RefreshTokenService refreshTokenService;
    private final boolean secureCookies;

    /**
     * Constructs a new AuthController.
     * 
     * @param authService The authentication service for login/registration operations
     * @param userRepository The repository for user data access
     * @param refreshTokenService The service for managing refresh tokens
     * @param secureCookies Whether to use secure cookies (HTTPS only), configured via application.yml
     */
    public AuthController(AuthService authService, UserRepository userRepository, com.example.usermanagement.service.RefreshTokenService refreshTokenService,
                          @Value("${app.security.secure-cookies:true}") boolean secureCookies) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.secureCookies = secureCookies;
    }

    /**
     * Authenticates a user and returns a JWT access token.
     * 
     * This endpoint:
     * <ol>
     *   Validates the user's credentials using PBKDF2 password verification
     *   Generates a JWT access token (short-lived, 5 minutes)
     *   Creates a refresh token and stores it in the database
     *   Sets both tokens as HttpOnly cookies (REMEMBER for access, REFRESH for refresh)
     *   Records the login event in the audit log with IP address
     *   Updates the user's last login timestamp
     * </ol>
     * 
     * If the user has the {@code forcePasswordReset} flag set, the response includes
     * this flag so the frontend can prompt for password change.
     * 
     * @param req The authentication request containing username and password
     * @param request The HTTP request (used to extract IP address for audit)
     * @param response The HTTP response (used to set cookies)
     * @return ResponseEntity containing the JWT access token and forcePasswordReset flag
     * @throws IllegalArgumentException If credentials are invalid (handled by GlobalExceptionHandler)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req, HttpServletRequest request, HttpServletResponse response) {
        // Prefer X-Forwarded-For if present (behind proxies); fall back to remoteAddr
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        ip = com.example.usermanagement.security.SecurityUtil.normalizeIp(ip);
        AuthResponse auth = authService.login(req, ip);
    // always issue refresh token cookie on login
    ResponseCookie accessCookie = ResponseCookie.from("REMEMBER", auth.getToken())
        .httpOnly(true)
        .secure(secureCookies)
        .path("/")
        .maxAge(60L * 60L * 24L * 30L)
        .sameSite("Lax")
        .build();
    response.addHeader("Set-Cookie", accessCookie.toString());

    String refresh = refreshTokenService.createRefreshToken(authService.getUsernameFromToken(auth.getToken()));
    ResponseCookie refreshCookie = ResponseCookie.from("REFRESH", refresh)
        .httpOnly(true)
        .secure(secureCookies)
        .path("/")
        .maxAge(60L * 60L * 24L * 30L)
        .sameSite("Lax")
        .build();
    response.addHeader("Set-Cookie", refreshCookie.toString());
        return ResponseEntity.ok(auth);
    }

    /**
     * Registers a new user in the system.
     * 
     * This endpoint:
     * <ol>
     *   Validates that the username is not already taken
     *   Generates a unique salt for the password
     *   Hashes the password using PBKDF2 with the generated salt
     *   Creates a new user with the USER role
     * </ol>
     * 
     * Note: The new user must log in separately after registration to obtain a JWT token.
     * 
     * @param req The registration request containing username and password
     * @return ResponseEntity with status 200 OK on success
     * @throws IllegalArgumentException If the username is already taken (handled by GlobalExceptionHandler)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok().build();
    }

    /**
     * Refreshes an expired access token using a refresh token.
     * 
     * This endpoint implements token rotation for enhanced security:
     * <ol>
     *   Extracts the refresh token from the REFRESH cookie
     *   Validates the refresh token (checks existence and expiration)
     *   Deletes the old refresh token (rotation)
     *   Generates a new refresh token and access token
     *   Sets the new refresh token as a cookie
     *   Returns the new access token in the response body
     * </ol>
     * 
     * If the refresh token is invalid or expired, returns 401 Unauthorized.
     * 
     * @param request The HTTP request (used to extract the refresh token cookie)
     * @param response The HTTP response (used to set the new refresh token cookie)
     * @return ResponseEntity containing the new JWT access token, or 401 if invalid
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) return ResponseEntity.status(401).build();
        String refreshToken = null;
        for (jakarta.servlet.http.Cookie c : cookies) {
            if ("REFRESH".equals(c.getName())) { refreshToken = c.getValue(); break; }
        }
        if (refreshToken == null) return ResponseEntity.status(401).build();
        try {
            String username = refreshTokenService.validate(refreshToken).orElseThrow(() -> new IllegalArgumentException("Token de refresco inválido"));
            // rotate
            String newRefresh = refreshTokenService.rotateRefreshToken(refreshToken);
            // generate new access token
            String newAccess = authService.createAccessTokenForUser(username);

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH", newRefresh)
            .httpOnly(true)
            .secure(secureCookies)
            .path("/")
            .maxAge(60L * 60L * 24L * 30L)
            .sameSite("Lax")
            .build();
            response.addHeader("Set-Cookie", refreshCookie.toString());

            AuthResponse resp = new AuthResponse(newAccess);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Logs out the current user by revoking refresh tokens and clearing cookies.
     * 
     * This endpoint:
     * <ol>
     *   Extracts the refresh token from the REFRESH cookie
     *   Revokes the refresh token in the database (deletes it)
     *   Clears both REFRESH and REMEMBER cookies by setting maxAge to 0
     * </ol>
     * 
     * Note: Access tokens are stateless JWTs and cannot be revoked individually.
     * They will expire naturally after their short lifetime (5 minutes).
     * 
     * @param request The HTTP request (used to extract cookies)
     * @param response The HTTP response (used to clear cookies)
     * @return ResponseEntity with status 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie c : cookies) {
                if ("REFRESH".equals(c.getName())) {
                    refreshTokenService.revokeToken(c.getValue());
                }
            }
        }
        // clear cookies
    ResponseCookie clearRefresh = ResponseCookie.from("REFRESH", "")
        .httpOnly(true)
        .secure(secureCookies)
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();
        response.addHeader("Set-Cookie", clearRefresh.toString());

        ResponseCookie clearAccess = ResponseCookie.from("REMEMBER", "")
                .httpOnly(true)
                .secure(secureCookies)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", clearAccess.toString());
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves the last login timestamp for the authenticated user.
     * 
     * This endpoint requires authentication (JWT token in Authorization header).
     * The username is extracted from the Spring Security Authentication object.
     * 
     * @param authentication The Spring Security authentication object (injected automatically)
     * @return ResponseEntity containing the last login timestamp, or null if never logged in
     * @throws RuntimeException If the user is not found (should not happen for authenticated users)
     */
    @GetMapping("/me/last-login")
    public ResponseEntity<OffsetDateTime> lastLogin(Authentication authentication) {
        String username = authentication.getName();
    User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(user.getLastLogin());
    }

    /**
     * Changes the password for the authenticated user.
     * 
     * This endpoint:
     * <ol>
     *   Verifies the current password using PBKDF2
     *   Generates a new salt for the user
     *   Hashes the new password with the new salt
     *   Updates the user's password hash and salt in the database
     *   Clears the forcePasswordReset flag
     * </ol>
     * 
     * This endpoint requires authentication (JWT token in Authorization header).
     * 
     * @param authentication The Spring Security authentication object (injected automatically)
     * @param req The password change request containing old and new passwords
     * @return ResponseEntity with status 200 OK on success, or 403 if current password is incorrect
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest req) {
        String username = authentication.getName();
    User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!Pbkdf2Password.verify(req.getOldPassword().toCharArray(), user.getSalt(), user.getPasswordHash())) {
            return ResponseEntity.status(403).body("Contraseña actual incorrecta");
        }
        String salt = Pbkdf2Password.generateSalt();
        user.setSalt(salt);
        user.setPasswordHash(Pbkdf2Password.hash(req.getNewPassword().toCharArray(), salt));
        // Clear the forcePasswordReset flag once the user changes their password
        user.setForcePasswordReset(false);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}
