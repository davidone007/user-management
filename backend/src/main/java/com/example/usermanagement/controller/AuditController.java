package com.example.usermanagement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.usermanagement.model.LoginAudit;
import com.example.usermanagement.repository.LoginAuditRepository;

/**
 * REST controller for audit log operations.
 * 
 * This controller provides endpoints for querying login audit records.
 * All endpoints require ADMIN role authorization.
 * 
 * Available operations:
 * 
 *   Retrieve login audit records for a specific user
 * 
 * 
 * Audit records contain:
 * 
 *   Username of the user who logged in
 *   IP address from which the login originated (normalized)
 *   Timestamp of the login event
 * 
 * 
 * Security:
 * 
 *   All endpoints require ADMIN role (enforced by {@code @PreAuthorize})
 *   JWT token must be included in Authorization header
 * 
 * 
 * @author User Management System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AuditController {
    private final LoginAuditRepository auditRepo;

    /**
     * Constructs a new AuditController.
     * 
     * @param auditRepo The repository for login audit data access
     */
    public AuditController(LoginAuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    /**
     * Retrieves login audit records for a specific user.
     * 
     * This endpoint queries all login audit records and filters them by username.
     * The results are returned in chronological order (oldest first).
     * 
     * Note: The current implementation loads all audit records and filters in memory.
     * For production with large datasets, consider adding a repository method with
     * a WHERE clause for better performance.
     * 
     * @param username The username to filter audit records by
     * @return ResponseEntity containing a list of LoginAudit records for the specified user
     */
    @GetMapping("/audit")
    public ResponseEntity<List<LoginAudit>> getAudit(@RequestParam("username") String username) {
        List<LoginAudit> entries = auditRepo.findAll();
        entries.removeIf(e -> !e.getUsername().equals(username));
        return ResponseEntity.ok(entries);
    }
}
