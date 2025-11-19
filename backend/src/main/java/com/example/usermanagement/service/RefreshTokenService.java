package com.example.usermanagement.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.usermanagement.model.RefreshToken;
import com.example.usermanagement.repository.RefreshTokenRepository;

@Service
/**
 * Service that manages persistent refresh tokens. Responsibilities:
 * - create refresh tokens persisted in the database
 * - validate token existence and expiry
 * - rotate tokens (delete old, create new)
 * - revoke tokens for a single token or for a user
 *
 * Errors use IllegalArgumentException with user-facing messages (Spanish).
 */
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final SecureRandom random = new SecureRandom();
    private final Duration validity = Duration.ofDays(30);

    /**
     * Constructs a new RefreshTokenService.
     * 
     * @param repo The repository for refresh token data access
     */
    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    /**
     * Generates a cryptographically secure random token string.
     * 
     * The token is 64 bytes of random data, Base64 URL-encoded without padding.
     * This produces a URL-safe token string suitable for use in cookies and URLs.
     * 
     * @return A Base64 URL-encoded token string (86 characters)
     */
    private String generateTokenString() {
        byte[] b = new byte[64];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    /**
     * Creates a new refresh token for a user and stores it in the database.
     * 
     * This method:
     * <ol>
     *   Generates a cryptographically secure random token string
     *   Creates a RefreshToken entity with the token, username, and expiration (30 days)
     *   Saves the token to the database
     * </ol>
     * 
     * @param username The username for which to create the refresh token
     * @return The generated refresh token string
     */
    public String createRefreshToken(String username) {
        String token = generateTokenString();
        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setUsername(username);
        rt.setExpiresAt(OffsetDateTime.now().plus(validity));
        repo.save(rt);
        return token;
    }

    /**
     * Rotates a refresh token by deleting the old one and creating a new one for the same user.
     * 
     * Token rotation is a security best practice that prevents token reuse. This method:
     * <ol>
     *   Validates that the old token exists and is not expired
     *   Deletes the old token from the database
     *   Creates a new refresh token for the same user
     * </ol>
     * 
     * If the token is invalid or expired, throws IllegalArgumentException.
     * 
     * @param oldToken The refresh token to rotate
     * @return The newly created refresh token string
     * @throws IllegalArgumentException If the token is invalid or expired
     */
    public String rotateRefreshToken(String oldToken) {
        RefreshToken existing = repo.findByToken(oldToken).orElseThrow(() -> new IllegalArgumentException("Token de refresco inválido"));
        if (existing.getExpiresAt() == null || existing.getExpiresAt().isBefore(OffsetDateTime.now())) {
            repo.delete(existing);
            throw new IllegalArgumentException("Token de refresco caducado");
        }
        String username = existing.getUsername();
        repo.delete(existing);
        return createRefreshToken(username);
    }

    /**
     * Validates a refresh token and returns the associated username if valid.
     * 
     * This method checks:
     * 
     *   That the token exists in the database
     *   That the token has not expired (expiration date is in the future)
     * 
     * 
     * If the token is valid, returns the username. Otherwise, returns an empty Optional.
     * 
     * @param token The refresh token string to validate
     * @return An Optional containing the username if the token is valid, empty otherwise
     */
    public Optional<String> validate(String token) {
        return repo.findByToken(token).filter(r -> r.getExpiresAt() != null && r.getExpiresAt().isAfter(OffsetDateTime.now())).map(RefreshToken::getUsername);
    }

    /**
     * Revokes a single refresh token by deleting it from the database.
     * 
     * This method is typically called during logout to invalidate the user's
     * refresh token. If the token doesn't exist, the operation is a no-op.
     * 
     * @param token The refresh token string to revoke
     */
    public void revokeToken(String token) {
        repo.findByToken(token).ifPresent(repo::delete);
    }

    /**
     * Revokes all refresh tokens for a specific user.
     * 
     * This method finds all refresh tokens associated with the given username
     * and deletes them from the database. This is useful for:
     * 
     *   Forcing a user to re-authenticate on all devices
     *   Security measures when a user's account is compromised
     * 
     * 
     * @param username The username whose refresh tokens should be revoked
     */
    public void revokeAllForUser(String username) {
        repo.findAll().stream().filter(r -> username.equals(r.getUsername())).forEach(repo::delete);
    }
}
