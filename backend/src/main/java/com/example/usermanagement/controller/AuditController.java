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

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AuditController {
    private final LoginAuditRepository auditRepo;

    public AuditController(LoginAuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    @GetMapping("/audit")
    public ResponseEntity<List<LoginAudit>> getAudit(@RequestParam("username") String username) {
        List<LoginAudit> entries = auditRepo.findAll();
        entries.removeIf(e -> !e.getUsername().equals(username));
        return ResponseEntity.ok(entries);
    }
}
