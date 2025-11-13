package com.example.usermanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.usermanagement.model.LoginAudit;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
}
