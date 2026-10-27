package com.roberice.pos.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.roberice.pos.db.DatabaseManager;
import com.roberice.pos.service.AuthSession;

import javax.swing.*;
import java.awt.*;

/**
 * Main application entry point for Roberice POS & Inventory Management System.
 */
public class RobericeApp {

    public static void main(String[] args) {
        // Set Look and Feel
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("ScrollBar.showButtons", true);
            UIManager.put("TabbedPane.showTabSeparators", true);
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Enable Anti-Aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Initialize Database (MySQL or fallback to SQLite)
        DatabaseManager.initialize();

        SwingUtilities.invokeLater(RobericeApp::showLogin);
    }

    public static void showLogin() {
        LoginDialog login = new LoginDialog(null);
        login.setVisible(true);

        if (login.isLoginSuccessful() && AuthSession.isLoggedIn()) {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        } else {
            System.exit(0);
        }
    }
}
