package com.roberice.pos.service;

import com.roberice.pos.model.User;

/**
 * Singleton managing the authenticated user session.
 */
public class AuthSession {
    private static User currentUser = null;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean hasPermission(String module) {
        if (currentUser == null) return false;
        return currentUser.hasPermission(module);
    }

    public static String getFullName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }

    public static String getRole() {
        return currentUser != null ? currentUser.getRole() : "Visitor";
    }

    public static String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "";
    }
}
