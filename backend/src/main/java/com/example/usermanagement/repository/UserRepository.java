package com.example.usermanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.usermanagement.model.User;

/**
 * Spring Data JPA repository for User entities.
 * 
 * This interface provides CRUD operations and custom query methods for user management.
 * Spring Data JPA automatically implements this interface at runtime.
 * 
 * Standard JPA methods (inherited from {@link JpaRepository}):
 * 
 *   {@code save(User)} - Save or update a user
 *   {@code findById(Long)} - Find a user by ID
 *   {@code findAll()} - Get all users
 *   {@code deleteById(Long)} - Delete a user by ID
 *   {@code existsById(Long)} - Check if a user exists by ID
 * 
 * 
 * Custom query methods:
 * 
 *   {@code findByUsername(String)} - Find a user by username (unique)
 *   {@code existsByUsername(String)} - Check if a username is already taken
 * 
 * 
 * @author User Management System
 * @version 1.0
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
