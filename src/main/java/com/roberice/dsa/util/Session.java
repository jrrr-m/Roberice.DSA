package com.roberice.dsa.util;

import com.roberice.dsa.model.User;

public class Session {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasPermission(String token) {
        if (currentUser == null) return false;
        return currentUser.hasPermission(token);
    }

    public static void logout() {
        currentUser = null;
    }
}
