package com.roberice.pos.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a user account with granular module permissions.
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role = "Cashier"; // "Admin", "Cashier", "Store Manager", "Inventory Clerk", "Auditor"
    private String accountStatus = "Active"; // "Active", "Inactive"
    private List<String> permissions = new ArrayList<>();

    public static final List<String> ALL_MODULES = Arrays.asList(
            "dashboard",
            "pos",
            "view-inventory",
            "inventory",
            "products",
            "transactions",
            "sales-report",
            "file-manager",
            "audit",
            "users"
    );

    public User() {}

    public User(int userId, String username, String password, String fullName, String email, String role, String accountStatus) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.accountStatus = accountStatus;
        applyRoleDefaultPermissions(role);
    }

    public void applyRoleDefaultPermissions(String r) {
        permissions.clear();
        if (r == null) return;
        switch (r.trim().toLowerCase()) {
            case "admin":
                permissions.addAll(ALL_MODULES);
                break;
            case "cashier":
                permissions.addAll(Arrays.asList("pos", "view-inventory", "transactions", "sales-report"));
                break;
            case "inventory clerk":
                permissions.addAll(Arrays.asList("view-inventory", "inventory", "products"));
                break;
            case "store manager":
                permissions.addAll(Arrays.asList("dashboard", "pos", "view-inventory", "products", "transactions", "sales-report", "file-manager", "audit"));
                break;
            case "auditor":
                permissions.addAll(Arrays.asList("dashboard", "transactions", "sales-report", "audit"));
                break;
            default:
                break;
        }
    }

    public boolean hasPermission(String module) {
        if ("Admin".equalsIgnoreCase(role)) return true;
        if (module == null) return false;
        return permissions.contains(module.toLowerCase());
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
