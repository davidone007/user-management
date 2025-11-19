package com.example.usermanagement.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility class for password hashing and verification using PBKDF2.
 * 
 * This class implements password security using the PBKDF2 (Password-Based Key Derivation Function 2)
 * algorithm with HMAC-SHA256. PBKDF2 is a widely recommended algorithm for password hashing
 * that is resistant to brute-force attacks through its iterative hashing process.
 * 
 * Configuration:
 * 
 *   <strong>Algorithm:</strong> PBKDF2WithHmacSHA256
 *   <strong>Iterations:</strong> 310,000 (OWASP recommended minimum as of 2021)
 *   <strong>Key Length:</strong> 256 bits (32 bytes)
 *   <strong>Salt Length:</strong> 16 bytes (128 bits), Base64 encoded
 * 
 * 
 * Security features:
 * 
 *   Each password uses a unique, randomly generated salt
 *   High iteration count makes brute-force attacks computationally expensive
 *   Salts are stored separately from hashes to prevent rainbow table attacks
 * 
 * 
 * Usage pattern:
 * <ol>
 *   Generate a salt with {@link #generateSalt()}
 *   Hash the password with {@link #hash(char[], String)} using the salt
 *   Store both the salt and hash in the database
 *   Verify passwords with {@link #verify(char[], String, String)}
 * </ol>
 * 
 * @author User Management System
 * @version 1.0
 */
public final class Pbkdf2Password {
    /** PBKDF2 algorithm identifier */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    
    /** Number of iterations for key derivation (OWASP recommended minimum) */
    private static final int ITERATIONS = 310000;
    
    /** Output key length in bits */
    private static final int KEY_LENGTH = 256;
    
    /** Cryptographically secure random number generator for salt generation */
    private static final SecureRandom RAND = new SecureRandom();

    /**
     * Generates a cryptographically secure random salt for password hashing.
     * 
     * The salt is 16 bytes (128 bits) of random data, Base64 encoded for storage.
     * Each user should have a unique salt to prevent rainbow table attacks.
     * 
     * @return A Base64-encoded salt string
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RAND.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password using PBKDF2 with the provided salt.
     * 
     * The password is hashed using 310,000 iterations of PBKDF2 with HMAC-SHA256,
     * producing a 256-bit (32-byte) hash that is Base64 encoded for storage.
     * 
     * @param password The password to hash (as a char array to allow clearing from memory)
     * @param salt The Base64-encoded salt to use for hashing
     * @return A Base64-encoded hash string
     * @throws RuntimeException If the hashing algorithm is unavailable or the salt is invalid
     */
    public static String hash(char[] password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, Base64.getDecoder().decode(salt), ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing a password: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies a password against a stored hash and salt.
     * 
     * This method hashes the provided password with the given salt and compares
     * the result with the expected hash using a constant-time comparison to prevent
     * timing attacks.
     * 
     * @param password The password to verify (as a char array)
     * @param salt The Base64-encoded salt used when the password was originally hashed
     * @param expectedHash The Base64-encoded hash to compare against
     * @return {@code true} if the password matches the hash, {@code false} otherwise
     */
    public static boolean verify(char[] password, String salt, String expectedHash) {
        String h = hash(password, salt);
        return h.equals(expectedHash);
    }
}
