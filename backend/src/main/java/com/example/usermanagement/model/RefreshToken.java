package com.example.usermanagement.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity class representing a refresh token for JWT token rotation.
 * 
 * Refresh tokens are long-lived tokens (30 days) stored in the database that allow
 * users to obtain new access tokens without re-authenticating. This implements a token
 * rotation strategy for enhanced security.
 * 
 * Key characteristics:
 * 
 *   Tokens are cryptographically secure random strings (64 bytes, Base64 URL-encoded)
 *   Each token is associated with a username
 *   Tokens have an expiration date (30 days from creation)
 *   Tokens are rotated (deleted and recreated) on each refresh to prevent reuse
 * 
 * 
 * This entity is managed by the {@link com.example.usermanagement.service.RefreshTokenService}.
 * 
 * @author User Management System
 * @version 1.0
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private String username;
    private OffsetDateTime expiresAt;

    public Long getId() { return id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
}
