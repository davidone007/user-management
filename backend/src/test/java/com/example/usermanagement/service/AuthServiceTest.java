package com.example.usermanagement.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.usermanagement.dto.AuthRequest;
import com.example.usermanagement.dto.RegisterRequest;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.security.JwtUtil;

class AuthServiceTest {
    private UserRepository repo;
    private JwtUtil jwtUtil;
    private com.example.usermanagement.repository.LoginAuditRepository auditRepo;
    private AuthService service;

    @BeforeEach
    void setup() {
        repo = mock(UserRepository.class);
        jwtUtil = mock(JwtUtil.class);
        auditRepo = mock(com.example.usermanagement.repository.LoginAuditRepository.class);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("token");
        service = new AuthService(repo, jwtUtil, auditRepo);
    }

    @Test
    void registerAndLogin() {
        RegisterRequest r = new RegisterRequest();
        r.setUsername("alice");
        r.setPassword("pass123");
        when(repo.existsByUsername("alice")).thenReturn(false);

        service.register(r);

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(repo).save(cap.capture());
        assertEquals("alice", cap.getValue().getUsername());

        // prepare stored user for login
        User stored = cap.getValue();
        when(repo.findByUsername("alice")).thenReturn(Optional.of(stored));

        AuthRequest ar = new AuthRequest();
        ar.setUsername("alice");
        ar.setPassword("pass123");
    com.example.usermanagement.dto.AuthResponse resp = service.login(ar, "127.0.0.1");
    assertEquals("token", resp.getToken());
    }
}
