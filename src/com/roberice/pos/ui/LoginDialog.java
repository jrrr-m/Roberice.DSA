package com.roberice.pos.ui;

import com.roberice.pos.db.UserDAO;
import com.roberice.pos.model.User;
import com.roberice.pos.service.AuthSession;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Clean full-window authentication dialog for Roberice Store.
 * The card fills the entire window seamlessly with the amber top accent bar,
 * eliminating outer surrounding gaps.
 */
public class LoginDialog extends JDialog {
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JLabel lblError;
    private final UserDAO userDAO = new UserDAO();
    private boolean loginSuccessful = false;

    // Palette extracted from the design
    private static final Color COLOR_CARD_BG = new Color(255, 253, 246);      // Warm Ivory #FFFCF6
    private static final Color COLOR_AMBER_ACCENT = new Color(245, 158, 11);  // Amber top bar #F59E0B
    private static final Color COLOR_PRIMARY_BTN = new Color(27, 77, 50);     // Forest green #1B4D32
    private static final Color COLOR_PRIMARY_BTN_HOVER = new Color(21, 62, 40);
    private static final Color COLOR_TITLE = new Color(15, 30, 24);           // Deep dark green-slate #0F1E18
    private static final Color COLOR_MUTED = new Color(110, 125, 115);        // Slate grey #6E7D73
    private static final Color COLOR_FIELD_LABEL = new Color(38, 50, 43);     // Label color
    private static final Color COLOR_INPUT_BORDER = new Color(215, 210, 195); // Default border
    private static final Color COLOR_INPUT_FOCUS = new Color(27, 77, 50);     // Active focus border
    private static final Color COLOR_DIVIDER = new Color(230, 235, 240);      // Divider line

    public LoginDialog(Frame parent) {
        super(parent, "Sign In - Roberice Store", true);
        setSize(390, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Entire window is the card itself - fills from edge to edge
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill background with warm ivory
                g2.setColor(COLOR_CARD_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Amber top accent bar across the top edge
                g2.setColor(COLOR_AMBER_ACCENT);
                g2.fillRect(0, 0, getWidth(), 4);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(26, 36, 26, 36));

        // 1. Logo
        JLabel lblLogoImage = new JLabel();
        lblLogoImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        ImageIcon logoIcon = loadLogoIcon(58, 58);
        if (logoIcon != null) {
            lblLogoImage.setIcon(logoIcon);
        } else {
            lblLogoImage.setText("🌾");
            lblLogoImage.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        }

        // 2. Title & Subtitle
        JLabel lblTitle = new JLabel("Roberice Store");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_TITLE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("POS & Inventory Management System");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(COLOR_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Username Field
        JLabel lblUserTag = new JLabel("Username or Email");
        lblUserTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserTag.setForeground(COLOR_FIELD_LABEL);

        txtUsername = createRoundedTextField("admin");
        JPanel userPanel = createFieldWrapper(lblUserTag, txtUsername);

        // 4. Password Field
        JLabel lblPassTag = new JLabel("Password");
        lblPassTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassTag.setForeground(COLOR_FIELD_LABEL);

        txtPassword = createRoundedPasswordField("admin123");
        JPanel passPanel = createFieldWrapper(lblPassTag, txtPassword);

        // 5. Error message
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblError.setForeground(new Color(220, 38, 38));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 6. Sign In Button
        JButton btnLogin = new JButton("Sign In") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(COLOR_PRIMARY_BTN_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(COLOR_PRIMARY_BTN_HOVER);
                } else {
                    g2.setColor(COLOR_PRIMARY_BTN);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(310, 42));
        btnLogin.setMaximumSize(new Dimension(310, 42));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> doLogin());

        // 7. Subtle Divider
        JSeparator divider = new JSeparator(SwingConstants.HORIZONTAL);
        divider.setMaximumSize(new Dimension(310, 1));
        divider.setForeground(COLOR_DIVIDER);
        divider.setBackground(COLOR_DIVIDER);
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 8. Sign up Link
        JPanel bottomLinkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        bottomLinkPanel.setOpaque(false);
        bottomLinkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNoAcc = new JLabel("Don't have an account?");
        lblNoAcc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNoAcc.setForeground(COLOR_MUTED);

        JButton btnSignUp = new JButton("Sign up");
        btnSignUp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSignUp.setForeground(COLOR_PRIMARY_BTN);
        btnSignUp.setContentAreaFilled(false);
        btnSignUp.setBorderPainted(false);
        btnSignUp.setFocusPainted(false);
        btnSignUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSignUp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btnSignUp.addActionListener(e -> {
            RegisterDialog reg = new RegisterDialog(this);
            reg.setVisible(true);
        });

        bottomLinkPanel.add(lblNoAcc);
        bottomLinkPanel.add(btnSignUp);

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

        // Assemble main panel
        mainPanel.add(lblLogoImage);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(lblSubtitle);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(userPanel);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(passPanel);
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(lblError);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalStrut(18));
        mainPanel.add(divider);
        mainPanel.add(Box.createVerticalStrut(14));
        mainPanel.add(bottomLinkPanel);

        setContentPane(mainPanel);
    }

    private JPanel createFieldWrapper(JLabel label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(310, 60));
        panel.setPreferredSize(new Dimension(310, 60));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        return panel;
    }

    private JTextField createRoundedTextField(String defaultValue) {
        JTextField tf = new JTextField(defaultValue) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        setupFieldStyle(tf);
        return tf;
    }

    private JPasswordField createRoundedPasswordField(String defaultValue) {
        JPasswordField pf = new JPasswordField(defaultValue) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        setupFieldStyle(pf);
        return pf;
    }

    private void setupFieldStyle(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(COLOR_TITLE);
        tf.setOpaque(false);
        tf.setPreferredSize(new Dimension(310, 36));
        tf.setMaximumSize(new Dimension(310, 36));
        updateBorder(tf, false);

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateBorder(tf, true);
                tf.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                updateBorder(tf, false);
                tf.repaint();
            }
        });
    }

    private void updateBorder(JTextField tf, boolean focused) {
        Color borderColor = focused ? COLOR_INPUT_FOCUS : COLOR_INPUT_BORDER;
        int thickness = focused ? 2 : 1;
        tf.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(borderColor, 10, thickness),
                new EmptyBorder(4, 12, 4, 12)
        ));
    }

    private ImageIcon loadLogoIcon(int width, int height) {
        try {
            File f = new File("resources/logo.png");
            if (!f.exists()) {
                f = new File("src/resources/logo.png");
            }
            if (f.exists()) {
                Image img = ImageIO.read(f);
                Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
            InputStream is = getClass().getResourceAsStream("/resources/logo.png");
            if (is != null) {
                Image img = ImageIO.read(is);
                Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignored) {
        }
        return null;
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

    /**
     * Reusable custom rounded border.
     */
    private static class RoundedBorder implements Border {
        private final Color color;
        private final int radius;
        private final int thickness;

        RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            int offset = thickness / 2;
            g2.drawRoundRect(x + offset, y + offset, width - thickness, height - thickness, radius, radius);
            g2.dispose();
        }
    }
}
