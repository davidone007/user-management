package com.example.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the User Management System.
 * 
 * This is the entry point of the Spring Boot application. It uses the
 * {@code @SpringBootApplication} annotation which includes:
 * 
 *   {@code @Configuration} - Marks this class as a configuration class
 *   {@code @EnableAutoConfiguration} - Enables Spring Boot auto-configuration
 *   {@code @ComponentScan} - Scans for components in the package and sub-packages
 * 
 * 
 * The application runs on port 8080 (HTTPS enabled) and provides a REST API
 * for user management with JWT-based authentication.
 * 
 * @author User Management System
 * @version 1.0
 */
@SpringBootApplication
public class UserManagementApplication {
    /**
     * Main method that starts the Spring Boot application.
     * 
     * @param args Command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }
}
