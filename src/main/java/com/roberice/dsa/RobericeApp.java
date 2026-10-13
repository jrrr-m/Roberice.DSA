package com.roberice.dsa;

import com.formdev.flatlaf.FlatDarkLaf;
import com.roberice.dsa.db.Database;
import com.roberice.dsa.ui.LoginFrame;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class RobericeApp {

    public static void main(String[] args) {
        // 1. Initialize Modern FlatLaf Dark Theme
        try {
            FlatDarkLaf.setup();

            // Refined FlatLaf UI Tuning
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("TableHeader.separatorColor", UIUtils.BORDER_COLOR);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);

        } catch (Exception e) {
            System.err.println("[RobericeApp] Warning: Could not setup FlatLaf theme: " + e.getMessage());
        }

        // 2. Pre-flight Check: Verify MySQL Database Connectivity
        try (Connection conn = Database.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("[RobericeApp] Successfully connected to MySQL database 'roberice_db'.");
            }
        } catch (Exception e) {
            System.err.println("[RobericeApp] Database connection error: " + e.getMessage());
            JOptionPane.showMessageDialog(
                    null,
                    "Unable to connect to the MySQL database 'roberice_db'.\n\n" +
                    "Please ensure XAMPP MySQL is running on localhost:3306.\n" +
                    "Error details: " + e.getMessage(),
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        // 3. Launch Authentication GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
