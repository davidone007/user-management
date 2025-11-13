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

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    private String generateTokenString() {
        byte[] b = new byte[64];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

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
     * Rotate the provided refresh token by deleting the old one and creating a new one for the same user.
     * Returns the newly created refresh token string.
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

    public Optional<String> validate(String token) {
        return repo.findByToken(token).filter(r -> r.getExpiresAt() != null && r.getExpiresAt().isAfter(OffsetDateTime.now())).map(RefreshToken::getUsername);
    }

    public void revokeToken(String token) {
        repo.findByToken(token).ifPresent(repo::delete);
    }

    public void revokeAllForUser(String username) {
        repo.findAll().stream().filter(r -> username.equals(r.getUsername())).forEach(repo::delete);
    }
}
