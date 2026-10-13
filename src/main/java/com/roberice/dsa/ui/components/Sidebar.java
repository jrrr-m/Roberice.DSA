package com.roberice.dsa.ui.components;

import com.roberice.dsa.model.User;
import com.roberice.dsa.util.Session;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Sidebar extends JPanel {

    public static class NavItem {
        public String id;
        public String label;
        public String icon;
        public String permission;
        public JButton button;

        public NavItem(String id, String label, String icon, String permission) {
            this.id = id;
            this.label = label;
            this.icon = icon;
            this.permission = permission;
        }
    }

    private final Consumer<String> onNavigate;
    private final Runnable onLogout;
    private final List<NavItem> navItems = new ArrayList<>();
    private String activeId = "dashboard";

    public Sidebar(Consumer<String> onNavigate, Runnable onLogout) {
        this.onNavigate = onNavigate;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_SIDEBAR);
        setPreferredSize(new Dimension(250, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.BORDER_COLOR));

        initComponents();
    }

    private void initComponents() {
        // --- TOP: BRAND & USER CARD ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(22, 18, 16, 18));

        // Brand Logo Row
        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setOpaque(false);
        brandRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandIcon = new JLabel("🌾", SwingConstants.CENTER);
        brandIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        brandIcon.setPreferredSize(new Dimension(38, 38));
        brandIcon.setOpaque(true);
        brandIcon.setBackground(UIUtils.BG_MAIN);
        brandIcon.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIUtils.BRAND_GOLD, 1, true),
                new EmptyBorder(2, 2, 2, 2)
        ));

        JPanel brandTextPanel = new JPanel();
        brandTextPanel.setLayout(new BoxLayout(brandTextPanel, BoxLayout.Y_AXIS));
        brandTextPanel.setOpaque(false);

        JLabel brandLabel = new JLabel("ROBERICE");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        brandLabel.setForeground(UIUtils.PRIMARY);

        JLabel subBrand = new JLabel("DSA INVENTORY & POS");
        subBrand.setFont(new Font("Segoe UI", Font.BOLD, 10));
        subBrand.setForeground(UIUtils.BRAND_GOLD_HOVER);

        brandTextPanel.add(brandLabel);
        brandTextPanel.add(Box.createVerticalStrut(2));
        brandTextPanel.add(subBrand);

        brandRow.add(brandIcon);
        brandRow.add(brandTextPanel);

        topContainer.add(brandRow);
        topContainer.add(Box.createVerticalStrut(18));

        // User Card
        User user = Session.getCurrentUser();
        JPanel userCard = new JPanel(new BorderLayout(8, 0));
        userCard.setBackground(UIUtils.BG_CARD_ALT);
        userCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        userCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userAvatar = new JLabel("👤", SwingConstants.CENTER);
        userAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        userAvatar.setPreferredSize(new Dimension(34, 34));
        userAvatar.setOpaque(true);
        userAvatar.setBackground(UIUtils.PRIMARY_LIGHT);
        userAvatar.setBorder(new LineBorder(UIUtils.PRIMARY_BORDER, 1, true));
        userCard.add(userAvatar, BorderLayout.WEST);

        JPanel userMeta = new JPanel();
        userMeta.setLayout(new BoxLayout(userMeta, BoxLayout.Y_AXIS));
        userMeta.setOpaque(false);

        String name = (user != null && user.getFullName() != null) ? user.getFullName() : "Admin User";
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(UIUtils.FONT_BOLD);
        nameLbl.setForeground(UIUtils.TEXT_MAIN);

        String role = (user != null && user.getRole() != null) ? user.getRole().toUpperCase() : "ADMIN";
        JLabel roleLbl = new JLabel(role);
        roleLbl.setFont(UIUtils.FONT_SMALL);
        roleLbl.setForeground(UIUtils.PRIMARY);

        userMeta.add(nameLbl);
        userMeta.add(Box.createVerticalStrut(2));
        userMeta.add(roleLbl);
        userCard.add(userMeta, BorderLayout.CENTER);

        topContainer.add(userCard);
        add(topContainer, BorderLayout.NORTH);

        // --- CENTER: NAV ITEMS ---
        JPanel navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setOpaque(false);
        navContainer.setBorder(new EmptyBorder(8, 10, 8, 10));

        defineNavItems();

        for (NavItem item : navItems) {
            if (item.permission != null && !Session.hasPermission(item.permission)) {
                continue;
            }

            JButton btn = new JButton(item.icon + "  " + item.label);
            btn.setFont(UIUtils.FONT_REGULAR);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            btn.addActionListener(e -> {
                setActive(item.id);
                if (onNavigate != null) onNavigate.accept(item.id);
            });

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!item.id.equals(activeId)) {
                        btn.setBackground(new Color(241, 235, 216)); // #f1ebd8
                        btn.setForeground(UIUtils.TEXT_MAIN);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!item.id.equals(activeId)) {
                        btn.setBackground(UIUtils.BG_SIDEBAR);
                        btn.setForeground(UIUtils.TEXT_MUTED);
                    }
                }
            });

            item.button = btn;
            navContainer.add(btn);
            navContainer.add(Box.createVerticalStrut(3));
        }

        JScrollPane scroll = new JScrollPane(navContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // --- BOTTOM: LOGOUT ---
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(new EmptyBorder(12, 12, 16, 12));

        JButton logoutBtn = new JButton("🚪  Sign Out");
        logoutBtn.setFont(UIUtils.FONT_BOLD);
        logoutBtn.setBackground(UIUtils.BG_INPUT);
        logoutBtn.setForeground(UIUtils.TEXT_MUTED);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setBackground(UIUtils.DANGER_BG);
                logoutBtn.setForeground(UIUtils.DANGER_TEXT);
                logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UIUtils.DANGER_BORDER, 1, true),
                        new EmptyBorder(8, 14, 8, 14)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                logoutBtn.setBackground(UIUtils.BG_INPUT);
                logoutBtn.setForeground(UIUtils.TEXT_MUTED);
                logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                        new EmptyBorder(8, 14, 8, 14)
                ));
            }
        });

        logoutBtn.addActionListener(e -> {
            if (UIUtils.showConfirm(this, "Are you sure you want to sign out?", "Confirm Logout")) {
                Session.logout();
                if (onLogout != null) onLogout.run();
            }
        });

        bottomContainer.add(logoutBtn, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);

        updateActiveStyle();
    }

    private void defineNavItems() {
        navItems.add(new NavItem("dashboard", "Dashboard", "📊", "dashboard"));
        navItems.add(new NavItem("pos", "Point of Sale", "🛒", "pos"));
        navItems.add(new NavItem("view-inventory", "View Inventory", "📦", "view-inventory"));
        navItems.add(new NavItem("inventory", "Manage Inventory", "⚙️", "inventory"));
        navItems.add(new NavItem("products", "Product Master", "🌾", "products"));
        navItems.add(new NavItem("transactions", "Transactions", "🧾", "transactions"));
        navItems.add(new NavItem("sales-report", "Sales Analytics", "📈", "sales-report"));
        navItems.add(new NavItem("file-manager", "File Manager", "📁", "file-manager"));
        navItems.add(new NavItem("audit", "Audit Trail", "🕒", "audit"));
        navItems.add(new NavItem("users", "User Management", "👥", "users"));
    }

    public void setActive(String id) {
        this.activeId = id;
        updateActiveStyle();
    }

    private void updateActiveStyle() {
        for (NavItem item : navItems) {
            if (item.button == null) continue;
            if (item.id.equals(activeId)) {
                item.button.setBackground(UIUtils.PRIMARY_LIGHT);
                item.button.setForeground(UIUtils.PRIMARY);
                item.button.setFont(UIUtils.FONT_BOLD);
                item.button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, UIUtils.BRAND_GOLD),
                        new EmptyBorder(8, 12, 8, 14)
                ));
            } else {
                item.button.setBackground(UIUtils.BG_SIDEBAR);
                item.button.setForeground(UIUtils.TEXT_MUTED);
                item.button.setFont(UIUtils.FONT_REGULAR);
                item.button.setBorder(new EmptyBorder(8, 16, 8, 14));
            }
        }
    }
}
