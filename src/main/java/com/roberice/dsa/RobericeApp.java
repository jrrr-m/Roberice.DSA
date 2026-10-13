package com.roberice.dsa;

import com.formdev.flatlaf.FlatLightLaf;
import com.roberice.dsa.db.Database;
import com.roberice.dsa.ui.LoginFrame;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class RobericeApp {

    public static void main(String[] args) {
        // 1. Initialize Signature Roberice Warm Cream & Forest Green Theme (FlatLightLaf)
        try {
            FlatLightLaf.setup();

            // Core Global Palette Tuning matching original css/styles.css
            UIManager.put("Panel.background", UIUtils.BG_MAIN);
            UIManager.put("Viewport.background", UIUtils.BG_MAIN);
            UIManager.put("ScrollPane.background", UIUtils.BG_MAIN);
            UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());

            // Card, Table & Inputs
            UIManager.put("Table.background", UIUtils.BG_CARD);
            UIManager.put("Table.foreground", UIUtils.TEXT_MAIN);
            UIManager.put("Table.alternateRowColor", UIUtils.BG_CARD_ALT);
            UIManager.put("Table.gridColor", UIUtils.BORDER_COLOR);
            UIManager.put("Table.selectionBackground", UIUtils.PRIMARY_LIGHT);
            UIManager.put("Table.selectionForeground", UIUtils.PRIMARY);
            UIManager.put("TableHeader.background", new Color(243, 237, 216)); // #f3edd8
            UIManager.put("TableHeader.foreground", new Color(59, 78, 66));     // #3b4e42
            UIManager.put("TableHeader.separatorColor", UIUtils.BORDER_COLOR);
            UIManager.put("TableHeader.bottomSeparatorColor", UIUtils.BORDER_COLOR);

            // Form inputs
            UIManager.put("TextField.background", UIUtils.BG_INPUT);
            UIManager.put("TextField.foreground", UIUtils.TEXT_MAIN);
            UIManager.put("TextField.borderColor", UIUtils.BORDER_COLOR);
            UIManager.put("TextField.focusedBorderColor", UIUtils.PRIMARY);

            UIManager.put("PasswordField.background", UIUtils.BG_INPUT);
            UIManager.put("PasswordField.foreground", UIUtils.TEXT_MAIN);
            UIManager.put("PasswordField.borderColor", UIUtils.BORDER_COLOR);
            UIManager.put("PasswordField.focusedBorderColor", UIUtils.PRIMARY);

            UIManager.put("ComboBox.background", UIUtils.BG_INPUT);
            UIManager.put("ComboBox.foreground", UIUtils.TEXT_MAIN);
            UIManager.put("ComboBox.borderColor", UIUtils.BORDER_COLOR);

            UIManager.put("Spinner.background", UIUtils.BG_INPUT);
            UIManager.put("Spinner.foreground", UIUtils.TEXT_MAIN);
            UIManager.put("Spinner.borderColor", UIUtils.BORDER_COLOR);

            // TabbedPane
            UIManager.put("TabbedPane.background", UIUtils.BG_CARD);
            UIManager.put("TabbedPane.selectedBackground", UIUtils.PRIMARY_LIGHT);
            UIManager.put("TabbedPane.selectedForeground", UIUtils.PRIMARY);
            UIManager.put("TabbedPane.underlineColor", UIUtils.BRAND_GOLD);
            UIManager.put("TabbedPane.contentBorderColor", UIUtils.BORDER_COLOR);

            // Button & component arcs
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.thumb", new Color(212, 204, 184)); // #d4ccb8
            UIManager.put("ScrollBar.hoverThumbColor", new Color(181, 170, 145)); // #b5aa91
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);

        } catch (Exception e) {
            System.err.println("[RobericeApp] Warning: Could not setup FlatLightLaf theme: " + e.getMessage());
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
