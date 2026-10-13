package com.roberice.dsa.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int userId;
    private String username;
    private String fullName;
    private String role;
    private String accountStatus;
    private List<String> permissions = new ArrayList<>();
    private int transactionCount;

    public User() {}

    public User(int userId, String username, String fullName, String role, String accountStatus, List<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.accountStatus = accountStatus;
        if (permissions != null) {
            this.permissions = permissions;
        }
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    public boolean hasPermission(String token) {
        if ("admin".equalsIgnoreCase(role)) return true;
        return permissions != null && permissions.contains(token);
    }
}
