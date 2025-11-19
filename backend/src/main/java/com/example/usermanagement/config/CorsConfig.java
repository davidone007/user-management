package com.example.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuration class for Cross-Origin Resource Sharing (CORS).
 * 
 * This configuration allows the React frontend (running on localhost:3000)
 * to make requests to the backend API. CORS is necessary because the frontend
 * and backend run on different ports/origins.
 * 
 * Configuration details:
 * 
 *   <strong>Allowed Origins:</strong> http://localhost:3000 and https://localhost:3000
 *   <strong>Allowed Methods:</strong> GET, POST, PUT, DELETE, OPTIONS
 *   <strong>Allowed Headers:</strong> All headers (*)
 *   <strong>Credentials:</strong> Enabled (required for cookie-based authentication)
 * 
 * 
 * For production, the allowed origins should be restricted to the actual
 * frontend domain(s) instead of using localhost.
 * 
 * @author User Management System
 * @version 1.0
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();
    // Allow both http and https dev origins
    cfg.setAllowedOrigins(java.util.Arrays.asList("http://localhost:3000", "https://localhost:3000"));
    cfg.setAllowedMethods(java.util.Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(java.util.Arrays.asList("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(src);
    }
}
