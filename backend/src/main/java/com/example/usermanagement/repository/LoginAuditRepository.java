package com.example.usermanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.usermanagement.model.LoginAudit;

/**
 * Spring Data JPA repository for LoginAudit entities.
 * 
 * This interface provides CRUD operations for login audit records.
 * Spring Data JPA automatically implements this interface at runtime.
 * 
 * Standard JPA methods (inherited from {@link JpaRepository}):
 * 
 *   {@code save(LoginAudit)} - Save a new audit record
 *   {@code findAll()} - Get all audit records
 *   {@code findById(Long)} - Find an audit record by ID
 *   {@code deleteById(Long)} - Delete an audit record by ID
 * 
 * 
 * Note: Audit records are typically queried by username in the service layer
 * using {@code findAll()} and filtering, rather than through a repository method.
 * 
 * @author User Management System
 * @version 1.0
 */
public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
}
