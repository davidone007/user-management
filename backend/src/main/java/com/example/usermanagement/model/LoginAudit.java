package com.example.usermanagement.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity class representing a login audit record.
 * 
 * This class stores audit information for each successful login attempt, including:
 * 
 *   The username of the user who logged in
 *   The IP address from which the login originated (normalized for IPv6 compatibility)
 *   The timestamp of the login event
 * 
 * 
 * Audit records are created automatically by the {@link com.example.usermanagement.service.AuthService}
 * during the login process and can be queried by administrators through the audit endpoint.
 * 
 * @author User Management System
 * @version 1.0
 */
@Entity
@Table(name = "login_audit")
public class LoginAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String ip;
    private OffsetDateTime timestamp;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
}
