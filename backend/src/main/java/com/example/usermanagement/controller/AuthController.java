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

@RestController
@RequestMapping("/api/auth")
/**
 * REST controller that exposes authentication endpoints like /login, /register, /refresh
 * and password change. Responses include user-facing messages; errors propagate to the
 * GlobalExceptionHandler which returns the exception message as the response body.
 */
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final com.example.usermanagement.service.RefreshTokenService refreshTokenService;
    private final boolean secureCookies;

    public AuthController(AuthService authService, UserRepository userRepository, com.example.usermanagement.service.RefreshTokenService refreshTokenService,
                          @Value("${app.security.secure-cookies:true}") boolean secureCookies) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.secureCookies = secureCookies;
    }

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

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok().build();
    }

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

    @GetMapping("/me/last-login")
    public ResponseEntity<OffsetDateTime> lastLogin(Authentication authentication) {
        String username = authentication.getName();
    User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(user.getLastLogin());
    }

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
