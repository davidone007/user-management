package com.example.usermanagement.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity class representing a user in the system.
 * 
 * This class maps to the "users" table in the database and contains:
 * 
 *   User identification (id, username)
 *   Password security data (passwordHash, salt) - stored separately for PBKDF2 hashing
 *   Role-based access control (ADMIN or USER)
 *   Login tracking (lastLogin timestamp)
 *   Password reset enforcement flag (forcePasswordReset)
 * 
 * 
 * The password is never stored in plain text. Instead, it's hashed using PBKDF2
 * with a unique salt per user, stored in the {@code passwordHash} and {@code salt} fields.
 * 
 * @author User Management System
 * @version 1.0
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column
    private String salt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "force_password_reset")
    private boolean forcePasswordReset = false;

    /**
     * Enumeration of user roles in the system.
     * 
     *   {@code ADMIN} - Full access to all endpoints including user management and audit logs
     *   {@code USER} - Standard user with access to personal account management
     * 
     */
    public enum Role {ADMIN, USER}

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public OffsetDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(OffsetDateTime lastLogin) { this.lastLogin = lastLogin; }
    public boolean isForcePasswordReset() { return forcePasswordReset; }
    public void setForcePasswordReset(boolean forcePasswordReset) { this.forcePasswordReset = forcePasswordReset; }
}
