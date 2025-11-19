package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for password change requests.
 * 
 * This DTO is used when a user wants to change their password.
 * It requires both the current password (for verification) and the new password.
 * 
 * Validation constraints:
 * 
 *   {@code oldPassword} - Must not be blank, must match the user's current password
 *   {@code newPassword} - Must not be blank, will become the user's new password
 * 
 * 
 * Upon successful password change:
 * 
 *   A new salt is generated for the user
 *   The new password is hashed with PBKDF2 using the new salt
 *   The {@code forcePasswordReset} flag is cleared if it was set
 * 
 * 
 * @author User Management System
 * @version 1.0
 */
public class ChangePasswordRequest {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
