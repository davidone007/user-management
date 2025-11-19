package com.example.usermanagement.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    /**
     * Security configuration for the application. In development we allow the H2 console
     * and some OpenAPI endpoints. For production you should tighten these rules and
     * disable the H2 console.
     */
    private final JwtAuthenticationFilter jwtFilter;

    /**
     * Constructs a new SecurityConfig.
     * 
     * @param jwtFilter The JWT authentication filter to use for request filtering
     */
    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Configures the Spring Security filter chain.
     * 
     * This method sets up:
     * 
     *   CSRF protection disabled (stateless API)
     *   Stateless session management (no server-side sessions)
     *   Public endpoints: /api/auth/**, /v3/api-docs/**, /swagger-ui/**, /h2-console/**
     *   All other endpoints require authentication
     *   JWT authentication filter added before UsernamePasswordAuthenticationFilter
     *   H2 console frame options set to sameOrigin (development only)
     * 
     * 
     * Note: The H2 console should be disabled in production environments.
     * 
     * @param http The HttpSecurity object to configure
     * @return The configured SecurityFilterChain
     * @throws Exception If configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/h2-console/**").permitAll()
            .anyRequest().authenticated()
        )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    // Allow H2 console frames (only for development) so the web UI works
    http.headers().frameOptions().sameOrigin();

        return http.build();
    }
}
