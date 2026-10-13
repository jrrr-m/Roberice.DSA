package com.roberice.dsa.ui.components;

import com.roberice.dsa.model.User;
import com.roberice.dsa.util.Session;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        setPreferredSize(new Dimension(240, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.BORDER_COLOR));

        initComponents();
    }

    private void initComponents() {
        // --- TOP: BRAND & USER CARD ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(20, 18, 16, 18));

        // Brand Logo
        JLabel brandLabel = new JLabel("🌾 ROBERICE");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(UIUtils.TEXT_PRIMARY);
        brandLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subBrand = new JLabel("DSA INVENTORY & POS");
        subBrand.setFont(new Font("Segoe UI", Font.BOLD, 10));
        subBrand.setForeground(UIUtils.ACCENT_PRIMARY);
        subBrand.setAlignmentX(Component.LEFT_ALIGNMENT);

        topContainer.add(brandLabel);
        topContainer.add(Box.createVerticalStrut(2));
        topContainer.add(subBrand);
        topContainer.add(Box.createVerticalStrut(18));

        // User Card
        User user = Session.getCurrentUser();
        JPanel userCard = new JPanel(new BorderLayout(8, 0));
        userCard.setBackground(new Color(24, 34, 53));
        userCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        userCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        userCard.add(userIcon, BorderLayout.WEST);

        JPanel userMeta = new JPanel();
        userMeta.setLayout(new BoxLayout(userMeta, BoxLayout.Y_AXIS));
        userMeta.setOpaque(false);

        String name = (user != null && user.getFullName() != null) ? user.getFullName() : "Admin User";
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(UIUtils.FONT_BOLD);
        nameLbl.setForeground(UIUtils.TEXT_PRIMARY);

        String role = (user != null && user.getRole() != null) ? user.getRole().toUpperCase() : "ADMIN";
        JLabel roleLbl = new JLabel(role);
        roleLbl.setFont(UIUtils.FONT_SMALL);
        roleLbl.setForeground(UIUtils.ACCENT_SUCCESS);

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
        navContainer.setBorder(new EmptyBorder(8, 12, 8, 12));

        defineNavItems();

        for (NavItem item : navItems) {
            // Check permission
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
            btn.setBorder(new EmptyBorder(8, 14, 8, 14));

            btn.addActionListener(e -> {
                setActive(item.id);
                if (onNavigate != null) onNavigate.accept(item.id);
            });

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!item.id.equals(activeId)) {
                        btn.setBackground(UIUtils.BG_HOVER);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!item.id.equals(activeId)) {
                        btn.setBackground(UIUtils.BG_SIDEBAR);
                    }
                }
            });

            item.button = btn;
            navContainer.add(btn);
            navContainer.add(Box.createVerticalStrut(4));
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

        JButton logoutBtn = UIUtils.createDangerButton("🚪  Sign Out", e -> {
            if (UIUtils.showConfirm(this, "Are you sure you want to sign out?", "Confirm Logout")) {
                Session.logout();
                if (onLogout != null) onLogout.run();
            }
        });
        logoutBtn.setPreferredSize(new Dimension(0, 38));
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
                item.button.setBackground(UIUtils.ACCENT_PRIMARY);
                item.button.setForeground(Color.WHITE);
                item.button.setFont(UIUtils.FONT_BOLD);
            } else {
                item.button.setBackground(UIUtils.BG_SIDEBAR);
                item.button.setForeground(UIUtils.TEXT_SECONDARY);
                item.button.setFont(UIUtils.FONT_REGULAR);
            }
        }
    }
}
