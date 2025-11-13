package com.example.usermanagement.service;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.usermanagement.model.RefreshToken;
import com.example.usermanagement.repository.RefreshTokenRepository;

class RefreshTokenServiceTest {
    private RefreshTokenRepository repo;
    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        repo = mock(RefreshTokenRepository.class);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        service = new RefreshTokenService(repo);
    }

    @Test
    void createAndValidate() {
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        String token = service.createRefreshToken("alice");
        assertNotNull(token);

        RefreshToken saved = new RefreshToken();
        saved.setToken(token);
        saved.setUsername("alice");
        saved.setExpiresAt(OffsetDateTime.now().plusDays(1));
        when(repo.findByToken(token)).thenReturn(Optional.of(saved));

        Optional<String> v = service.validate(token);
        assertTrue(v.isPresent());
        assertEquals("alice", v.get());
    }

    @Test
    void rotateDeletesOldAndCreatesNew() {
        RefreshToken old = new RefreshToken();
        old.setToken("old");
        old.setUsername("bob");
        old.setExpiresAt(OffsetDateTime.now().plusDays(1));
        when(repo.findByToken("old")).thenReturn(Optional.of(old));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        String nt = service.rotateRefreshToken("old");
        assertNotNull(nt);
        assertNotEquals("old", nt);
        verify(repo).delete(old);
    }

    @Test
    void revokeTokenDeletes() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("x");
        rt.setUsername("u");
        rt.setExpiresAt(OffsetDateTime.now().plusDays(1));
        when(repo.findByToken("x")).thenReturn(Optional.of(rt));

        service.revokeToken("x");
        verify(repo).delete(rt);
    }
}
