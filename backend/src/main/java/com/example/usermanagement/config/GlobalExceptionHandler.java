package com.example.usermanagement.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler that converts common exceptions into HTTP responses.
 * 
 * This class provides centralized exception handling for all controllers.
 * It converts exceptions into appropriate HTTP responses with meaningful error messages.
 * 
 * Note: The handler returns exception messages directly; in production you may want
 * to map them to user-friendly codes/messages or avoid leaking internal details.
 * 
 * @author User Management System
 * @version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles validation errors from request body validation.
     * 
     * This method processes {@link MethodArgumentNotValidException} thrown when
     * request body validation fails (e.g., @NotBlank, @NotNull annotations).
     * It extracts field-level errors and returns them as a map in the response body.
     * 
     * @param ex The validation exception containing field errors
     * @return ResponseEntity with status 400 Bad Request and a map of field names to error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles all other exceptions that are not specifically handled.
     * 
     * This is a catch-all handler for any exception that doesn't have a specific
     * handler method. It returns a 500 Internal Server Error with the exception message
     * as the response body.
     * 
     * Warning: In production, consider sanitizing exception messages to avoid
     * exposing internal implementation details or stack traces.
     * 
     * @param ex The exception that was thrown
     * @return ResponseEntity with status 500 Internal Server Error and the exception message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
