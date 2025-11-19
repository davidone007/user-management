package com.example.usermanagement.security;

import java.security.SecureRandom;

/**
 * Utility class for security-related helper functions.
 * 
 * This class provides utility methods for:
 * 
 *   Generating secure, readable passwords (e.g., for temporary password resets)
 *   Normalizing IP addresses for consistent logging and audit trails
 * 
 * 
 * @author User Management System
 * @version 1.0
 */
public final class SecurityUtil {
    /** Character set for readable password generation (excludes ambiguous characters like 0, O, I, l) */
    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    
    /** Cryptographically secure random number generator */
    private static final SecureRandom RAND = new SecureRandom();

    /**
     * Generates a cryptographically secure, human-readable password.
     * 
     * The generated password uses a character set that excludes ambiguous characters
     * (0, O, I, l) to improve readability while maintaining security. This is useful
     * for generating temporary passwords that users need to type manually.
     * 
     * @param length The desired length of the password
     * @return A randomly generated password string
     */
    public static String generateReadablePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RAND.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Normalize an IP address string for logging/audit purposes.
     * - Converts IPv6 loopback (::1 or 0:0:0:0:0:0:0:1) to 127.0.0.1
     * - If the input contains an IPv4-mapped IPv6 address, returns the IPv4 portion
     */
    public static String normalizeIp(String ip) {
        if (ip == null) return null;
        ip = ip.trim();
        // common IPv6 loopback representations
        if (ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1")) return "127.0.0.1";
        // IPv4-mapped IPv6 ::ffff:127.0.0.1
        if (ip.startsWith("::ffff:")) {
            return ip.substring(7);
        }
        return ip;
    }
}
