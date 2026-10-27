package com.roberice.pos.ui;

import com.roberice.pos.db.DatabaseManager;
import com.roberice.pos.db.UserDAO;
import com.roberice.pos.model.User;
import com.roberice.pos.service.AuthSession;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Authentication dialog for Cashiers and Administrators.
 */
public class LoginDialog extends JDialog {
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JLabel lblError;
    private final UserDAO userDAO = new UserDAO();
    private boolean loginSuccessful = false;

    public LoginDialog(Frame parent) {
        super(parent, "Sign In - Roberice POS", true);
        setSize(440, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(32, 40, 32, 40));

        // Header / Logo
        JLabel lblLogo = new JLabel("ROBERICE");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblLogo.setForeground(UIUtils.COLOR_PRIMARY);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Wholesale & Retail Rice Merchandising");
        lblSub.setFont(UIUtils.FONT_SUBTITLE);
        lblSub.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSignInTitle = new JLabel("Sign In to POS Terminal");
        lblSignInTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSignInTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        lblSignInTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        JLabel lblUserTag = new JLabel("Username or Email");
        lblUserTag.setFont(UIUtils.FONT_BOLD);
        lblUserTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new JTextField(15);
        txtUsername.setFont(UIUtils.FONT_REGULAR);
        txtUsername.setPreferredSize(new Dimension(340, 36));
        txtUsername.setMaximumSize(new Dimension(340, 36));
        txtUsername.setText("admin"); // convenient default

        JLabel lblPassTag = new JLabel("Password");
        lblPassTag.setFont(UIUtils.FONT_BOLD);
        lblPassTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField(15);
        txtPassword.setFont(UIUtils.FONT_REGULAR);
        txtPassword.setPreferredSize(new Dimension(340, 36));
        txtPassword.setMaximumSize(new Dimension(340, 36));
        txtPassword.setText("admin123");

        lblError = new JLabel(" ");
        lblError.setFont(UIUtils.FONT_SMALL);
        lblError.setForeground(UIUtils.COLOR_DANGER);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnLogin = UIUtils.createPrimaryButton("Sign In");
        btnLogin.setMaximumSize(new Dimension(340, 42));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> doLogin());

        JButton btnRegister = new JButton("<html>Don't have an account? <u>Request Account</u></html>");
        btnRegister.setFont(UIUtils.FONT_SMALL);
        btnRegister.setForeground(UIUtils.COLOR_PRIMARY);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> {
            RegisterDialog reg = new RegisterDialog(this);
            reg.setVisible(true);
        });

        // Quick Credentials helper
        JPanel credBox = new JPanel(new GridLayout(2, 1, 2, 2));
        credBox.setBackground(new Color(248, 250, 252));
        credBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        credBox.setMaximumSize(new Dimension(340, 50));
        JLabel c1 = new JLabel("Default Admin: admin / admin123");
        c1.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        c1.setForeground(UIUtils.COLOR_TEXT_MUTED);
        JLabel c2 = new JLabel("Default Cashier: cashier1 / cashier123");
        c2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        c2.setForeground(UIUtils.COLOR_TEXT_MUTED);
        credBox.add(c1);
        credBox.add(c2);

        // Database status pill
        JLabel lblDbStatus = new JLabel(" " + DatabaseManager.getActiveDbInfo() + " ");
        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblDbStatus.setOpaque(true);
        lblDbStatus.setBackground(DatabaseManager.isMySql() ? new Color(220, 252, 231) : new Color(254, 243, 199));
        lblDbStatus.setForeground(DatabaseManager.isMySql() ? new Color(22, 101, 52) : new Color(180, 83, 9));
        lblDbStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Enter key listeners
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterKey);
        txtPassword.addKeyListener(enterKey);

        mainPanel.add(lblLogo);
        mainPanel.add(lblSub);
        mainPanel.add(Box.createVerticalStrut(14));
        mainPanel.add(lblSignInTitle);
        mainPanel.add(Box.createVerticalStrut(18));
        mainPanel.add(lblUserTag);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(txtUsername);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(lblPassTag);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(txtPassword);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(lblError);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(btnRegister);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(credBox);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(lblDbStatus);

        setContentPane(mainPanel);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter username and password.");
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                AuthSession.setCurrentUser(user);
                loginSuccessful = true;
                dispose();
            } else {
                lblError.setText("Invalid username or password.");
            }
        } catch (Exception ex) {
            lblError.setText(ex.getMessage());
        }
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}
