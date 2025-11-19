package com.example.usermanagement.util;

import com.example.usermanagement.security.Pbkdf2Password;

/**
 * Utility for local development: generate salt, hash and print SQL to set admin password.
 * Usage (from project root):
 *  ./mvnw -DskipTests package
 *  java -cp backend/target/classes com.example.usermanagement.util.DevPasswordUtil "NewPass123!"
 */
public class DevPasswordUtil {
    /**
     * Main method for command-line execution.
     * 
     * This utility generates a salt and password hash for a given password
     * and outputs an SQL UPDATE statement that can be executed in the H2 console
     * to set the admin user's password.
     * 
     * Usage:
     * <pre>
     * java -cp target/classes com.example.usermanagement.util.DevPasswordUtil "NewPass123!"
     * </pre>
     * 
     * The output is an SQL statement that updates the admin user's salt and
     * password_hash in the users table.
     * 
     * @param args Command-line arguments. The first argument should be the password to hash.
     *             If no argument is provided, prints usage information and exits.
     */
    public static void main(String[] args) {
        String pwd = null;
        if (args != null && args.length > 0) pwd = args[0];
        if (pwd == null || pwd.isEmpty()) {
            System.err.println("Usage: java com.example.usermanagement.util.DevPasswordUtil <password>");
            System.exit(2);
        }

        String salt = Pbkdf2Password.generateSalt();
        String hash = Pbkdf2Password.hash(pwd.toCharArray(), salt);

        System.out.println("-- Ejecuta la siguiente sentencia SQL en la consola H2 (o mediante JDBC) para fijar la contraseña del usuario 'admin':");
        System.out.println();
        System.out.println("UPDATE users SET salt='" + salt + "', password_hash='" + hash + "' WHERE username='admin';");
        System.out.println();
        System.out.println("-- Luego accede al frontend y prueba iniciar sesión con el usuario 'admin' y la contraseña proporcionada.");
    }
}
