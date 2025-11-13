package com.example.usermanagement.dto;

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
