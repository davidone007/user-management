package com.example.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 * 
 * This configuration sets up the OpenAPI specification for the REST API,
 * which enables interactive API documentation through Swagger UI.
 * 
 * The Swagger UI is accessible at:
 * 
 *   Swagger UI: http://localhost:8080/swagger-ui.html
 *   OpenAPI JSON: http://localhost:8080/v3/api-docs
 * 
 * 
 * This is useful for:
 * 
 *   API exploration and testing during development
 *   Documentation for API consumers
 *   Verifying endpoint contracts
 * 
 * 
 * @author User Management System
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("User Management API").version("1.0"));
    }
}
