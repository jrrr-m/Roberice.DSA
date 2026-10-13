package com.roberice.dsa.ui;

import com.roberice.dsa.model.User;
import com.roberice.dsa.service.AuthService;
import com.roberice.dsa.util.Session;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel errorLabel;
    private final JButton loginButton;

    public LoginFrame() {
        setTitle("Roberice DSA - Authentication");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UIUtils.BG_MAIN);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIUtils.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(40, 48, 40, 48));

        // Brand Banner
        JLabel iconLbl = new JLabel("🌾", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandLbl = new JLabel("ROBERICE DSA", SwingConstants.CENTER);
        brandLbl.setFont(UIUtils.FONT_TITLE);
        brandLbl.setForeground(UIUtils.PRIMARY);
        brandLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Grains Trading & Inventory Management System", SwingConstants.CENTER);
        subLbl.setFont(UIUtils.FONT_SMALL);
        subLbl.setForeground(UIUtils.TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(iconLbl);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(brandLbl);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(subLbl);
        mainPanel.add(Box.createVerticalStrut(28));

        // Card Container for Form (with signature Harvest Gold top border)
        JPanel card = new JPanel();
        card.setBackground(UIUtils.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(3, 0, 0, 0, UIUtils.BRAND_GOLD)
                ),
                new EmptyBorder(24, 28, 24, 28)
        ));

        // Username
        JLabel uLbl = new JLabel("Username");
        uLbl.setFont(UIUtils.FONT_BOLD);
        uLbl.setForeground(UIUtils.TEXT_SECONDARY);
        uLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField("admin");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setPreferredSize(new Dimension(320, 40));
        usernameField.setFont(UIUtils.FONT_REGULAR);
        usernameField.putClientProperty("JTextField.placeholderText", "Enter username");

        // Password
        JLabel pLbl = new JLabel("Password");
        pLbl.setFont(UIUtils.FONT_BOLD);
        pLbl.setForeground(UIUtils.TEXT_SECONDARY);
        pLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField("admin123");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setPreferredSize(new Dimension(320, 40));
        passwordField.setFont(UIUtils.FONT_REGULAR);

        // Error message label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(UIUtils.FONT_SMALL);
        errorLabel.setForeground(UIUtils.ACCENT_DANGER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Sign In Button
        loginButton = UIUtils.createPrimaryButton("Sign In to System", e -> doLogin());
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setPreferredSize(new Dimension(320, 44));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Enter key listeners
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        card.add(uLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));
        card.add(pLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(12));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(loginButton);

        mainPanel.add(card);
        mainPanel.add(Box.createVerticalStrut(18));

        // Quick Credentials Notice
        JLabel hint = new JLabel("Default Admin: admin / admin123  •  Cashier: cashier / cashier123", SwingConstants.CENTER);
        hint.setFont(UIUtils.FONT_SMALL);
        hint.setForeground(UIUtils.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(hint);

        add(mainPanel);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        loginButton.setEnabled(false);
        errorLabel.setText("Verifying credentials...");
        errorLabel.setForeground(UIUtils.ACCENT_INFO);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return AuthService.login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    User user = get();
                    Session.setCurrentUser(user);

                    // Open Main Window
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        MainFrame mainFrame = new MainFrame();
                        mainFrame.setVisible(true);
                    });

                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    errorLabel.setText(msg);
                    errorLabel.setForeground(UIUtils.ACCENT_DANGER);
                }
            }
        };
        worker.execute();
    }
}
