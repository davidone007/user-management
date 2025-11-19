package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for user registration requests.
 * 
 * This DTO is used when a new user registers in the system.
 * It contains the basic information needed to create a new user account.
 * 
 * Validation constraints:
 * 
 *   {@code username} - Must not be blank and must be unique
 *   {@code password} - Must not be blank
 * 
 * 
 * Upon successful registration, the user is created with the {@code USER} role by default.
 * The password is hashed using PBKDF2 with a randomly generated salt before storage.
 * 
 * @author User Management System
 * @version 1.0
 */
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
