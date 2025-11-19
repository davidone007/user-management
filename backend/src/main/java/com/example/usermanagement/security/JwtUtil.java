package com.example.usermanagement.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for JWT (JSON Web Token) operations.
 * 
 * This component handles the creation, parsing, and validation of JWT tokens
 * used for authentication in the application. Tokens are signed using HMAC-SHA
 * with a secret key configured in {@code application.yml}.
 * 
 * Token structure:
 * 
 *   <strong>Subject:</strong> The username of the authenticated user
 *   <strong>Role claim:</strong> The user's role (ADMIN or USER)
 *   <strong>Issued at:</strong> Timestamp when the token was created
 *   <strong>Expiration:</strong> Timestamp when the token expires (configurable, default 5 minutes)
 * 
 * 
 * Security considerations:
 * 
 *   Tokens are short-lived (5 minutes) to minimize exposure if compromised
 *   Refresh tokens are used for obtaining new access tokens without re-authentication
 *   The secret key should be changed in production and kept secure
 * 
 * 
 * @author User Management System
 * @version 1.0
 */
@Component
public class JwtUtil {
    private final Key key;
    private final long expirationMs;

    /**
     * Constructs a new JwtUtil instance.
     * 
     * @param secret The secret key used to sign JWT tokens (from application.yml)
     * @param expirationMs Token expiration time in milliseconds
     */
    public JwtUtil(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a new JWT access token for a user.
     * 
     * @param username The username to include as the token subject
     * @param role The user's role (ADMIN or USER) to include as a claim
     * @return A signed JWT token string
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }

    /**
     * Parses and validates a JWT token.
     * 
     * @param token The JWT token string to parse
     * @return A Jws object containing the token claims if valid
     * @throws io.jsonwebtoken.JwtException If the token is invalid, expired, or tampered with
     */
    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    /**
     * Extracts the username from a JWT token.
     * 
     * @param token The JWT token string
     * @return The username (subject) from the token
     * @throws io.jsonwebtoken.JwtException If the token is invalid
     */
    public String extractUsername(String token) {
        Jws<Claims> j = parse(token);
        return j.getBody().getSubject();
    }
}
