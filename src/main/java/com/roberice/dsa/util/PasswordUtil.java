package com.roberice.dsa.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return "";
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }

        // Check if stored password is BCrypt hashed
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2y$") || storedHash.startsWith("$2b$")) {
            try {
                // PHP password_hash uses $2y$, jBCrypt expects $2a$
                String normalizedHash = storedHash;
                if (normalizedHash.startsWith("$2y$") || normalizedHash.startsWith("$2b$")) {
                    normalizedHash = "$2a$" + normalizedHash.substring(4);
                }
                return BCrypt.checkpw(plainPassword, normalizedHash);
            } catch (Exception e) {
                // If BCrypt check fails, continue to check plain text
            }
        }

        // Plain text fallback (matches PHP logic in login.php)
        return plainPassword.equals(storedHash);
    }
}
