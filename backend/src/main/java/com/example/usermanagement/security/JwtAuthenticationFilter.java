package com.example.usermanagement.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 

/**
 * Spring Security filter that processes JWT tokens from incoming requests.
 * 
 * This filter intercepts all HTTP requests and:
 * <ol>
 *   Extracts the JWT token from the Authorization header (Bearer token)
 *   Validates and parses the token using {@link JwtUtil}
 *   Extracts the username and role from the token claims
 *   Sets up Spring Security authentication context for the request
 * </ol>
 * 
 * If no token is present or the token is invalid, the filter clears the security
 * context and allows the request to continue. The actual authorization is handled by
 * Spring Security's method security annotations (e.g., {@code @PreAuthorize}).
 * 
 * This filter is registered in {@link SecurityConfig} and runs before
 * {@code UsernamePasswordAuthenticationFilter}.
 * 
 * @author User Management System
 * @version 1.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    /**
     * Constructs a new JwtAuthenticationFilter.
     * 
     * @param jwtUtil The JWT utility for parsing and validating tokens
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Processes the request to extract and validate JWT tokens.
     * 
     * Looks for an Authorization header with format "Bearer &lt;token&gt;",
     * validates the token, and sets up Spring Security authentication if valid.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param filterChain The filter chain to continue processing
     * @throws ServletException If a servlet error occurs
     * @throws IOException If an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Jws<Claims> claims = jwtUtil.parse(token);
                String username = claims.getBody().getSubject();
                String role = (String) claims.getBody().get("role");
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, java.util.Collections.singletonList(new SimpleGrantedAuthority(role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                // invalid token -> clear context
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
