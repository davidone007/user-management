package com.example.usermanagement.dto;

/**
 * Data Transfer Object for authentication responses.
 * 
 * This DTO is returned after successful login or token refresh operations.
 * It contains:
 * 
 *   {@code token} - The JWT access token (short-lived, typically 5 minutes)
 *   {@code forcePasswordReset} - Flag indicating if the user must change their password
 *   {@code tempPassword} - Temporary password (only set when admin resets a user's password)
 * 
 * 
 * The access token should be included in the Authorization header for subsequent API requests:
 * <pre>Authorization: Bearer &lt;token&gt;</pre>
 * 
 * @author User Management System
 * @version 1.0
 */
public class AuthResponse {
    private String token;
    private boolean forcePasswordReset;
    private String tempPassword;
    public AuthResponse() {}
    public AuthResponse(String token) { this.token = token; }
    public boolean isForcePasswordReset() { return forcePasswordReset; }
    public void setForcePasswordReset(boolean forcePasswordReset) { this.forcePasswordReset = forcePasswordReset; }
    public String getTempPassword() { return tempPassword; }
    public void setTempPassword(String tempPassword) { this.tempPassword = tempPassword; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
