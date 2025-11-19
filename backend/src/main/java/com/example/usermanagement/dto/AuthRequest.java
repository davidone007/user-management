package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for authentication requests (login).
 * 
 * This DTO is used when a user attempts to log in to the system.
 * It contains the user's credentials and an optional "remember me" flag.
 * 
 * Validation constraints:
 * 
 *   {@code username} - Must not be blank
 *   {@code password} - Must not be blank
 *   {@code remember} - Optional boolean flag (currently not used in the implementation)
 * 
 * 
 * @author User Management System
 * @version 1.0
 */
public class AuthRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private boolean remember;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isRemember() { return remember; }
    public void setRemember(boolean remember) { this.remember = remember; }
}
