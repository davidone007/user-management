package com.example.usermanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.usermanagement.model.RefreshToken;

/**
 * Spring Data JPA repository for RefreshToken entities.
 * 
 * This interface provides CRUD operations and custom query methods for refresh token management.
 * Spring Data JPA automatically implements this interface at runtime.
 * 
 * Standard JPA methods (inherited from {@link JpaRepository}):
 * 
 *   {@code save(RefreshToken)} - Save or update a refresh token
 *   {@code findById(Long)} - Find a refresh token by ID
 *   {@code findAll()} - Get all refresh tokens
 *   {@code delete(RefreshToken)} - Delete a refresh token
 * 
 * 
 * Custom query methods:
 * 
 *   {@code findByToken(String)} - Find a refresh token by its token string (used for validation)
 * 
 * 
 * @author User Management System
 * @version 1.0
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
}
