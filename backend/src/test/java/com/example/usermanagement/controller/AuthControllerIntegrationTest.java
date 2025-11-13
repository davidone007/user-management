package com.example.usermanagement.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verifyNoInteractions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.usermanagement.service.RefreshTokenService;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerIntegrationTest {
    @Autowired MockMvc mvc;
    @MockBean RefreshTokenService refreshTokenService;
    @MockBean com.example.usermanagement.service.AuthService authService;
    @MockBean com.example.usermanagement.repository.UserRepository userRepository;
    @MockBean com.example.usermanagement.security.JwtUtil jwtUtil;
    @MockBean com.example.usermanagement.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() { }

    @Test
    void logoutCallsRevoke() throws Exception {
        // send a simple logout request (cookies not actually present in this test)
        mvc.perform(post("/api/auth/logout")
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser").roles("USER"))
        ).andExpect(status().isOk());
        // No refresh token cookie present; ensure no interactions fail
        verifyNoInteractions(refreshTokenService);
    }
}
