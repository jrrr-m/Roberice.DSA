package com.roberice.dsa.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roberice.dsa.db.Database;
import com.roberice.dsa.model.User;
import com.roberice.dsa.util.PasswordUtil;

import java.util.*;

public class UserService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<User> getAllUsers() {
        String sql = "SELECT u.user_id, u.username, u.full_name, u.role, u.account_status, u.permissions, " +
                "COUNT(t.transaction_id) AS tx_count " +
                "FROM users u " +
                "LEFT JOIN sales_transaction t ON u.user_id = t.user_id " +
                "GROUP BY u.user_id " +
                "ORDER BY u.user_id ASC";

        List<Map<String, Object>> rows = Database.queryForList(sql);
        List<User> list = new ArrayList<>();

        for (Map<String, Object> r : rows) {
            User u = new User();
            u.setUserId(((Number) r.get("user_id")).intValue());
            u.setUsername(String.valueOf(r.get("username")));
            u.setFullName(r.get("full_name") != null ? String.valueOf(r.get("full_name")) : u.getUsername());
            u.setRole(String.valueOf(r.get("role")));
            u.setAccountStatus(String.valueOf(r.get("account_status")));
            u.setTransactionCount(r.get("tx_count") != null ? ((Number) r.get("tx_count")).intValue() : 0);

            List<String> perms = new ArrayList<>();
            Object permsObj = r.get("permissions");
            if (permsObj != null && !permsObj.toString().trim().isEmpty()) {
                try {
                    perms = objectMapper.readValue(permsObj.toString(), new TypeReference<List<String>>() {});
                } catch (Exception ignored) {}
            }
            if (perms.isEmpty()) {
                if ("admin".equalsIgnoreCase(u.getRole())) {
                    perms = Arrays.asList("dashboard", "pos", "view-inventory", "inventory", "products",
                            "transactions", "sales-report", "file-manager", "audit", "users");
                } else {
                    perms = Arrays.asList("pos", "view-inventory", "transactions", "sales-report");
                }
            }
            u.setPermissions(perms);
            list.add(u);
        }
        return list;
    }

    public static void createUser(String username, String password, String fullName, String role, String status, List<String> permissions) throws Exception {
        if (username == null || username.trim().isEmpty()) throw new Exception("Username is required.");
        if (password == null || password.trim().isEmpty()) throw new Exception("Password is required.");

        Map<String, Object> existing = Database.queryForMap("SELECT user_id FROM users WHERE username = ?", username.trim());
        if (existing != null) {
            throw new Exception("Username '" + username + "' is already taken.");
        }

        String hash = PasswordUtil.hashPassword(password.trim());
        String permsJson = objectMapper.writeValueAsString(permissions != null ? permissions : Collections.emptyList());

        String sql = "INSERT INTO users (username, password, full_name, role, account_status, permissions) VALUES (?, ?, ?, ?, ?, ?)";
        Database.update(sql, username.trim(), hash, fullName != null ? fullName.trim() : username.trim(),
                role != null ? role.trim() : "Cashier",
                status != null ? status.trim() : "Active",
                permsJson);
    }

    public static void updateUser(int userId, String username, String newPasswordOrEmpty, String fullName, String role, String status, List<String> permissions) throws Exception {
        if (username == null || username.trim().isEmpty()) throw new Exception("Username is required.");

        Map<String, Object> check = Database.queryForMap("SELECT user_id FROM users WHERE username = ? AND user_id != ?", username.trim(), userId);
        if (check != null) {
            throw new Exception("Username '" + username + "' is already in use by another user.");
        }

        String permsJson = objectMapper.writeValueAsString(permissions != null ? permissions : Collections.emptyList());

        if (newPasswordOrEmpty != null && !newPasswordOrEmpty.trim().isEmpty()) {
            String hash = PasswordUtil.hashPassword(newPasswordOrEmpty.trim());
            String sql = "UPDATE users SET username = ?, password = ?, full_name = ?, role = ?, account_status = ?, permissions = ? WHERE user_id = ?";
            Database.update(sql, username.trim(), hash, fullName.trim(), role, status, permsJson, userId);
        } else {
            String sql = "UPDATE users SET username = ?, full_name = ?, role = ?, account_status = ?, permissions = ? WHERE user_id = ?";
            Database.update(sql, username.trim(), fullName.trim(), role, status, permsJson, userId);
        }
    }

    public static void deleteUser(int userId) throws Exception {
        // Check if user has transactions
        Map<String, Object> tx = Database.queryForMap("SELECT COUNT(*) AS c FROM sales_transaction WHERE user_id = ?", userId);
        int txCount = tx != null && tx.get("c") != null ? ((Number) tx.get("c")).intValue() : 0;

        if (txCount > 0) {
            // Cannot delete due to foreign key constraint; deactivate instead
            Database.update("UPDATE users SET account_status = 'Inactive' WHERE user_id = ?", userId);
            throw new Exception("User has " + txCount + " sales records and cannot be deleted. The account has been deactivated instead.");
        } else {
            Database.update("DELETE FROM users WHERE user_id = ?", userId);
        }
    }
}
