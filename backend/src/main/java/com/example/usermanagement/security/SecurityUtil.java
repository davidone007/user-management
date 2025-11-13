package com.example.usermanagement.security;

import java.security.SecureRandom;

public final class SecurityUtil {
    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom RAND = new SecureRandom();

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
