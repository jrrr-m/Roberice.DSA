package com.roberice.pos.ui.panels;

import com.roberice.pos.db.UserDAO;
import com.roberice.pos.model.AccountRequest;
import com.roberice.pos.model.User;
import com.roberice.pos.service.AuthSession;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User Management & Permissions Panel.
 * Add system users, assign roles, and configure granular module permissions.
 */
public class UsersPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();

    // Metric Labels
    private final JLabel lblTotalUsers = new JLabel("0");
    private final JLabel lblActiveAccounts = new JLabel("0");
    private final JLabel lblPendingRequests = new JLabel("0");
    private final JLabel lblAdmins = new JLabel("0");

    // Tab buttons & state
    private boolean showRequestsTab = false;
    private final JButton btnTabActive = new JButton();
    private final JButton btnTabRequests = new JButton();

    // Filter controls
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cmbRole = new JComboBox<>(new String[]{"All Roles", "Admin", "Cashier", "Inventory Clerk", "Auditor"});
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"All Statuses", "Active", "Deactivated"});
    private final JLabel lblCountShowing = new JLabel("Showing 0 accounts");

    // Container for dynamic rows
    private final JPanel rowsContainer = new JPanel();
    private final JPanel tableHeaderPanel = new JPanel(new BorderLayout());

    private List<User> userList = new ArrayList<>();
    private List<AccountRequest> requestList = new ArrayList<>();

    public UsersPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel mainLayout = new JPanel();
        mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
        mainLayout.setOpaque(false);

        // 1. Top Metric Cards (4 cards)
        JPanel metricsPanel = createMetricsPanel();
        mainLayout.add(metricsPanel);
        mainLayout.add(Box.createVerticalStrut(16));

        // 2. Tab Bar Pills
        JPanel tabBar = createTabBar();
        mainLayout.add(tabBar);
        mainLayout.add(Box.createVerticalStrut(14));

        // 3. Search & Filter Bar Card
        JPanel filterCard = createFilterCard();
        mainLayout.add(filterCard);
        mainLayout.add(Box.createVerticalStrut(16));

        // 4. Main Table Card Container
        JPanel tableCard = UIUtils.createCard();
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setOpaque(false);
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Table Title & Subtitle bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel lblSectionTitle = new JLabel("System User Accounts & Module Access");
        lblSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSectionTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        lblCountShowing.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCountShowing.setForeground(new Color(130, 125, 115));

        titleBar.add(lblSectionTitle, BorderLayout.WEST);
        titleBar.add(lblCountShowing, BorderLayout.EAST);
        tableCard.add(titleBar, BorderLayout.NORTH);

        // Header & Rows Panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 8));
        contentPanel.setOpaque(false);

        contentPanel.add(tableHeaderPanel, BorderLayout.NORTH);

        rowsContainer.setLayout(new BoxLayout(rowsContainer, BoxLayout.Y_AXIS));
        rowsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(rowsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        tableCard.add(contentPanel, BorderLayout.CENTER);
        mainLayout.add(tableCard);

        add(mainLayout, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createMetricsPanel() {
        JPanel p = new JPanel(new GridLayout(1, 4, 14, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        p.add(createKpiCard(lblTotalUsers, "TOTAL SYSTEM USERS", "Registered store accounts", new Color(209, 250, 229), "users"));
        p.add(createKpiCard(lblActiveAccounts, "ACTIVE ACCOUNTS", "Can sign in to system", new Color(243, 244, 246), "check"));
        p.add(createKpiCard(lblPendingRequests, "PENDING APPROVAL REQUESTS", "All requests reviewed", new Color(254, 243, 199), "clock"));
        p.add(createKpiCard(lblAdmins, "ADMINISTRATORS", "Full system governance", new Color(243, 232, 255), "shield"));

        return p;
    }

    private JPanel createKpiCard(JLabel valLbl, String tag, String subtext, Color badgeBg, String iconType) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(10, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTag = new JLabel(tag);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(UIUtils.COLOR_TEXT_MUTED);

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel lblSub = new JLabel(subtext);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        left.add(lblTag);
        left.add(Box.createVerticalStrut(2));
        left.add(valLbl);
        left.add(Box.createVerticalStrut(2));
        left.add(lblSub);

        // Right icon badge
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setStroke(new BasicStroke(1.6f));

                switch (iconType) {
                    case "users":
                        g2.setColor(new Color(16, 120, 80));
                        g2.drawOval(cx - 5, cy - 6, 10, 7);
                        g2.drawArc(cx - 7, cy, 14, 10, 0, 180);
                        break;
                    case "check":
                        g2.setColor(new Color(75, 85, 99));
                        g2.drawOval(cx - 7, cy - 7, 14, 14);
                        g2.drawLine(cx - 4, cy, cx - 1, cy + 3);
                        g2.drawLine(cx - 1, cy + 3, cx + 4, cy - 3);
                        break;
                    case "clock":
                        g2.setColor(new Color(180, 83, 9));
                        g2.drawOval(cx - 7, cy - 7, 14, 14);
                        g2.drawLine(cx, cy - 4, cx, cy);
                        g2.drawLine(cx, cy, cx + 3, cy);
                        break;
                    case "shield":
                        g2.setColor(new Color(126, 34, 206));
                        g2.drawRoundRect(cx - 6, cy - 7, 12, 14, 4, 4);
                        g2.drawLine(cx - 3, cy, cx + 3, cy);
                        break;
                }
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(36, 36));

        JPanel badgeWrap = new JPanel(new GridBagLayout());
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        card.add(left, BorderLayout.CENTER);
        card.add(badgeWrap, BorderLayout.EAST);

        return card;
    }

    private JPanel createTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        styleTabButton(btnTabActive, "👥 Active User Accounts  6", true);
        btnTabActive.addActionListener(e -> {
            showRequestsTab = false;
            updateTabStyles();
            renderTable();
        });

        styleTabButton(btnTabRequests, "⏳ Account Creation Requests  0 Pending", false);
        btnTabRequests.addActionListener(e -> {
            showRequestsTab = true;
            updateTabStyles();
            renderTable();
        });

        bar.add(btnTabActive);
        bar.add(btnTabRequests);

        return bar;
    }

    private void styleTabButton(JButton btn, String text, boolean active) {
        btn.setText(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);

        if (active) {
            btn.setForeground(Color.WHITE);
        } else {
            btn.setForeground(new Color(70, 75, 70));
        }
    }

    private void updateTabStyles() {
        btnTabActive.repaint();
        btnTabRequests.repaint();
    }

    private JPanel createFilterCard() {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(14, 0));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        // Search Input (Constrained width to avoid excessive empty space)
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by name, email, or username...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        JPanel searchWrap = new JPanel(new BorderLayout(8, 0));
        searchWrap.setBackground(Color.WHITE);
        searchWrap.setPreferredSize(new Dimension(320, 36));
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 220, 210), 1),
                new EmptyBorder(0, 10, 0, 0)
        ));
        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        lblSearchIcon.setForeground(UIUtils.COLOR_TEXT_MUTED);
        txtSearch.setBorder(new EmptyBorder(8, 4, 8, 10));
        searchWrap.add(lblSearchIcon, BorderLayout.WEST);
        searchWrap.add(txtSearch, BorderLayout.CENTER);

        // Right selectors + Buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        // Role Dropdown with label
        JPanel roleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        roleWrap.setOpaque(false);
        JLabel lblRole = new JLabel("Role");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRole.setForeground(UIUtils.COLOR_TEXT_MUTED);
        cmbRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbRole.setPreferredSize(new Dimension(130, 36));
        cmbRole.setBackground(Color.WHITE);
        cmbRole.addActionListener(e -> applyFilters());
        roleWrap.add(lblRole);
        roleWrap.add(cmbRole);

        // Status Dropdown with label
        JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        statusWrap.setOpaque(false);
        JLabel lblStatus = new JLabel("Account Status");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblStatus.setForeground(UIUtils.COLOR_TEXT_MUTED);
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbStatus.setPreferredSize(new Dimension(130, 36));
        cmbStatus.setBackground(Color.WHITE);
        cmbStatus.addActionListener(e -> applyFilters());
        statusWrap.add(lblStatus);
        statusWrap.add(cmbStatus);

        // Roles & Permissions button
        JButton btnRolesMatrix = new JButton("📊 Roles & Permissions");
        btnRolesMatrix.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnRolesMatrix.setForeground(new Color(40, 50, 45));
        btnRolesMatrix.setBackground(Color.WHITE);
        btnRolesMatrix.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 220, 210), 1),
                new EmptyBorder(7, 12, 7, 12)
        ));
        btnRolesMatrix.setFocusPainted(false);
        btnRolesMatrix.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRolesMatrix.addActionListener(e -> showPermissionsMatrixDialog());

        // Add New User button
        JButton btnAddNewUser = new JButton("+ Add New User") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(20, 60, 38));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(34, 95, 62));
                } else {
                    g2.setColor(new Color(27, 77, 50));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAddNewUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddNewUser.setForeground(Color.WHITE);
        btnAddNewUser.setContentAreaFilled(false);
        btnAddNewUser.setBorderPainted(false);
        btnAddNewUser.setFocusPainted(false);
        btnAddNewUser.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnAddNewUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddNewUser.addActionListener(e -> showAddEditDialog(null));

        rightPanel.add(roleWrap);
        rightPanel.add(statusWrap);
        rightPanel.add(btnRolesMatrix);
        rightPanel.add(btnAddNewUser);

        card.add(searchWrap, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    private JPanel createUsersTableHeader() {
        JPanel header = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(242, 234, 213)); // #F2EAD5
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(8, 16, 8, 16));
        header.setPreferredSize(new Dimension(1000, 36));
        header.setMinimumSize(new Dimension(800, 36));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 4, 0, 4);
        gbc.weighty = 1.0;

        addHeaderCol(header, gbc, "USER PROFILE & CONTACT", 0.22, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "USERNAME", 0.10, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "ROLE", 0.10, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "AUTHORIZED MODULES & PERMISSIONS", 0.38, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "STATUS", 0.08, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "ACTIONS", 0.12, SwingConstants.RIGHT);

        return header;
    }

    private JPanel createRequestsTableHeader() {
        JPanel header = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(242, 234, 213));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 4, 0, 4);

        addHeaderCol(header, gbc, "REQ ID", 0.08, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "APPLICANT NAME", 0.22, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "USERNAME & EMAIL", 0.24, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "REQUESTED ROLE", 0.16, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "STATUS", 0.12, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "ACTIONS", 0.18, SwingConstants.RIGHT);

        return header;
    }

    private void addHeaderCol(JPanel p, GridBagConstraints gbc, String title, double weight, int align) {
        gbc.weightx = weight;
        JLabel lbl = new JLabel(title, align);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(90, 80, 70));
        p.add(lbl, gbc);
    }

    public void refreshData() {
        try {
            userList = userDAO.getAllUsers();
            requestList = userDAO.getAccountRequests();

            int activeCount = 0;
            int adminCount = 0;
            for (User u : userList) {
                if ("Active".equalsIgnoreCase(u.getAccountStatus())) activeCount++;
                if ("Admin".equalsIgnoreCase(u.getRole())) adminCount++;
            }

            int pendingCount = 0;
            for (AccountRequest r : requestList) {
                if ("Pending".equalsIgnoreCase(r.getStatus())) pendingCount++;
            }

            lblTotalUsers.setText(String.valueOf(userList.size()));
            lblActiveAccounts.setText(String.valueOf(activeCount));
            lblPendingRequests.setText(String.valueOf(pendingCount));
            lblAdmins.setText(String.valueOf(adminCount));

            btnTabActive.setText("👥 Active User Accounts  " + userList.size());
            btnTabRequests.setText("⏳ Account Creation Requests  " + pendingCount + " Pending");

            renderTable();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void renderTable() {
        tableHeaderPanel.removeAll();
        if (showRequestsTab) {
            tableHeaderPanel.add(createRequestsTableHeader(), BorderLayout.CENTER);
        } else {
            tableHeaderPanel.add(createUsersTableHeader(), BorderLayout.CENTER);
        }
        tableHeaderPanel.revalidate();
        tableHeaderPanel.repaint();

        applyFilters();
    }

    private void applyFilters() {
        rowsContainer.removeAll();

        String kw = txtSearch.getText().trim().toLowerCase();
        String selectedRole = (String) cmbRole.getSelectedItem();
        boolean filterRole = selectedRole != null && !selectedRole.equalsIgnoreCase("All Roles");
        String selectedStatus = (String) cmbStatus.getSelectedItem();
        boolean filterStatus = selectedStatus != null && !selectedStatus.equalsIgnoreCase("All Statuses");

        int count = 0;

        if (!showRequestsTab) {
            for (User u : userList) {
                if (filterRole && !selectedRole.equalsIgnoreCase(u.getRole())) continue;
                if (filterStatus && !selectedStatus.equalsIgnoreCase(u.getAccountStatus())) continue;

                if (!kw.isEmpty()) {
                    String name = (u.getFullName() != null ? u.getFullName() : "").toLowerCase();
                    String user = (u.getUsername() != null ? u.getUsername() : "").toLowerCase();
                    String email = (u.getEmail() != null ? u.getEmail() : "").toLowerCase();
                    String role = (u.getRole() != null ? u.getRole() : "").toLowerCase();

                    if (!name.contains(kw) && !user.contains(kw) && !email.contains(kw) && !role.contains(kw)) {
                        continue;
                    }
                }

                rowsContainer.add(createUserRow(u));
                rowsContainer.add(createRowDivider());
                count++;
            }
            lblCountShowing.setText("Showing " + count + " of " + userList.size() + " accounts");
        } else {
            for (AccountRequest req : requestList) {
                if (filterRole && !selectedRole.equalsIgnoreCase(req.getRole())) continue;
                if (filterStatus && !selectedStatus.equalsIgnoreCase(req.getStatus())) continue;

                if (!kw.isEmpty()) {
                    String name = (req.getFullName() != null ? req.getFullName() : "").toLowerCase();
                    String user = (req.getUsername() != null ? req.getUsername() : "").toLowerCase();
                    String email = (req.getEmail() != null ? req.getEmail() : "").toLowerCase();

                    if (!name.contains(kw) && !user.contains(kw) && !email.contains(kw)) {
                        continue;
                    }
                }

                rowsContainer.add(createRequestRow(req));
                rowsContainer.add(createRowDivider());
                count++;
            }
            lblCountShowing.setText("Showing " + count + " of " + requestList.size() + " requests");
        }

        if (count == 0) {
            JPanel empty = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
            empty.setOpaque(false);
            JLabel lblEmpty = new JLabel(showRequestsTab ? "No account requests match the filters." : "No user accounts match current search or filters.");
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblEmpty.setForeground(UIUtils.COLOR_TEXT_MUTED);
            empty.add(lblEmpty);
            rowsContainer.add(empty);
        }

        rowsContainer.revalidate();
        rowsContainer.repaint();
    }

    private JPanel createUserRow(User u) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 16, 8, 16));
        row.setPreferredSize(new Dimension(1000, 52));
        row.setMinimumSize(new Dimension(800, 52));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 4, 0, 4);
        gbc.weighty = 1.0;

        // 1. USER PROFILE & CONTACT (Avatar circle + Full Name + Email + 'You' pill if current)
        gbc.weightx = 0.22;
        row.add(createUserProfileCell(u), gbc);

        // 2. USERNAME
        gbc.weightx = 0.10;
        JLabel lblUser = new JLabel(u.getUsername());
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        row.add(lblUser, gbc);

        // 3. ROLE PILL
        gbc.weightx = 0.10;
        row.add(createRoleCell(u.getRole()), gbc);

        // 4. AUTHORIZED MODULES & PERMISSIONS
        gbc.weightx = 0.38;
        row.add(createPermissionsCell(u), gbc);

        // 5. STATUS
        gbc.weightx = 0.08;
        row.add(createStatusCell(u.getAccountStatus()), gbc);

        // 6. ACTIONS (Edit, Deactivate/Activate, Delete)
        gbc.weightx = 0.12;
        row.add(createUserActionsCell(u), gbc);

        return row;
    }

    private JPanel createUserProfileCell(User u) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);

        // Initials avatar circle
        String initials = getInitials(u.getFullName() != null ? u.getFullName() : u.getUsername());
        JLabel avatar = new JLabel(initials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 240, 230)); // Warm sand circle
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        avatar.setForeground(new Color(110, 100, 90));
        avatar.setPreferredSize(new Dimension(34, 34));

        // Name + You Pill + Email
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JPanel nameLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nameLine.setOpaque(false);

        JLabel lblName = new JLabel(u.getFullName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        nameLine.add(lblName);

        boolean isSelf = AuthSession.getCurrentUser() != null &&
                AuthSession.getCurrentUser().getUserId() == u.getUserId();
        if (isSelf) {
            JLabel pillYou = new JLabel("You", SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(224, 242, 254)); // Light blue
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pillYou.setFont(new Font("Segoe UI", Font.BOLD, 9));
            pillYou.setForeground(new Color(2, 132, 199));
            pillYou.setBorder(new EmptyBorder(2, 6, 2, 6));
            nameLine.add(pillYou);
        }

        JLabel lblEmail = new JLabel("✉ " + (u.getEmail() != null ? u.getEmail() : "no-email@roberice.com"));
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblEmail.setForeground(new Color(130, 125, 115));

        textPanel.add(nameLine);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblEmail);

        p.add(avatar);
        p.add(textPanel);

        return p;
    }

    private JPanel createRoleCell(String role) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
        p.setOpaque(false);

        Color bg;
        Color fg;

        if ("Admin".equalsIgnoreCase(role)) {
            bg = new Color(209, 250, 229); // Mint
            fg = new Color(16, 120, 80);
        } else if ("Cashier".equalsIgnoreCase(role)) {
            bg = new Color(224, 242, 254); // Cyan
            fg = new Color(2, 132, 199);
        } else if ("Inventory Clerk".equalsIgnoreCase(role)) {
            bg = new Color(254, 243, 199); // Amber
            fg = new Color(180, 83, 9);
        } else if ("Auditor".equalsIgnoreCase(role)) {
            bg = new Color(224, 242, 254);
            fg = new Color(3, 105, 161);
        } else {
            bg = new Color(243, 244, 246);
            fg = new Color(75, 85, 99);
        }

        JLabel pill = new JLabel(role != null ? role : "Staff", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pill.setForeground(fg);
        pill.setBorder(new EmptyBorder(3, 10, 3, 10));
        pill.setOpaque(false);

        p.add(pill);
        return p;
    }

    private JPanel createPermissionsCell(User u) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 16));
        p.setOpaque(false);

        List<String> perms = u.getPermissions();
        boolean isFullAdmin = "Admin".equalsIgnoreCase(u.getRole()) ||
                (perms != null && (perms.contains("all") || perms.size() >= 10));

        if (isFullAdmin) {
            JLabel pillFull = new JLabel("★ Full System Access (10 Modules)", SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(209, 250, 229)); // Light mint
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pillFull.setFont(new Font("Segoe UI", Font.BOLD, 10));
            pillFull.setForeground(new Color(16, 120, 80));
            pillFull.setBorder(new EmptyBorder(3, 10, 3, 10));
            p.add(pillFull);
        } else {
            if (perms != null) {
                for (String mod : perms) {
                    JLabel lblMod = new JLabel(formatModuleTag(mod));
                    lblMod.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    lblMod.setForeground(new Color(80, 85, 80));
                    p.add(lblMod);
                }
            }
        }

        return p;
    }

    private String formatModuleTag(String mod) {
        switch (mod.toLowerCase()) {
            case "dashboard": return "📊 Dashboard";
            case "pos": return "🛒 POS";
            case "view-inventory": return "👁 View Inventory";
            case "inventory": return "📦 Manage Inventory";
            case "products": return "🏷 Products";
            case "transactions": return "📋 Transactions";
            case "sales-report": return "📈 Sales Report";
            case "file-manager": return "📁 Files";
            case "audit": return "📜 Audit";
            case "users": return "👥 Users";
            default: return "• " + mod;
        }
    }

    private JPanel createStatusCell(String status) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
        p.setOpaque(false);

        boolean isActive = "Active".equalsIgnoreCase(status);
        Color bg = isActive ? new Color(209, 250, 229) : new Color(254, 226, 226);
        Color fg = isActive ? new Color(16, 120, 80) : new Color(185, 28, 28);

        JLabel pill = new JLabel(isActive ? "Active" : "Deactivated", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pill.setForeground(fg);
        pill.setBorder(new EmptyBorder(3, 10, 3, 10));
        pill.setOpaque(false);

        p.add(pill);
        return p;
    }

    private JPanel createUserActionsCell(User u) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 12));
        actions.setOpaque(false);

        boolean isSelf = AuthSession.getCurrentUser() != null &&
                AuthSession.getCurrentUser().getUserId() == u.getUserId();

        JButton btnEdit = createBorderedButton("✏ Edit");
        btnEdit.addActionListener(e -> showAddEditDialog(u));
        actions.add(btnEdit);

        if (!isSelf) {
            boolean isActive = "Active".equalsIgnoreCase(u.getAccountStatus());
            JButton btnToggle = createBorderedButton(isActive ? "🚫 Deactivate" : "✔ Activate");
            btnToggle.addActionListener(e -> toggleUserStatus(u));
            actions.add(btnToggle);

            JButton btnDelete = createBorderedButton("🗑");
            btnDelete.setForeground(new Color(220, 38, 38));
            btnDelete.addActionListener(e -> deleteUser(u));
            actions.add(btnDelete);
        }

        return actions;
    }

    private JPanel createRequestRow(AccountRequest req) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 16, 12, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 4, 0, 4);

        // 1. REQ ID
        gbc.weightx = 0.08;
        JLabel lblId = new JLabel("REQ-" + req.getRequestId());
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblId.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        row.add(lblId, gbc);

        // 2. APPLICANT NAME
        gbc.weightx = 0.22;
        JLabel lblName = new JLabel(req.getFullName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        row.add(lblName, gbc);

        // 3. USERNAME & EMAIL
        gbc.weightx = 0.24;
        JPanel userEmail = new JPanel();
        userEmail.setLayout(new BoxLayout(userEmail, BoxLayout.Y_AXIS));
        userEmail.setOpaque(false);
        JLabel lblU = new JLabel(req.getUsername());
        lblU.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblU.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        JLabel lblEm = new JLabel(req.getEmail());
        lblEm.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEm.setForeground(new Color(130, 125, 115));
        userEmail.add(lblU);
        userEmail.add(lblEm);
        row.add(userEmail, gbc);

        // 4. REQUESTED ROLE
        gbc.weightx = 0.16;
        row.add(createRoleCell(req.getRole()), gbc);

        // 5. STATUS
        gbc.weightx = 0.12;
        row.add(createStatusCell(req.getStatus()), gbc);

        // 6. ACTIONS (Approve / Reject)
        gbc.weightx = 0.18;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        if ("Pending".equalsIgnoreCase(req.getStatus())) {
            JButton btnApprove = createBorderedButton("✔ Approve");
            btnApprove.setForeground(new Color(22, 101, 52));
            btnApprove.addActionListener(e -> approveRequest(req));

            JButton btnReject = createBorderedButton("✖ Reject");
            btnReject.setForeground(new Color(220, 38, 38));
            btnReject.addActionListener(e -> rejectRequest(req));

            actions.add(btnApprove);
            actions.add(btnReject);
        } else {
            JLabel lblDone = new JLabel("Reviewed");
            lblDone.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblDone.setForeground(UIUtils.COLOR_TEXT_MUTED);
            actions.add(lblDone);
        }
        row.add(actions, gbc);

        return row;
    }

    private JButton createBorderedButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btn.setForeground(new Color(60, 65, 60));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 220, 210), 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JComponent createRowDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(238, 234, 224));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    // =========================================================================
    // Action Dialogs
    // =========================================================================

    public void showAddEditDialog(User editUser) {
        boolean isEdit = editUser != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Staff User Account" : "Add New System User", true);
        dlg.setLayout(new BorderLayout(0, 16));
        dlg.setSize(520, 600);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UIUtils.COLOR_BG);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField txtFullName = new JTextField(isEdit ? editUser.getFullName() : "");
        JTextField txtUsername = new JTextField(isEdit ? editUser.getUsername() : "");
        txtUsername.setEnabled(!isEdit);
        JTextField txtEmail = new JTextField(isEdit ? editUser.getEmail() : "");
        JPasswordField txtPass = new JPasswordField();

        JComboBox<String> cmbRolePick = new JComboBox<>(new String[]{"Cashier", "Inventory Clerk", "Auditor", "Admin"});
        if (isEdit) cmbRolePick.setSelectedItem(editUser.getRole());

        // Granular Permissions check boxes
        String[] allMods = {"dashboard", "pos", "view-inventory", "inventory", "products", "transactions", "sales-report", "file-manager", "audit", "users"};
        String[] modLabels = {"Dashboard Overview", "Point of Sale (POS)", "View Inventory Drill-down", "Manage Inventory & Restocks", "Products & Pricing Catalog", "Transaction History", "Sales Reports & Analytics", "File & Backup Management", "Audit History & Trail", "User Account Management"};

        List<JCheckBox> chkList = new ArrayList<>();
        JPanel permGrid = new JPanel(new GridLayout(5, 2, 8, 6));
        permGrid.setOpaque(false);

        List<String> curPerms = isEdit && editUser.getPermissions() != null ? editUser.getPermissions() : new ArrayList<>();
        boolean isAll = curPerms.contains("all");

        for (int i = 0; i < allMods.length; i++) {
            String mKey = allMods[i];
            JCheckBox cb = new JCheckBox(modLabels[i], isAll || curPerms.contains(mKey));
            cb.setOpaque(false);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            chkList.add(cb);
            permGrid.add(cb);
        }

        // Auto check defaults when role changes
        cmbRolePick.addActionListener(e -> {
            String r = (String) cmbRolePick.getSelectedItem();
            for (int i = 0; i < allMods.length; i++) {
                String mKey = allMods[i];
                boolean check = false;
                if ("Admin".equalsIgnoreCase(r)) {
                    check = true;
                } else if ("Cashier".equalsIgnoreCase(r)) {
                    check = mKey.equals("pos") || mKey.equals("view-inventory") || mKey.equals("transactions") || mKey.equals("sales-report");
                } else if ("Inventory Clerk".equalsIgnoreCase(r)) {
                    check = mKey.equals("view-inventory") || mKey.equals("inventory") || mKey.equals("products");
                } else if ("Auditor".equalsIgnoreCase(r)) {
                    check = mKey.equals("dashboard") || mKey.equals("transactions") || mKey.equals("sales-report") || mKey.equals("audit");
                }
                chkList.get(i).setSelected(check);
            }
        });

        addFormField(p, "Full Name *", txtFullName);
        addFormField(p, "Username *", txtUsername);
        addFormField(p, "Email Address *", txtEmail);
        if (!isEdit) {
            addFormField(p, "Initial Password *", txtPass);
        }
        addFormField(p, "System Role *", cmbRolePick);

        JLabel lblPerm = new JLabel("Module Access Permissions:");
        lblPerm.setFont(new Font("Segoe UI", Font.BOLD, 11));
        p.add(lblPerm);
        p.add(Box.createVerticalStrut(6));
        p.add(permGrid);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UIUtils.createPrimaryButton(isEdit ? "Save Changes" : "Create Account");
        btnSave.addActionListener(e -> {
            String name = txtFullName.getText().trim();
            String user = txtUsername.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();
            String role = (String) cmbRolePick.getSelectedItem();

            if (name.isEmpty() || user.isEmpty() || email.isEmpty() || (!isEdit && pass.isEmpty())) {
                JOptionPane.showMessageDialog(dlg, "Please fill in all required fields.", "Incomplete Form", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<String> chosenPerms = new ArrayList<>();
            for (int i = 0; i < allMods.length; i++) {
                if (chkList.get(i).isSelected()) {
                    chosenPerms.add(allMods[i]);
                }
            }

            try {
                if (isEdit) {
                    editUser.setFullName(name);
                    editUser.setEmail(email);
                    editUser.setRole(role);
                    editUser.setPermissions(chosenPerms);
                    userDAO.updateUser(editUser);
                    JOptionPane.showMessageDialog(dlg, "User account updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    User newUser = new User();
                    newUser.setFullName(name);
                    newUser.setUsername(user);
                    newUser.setEmail(email);
                    newUser.setRole(role);
                    newUser.setAccountStatus("Active");
                    newUser.setPermissions(chosenPerms);
                    userDAO.addUser(newUser, pass);
                    JOptionPane.showMessageDialog(dlg, "New user account created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dlg.dispose();
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error saving user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(p, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void addFormField(JPanel p, String label, JComponent comp) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(comp);
        p.add(Box.createVerticalStrut(10));
    }

    private void toggleUserStatus(User u) {
        boolean currentlyActive = "Active".equalsIgnoreCase(u.getAccountStatus());
        String newStatus = currentlyActive ? "Deactivated" : "Active";
        int confirm = JOptionPane.showConfirmDialog(this,
                (currentlyActive ? "Deactivate" : "Re-activate") + " account for '" + u.getFullName() + "'?",
                "Confirm Account Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userDAO.setUserStatus(u.getUserId(), newStatus);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteUser(User u) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user account '" + u.getFullName() + "'?\nThis action cannot be undone.",
                "Confirm Delete User", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userDAO.deleteUser(u.getUserId());
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void approveRequest(AccountRequest req) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Approve registration request for " + req.getFullName() + " (" + req.getUsername() + ")?",
                "Approve Request", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userDAO.reviewAccountRequest(req.getRequestId(), "Approved", null, AuthSession.getCurrentUser() != null ? AuthSession.getCurrentUser().getUserId() : 1);
                JOptionPane.showMessageDialog(this, "Request approved and account activated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rejectRequest(AccountRequest req) {
        String reason = JOptionPane.showInputDialog(this, "Enter reason for rejection:", "Reject Request", JOptionPane.PLAIN_MESSAGE);
        if (reason != null) {
            try {
                userDAO.reviewAccountRequest(req.getRequestId(), "Rejected", reason, AuthSession.getCurrentUser() != null ? AuthSession.getCurrentUser().getUserId() : 1);
                JOptionPane.showMessageDialog(this, "Request marked as rejected.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPermissionsMatrixDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "System Role & Permissions Reference Matrix", true);
        dlg.setSize(750, 480);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UIUtils.COLOR_BG);

        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.setOpaque(false);

        JLabel lblInfo = new JLabel("Role Access Baseline Matrix (Admins can override per user):");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(lblInfo, BorderLayout.NORTH);

        String[] cols = {"Module Name", "Cashier", "Inventory Clerk", "Auditor", "Admin"};
        Object[][] matrixData = {
                {"Dashboard Overview", "No", "No", "Yes (Read)", "Yes (Full)"},
                {"Point of Sale (POS)", "Yes (Checkout)", "No", "No", "Yes (Full)"},
                {"View Inventory", "Yes (Read-only)", "Yes (Read-only)", "Yes (Read-only)", "Yes (Full)"},
                {"Manage Inventory", "No", "Yes (Restock/Open)", "No", "Yes (Full)"},
                {"Products & Pricing", "No", "Yes (Catalog)", "No", "Yes (Full)"},
                {"Transaction History", "Yes (Own Sales)", "No", "Yes (All)", "Yes (Full)"},
                {"Sales Report & Analytics", "Yes (Summary)", "No", "Yes (Audit)", "Yes (Full)"},
                {"File Management & Backups", "No", "No", "No", "Yes (Full)"},
                {"Audit History & Trail", "No", "No", "Yes (Review)", "Yes (Full)"},
                {"User Management", "No", "No", "No", "Yes (Full)"}
        };

        JTable tblMatrix = new JTable(matrixData, cols);
        UIUtils.styleTable(tblMatrix);
        p.add(new JScrollPane(tblMatrix), BorderLayout.CENTER);

        JButton btnClose = UIUtils.createPrimaryButton("Close Reference");
        btnClose.addActionListener(e -> dlg.dispose());
        JPanel bWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bWrap.setOpaque(false);
        bWrap.add(btnClose);
        p.add(bWrap, BorderLayout.SOUTH);

        dlg.add(p);
        dlg.setVisible(true);
    }
}
