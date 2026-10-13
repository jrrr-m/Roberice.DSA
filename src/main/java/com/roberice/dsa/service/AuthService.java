package com.roberice.dsa.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roberice.dsa.db.Database;
import com.roberice.dsa.model.User;
import com.roberice.dsa.util.PasswordUtil;

import java.util.*;

public class AuthService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static User login(String username, String password) throws Exception {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new Exception("Username and password are required.");
        }

        String sql = "SELECT user_id, username, password, full_name, role, account_status, permissions FROM users WHERE username = ? LIMIT 1";
        Map<String, Object> userRow = Database.queryForMap(sql, username.trim());

        if (userRow == null) {
            throw new Exception("Invalid username or password.");
        }

        String status = String.valueOf(userRow.get("account_status"));
        if (!"Active".equalsIgnoreCase(status)) {
            throw new Exception("Account is inactive. Please contact the administrator.");
        }

        String storedPassword = String.valueOf(userRow.get("password"));
        boolean valid = PasswordUtil.verifyPassword(password, storedPassword);

        if (!valid) {
            throw new Exception("Invalid username or password.");
        }

        // If matched plain text, upgrade to BCrypt in database
        if (!storedPassword.startsWith("$2a$") && !storedPassword.startsWith("$2y$") && storedPassword.equals(password)) {
            String newHash = PasswordUtil.hashPassword(password);
            Database.update("UPDATE users SET password = ? WHERE user_id = ?", newHash, userRow.get("user_id"));
        }

        List<String> perms = new ArrayList<>();
        Object permsObj = userRow.get("permissions");
        if (permsObj != null && !permsObj.toString().trim().isEmpty()) {
            try {
                perms = objectMapper.readValue(permsObj.toString(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }

        String role = String.valueOf(userRow.get("role")).toLowerCase();
        if (perms.isEmpty()) {
            if ("admin".equals(role)) {
                perms = Arrays.asList("dashboard", "pos", "view-inventory", "inventory", "products",
                        "transactions", "sales-report", "file-manager", "audit", "users");
            } else {
                perms = Arrays.asList("pos", "view-inventory", "transactions", "sales-report");
            }
        }

        int userId = ((Number) userRow.get("user_id")).intValue();
        return new User(userId, String.valueOf(userRow.get("username")), String.valueOf(userRow.get("full_name")), role, status, perms);
    }
}
