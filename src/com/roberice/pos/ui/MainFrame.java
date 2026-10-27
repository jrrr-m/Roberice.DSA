package com.roberice.pos.ui;

import com.roberice.pos.model.User;
import com.roberice.pos.service.AuthSession;
import com.roberice.pos.ui.common.UIUtils;
import com.roberice.pos.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Application Window for Roberice POS & Inventory Management System.
 * Features unified sidebar navigation, user profile controls, and dynamic header actions.
 */
public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);

    private final JLabel lblTopTitle = new JLabel("Dashboard");
    private final JLabel lblTopSubtitle = new JLabel("Store overview, KPI performance, and sales trend analytics.");
    private final JPanel headerActionCenter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

    private final JPanel navListPanel = new JPanel();
    private final Map<String, JButton> navButtons = new HashMap<>();

    // Child Panels
    private DashboardPanel dashboardPanel;
    private PosPanel posPanel;
    private ViewInventoryPanel viewInventoryPanel;
    private InventoryManagePanel inventoryManagePanel;
    private ProductsPanel productsPanel;
    private TransactionsPanel transactionsPanel;
    private SalesReportPanel salesReportPanel;
    private FileManagerPanel fileManagerPanel;
    private AuditPanel auditPanel;
    private UsersPanel usersPanel;

    public MainFrame() {
        super("Roberice - POS & Multi-Packaging Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 840);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.COLOR_BG);

        // 1. Sidebar Panel
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // 2. Right Main Container (Top Nav + Cards)
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(UIUtils.COLOR_BG);

        JPanel topNav = createTopNav();
        mainContainer.add(topNav, BorderLayout.NORTH);

        initCards();
        mainContainer.add(cardsPanel, BorderLayout.CENTER);

        root.add(mainContainer, BorderLayout.CENTER);
        setContentPane(root);

        // Navigate to default authorized view
        navigateToDefault();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(245, 800));
        sidebar.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(30, 41, 59)));

        // Brand Header (Synchronized ~81px height)
        JPanel brandPanel = new JPanel(new BorderLayout(0, 4));
        brandPanel.setPreferredSize(new Dimension(245, 81));
        brandPanel.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        brandPanel.setBorder(new EmptyBorder(18, 20, 14, 20));

        JLabel lblBrand = new JLabel("ROBERICE");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBrand.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Wholesale & Retail Rice");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(148, 163, 184));

        brandPanel.add(lblBrand, BorderLayout.NORTH);
        brandPanel.add(lblSub, BorderLayout.CENTER);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Links
        navListPanel.setLayout(new BoxLayout(navListPanel, BoxLayout.Y_AXIS));
        navListPanel.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        navListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        buildNavMenu();

        JScrollPane navScroll = new JScrollPane(navListPanel);
        navScroll.setBorder(null);
        navScroll.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        navScroll.getViewport().setBackground(UIUtils.COLOR_SIDEBAR_BG);
        sidebar.add(navScroll, BorderLayout.CENTER);

        // User Profile Card & Logout (Sidebar Footer)
        JPanel userCard = new JPanel(new BorderLayout(8, 0));
        userCard.setBackground(new Color(30, 41, 59));
        userCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(51, 65, 85)),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel uInfo = new JPanel(new GridLayout(2, 1, 2, 2));
        uInfo.setBackground(new Color(30, 41, 59));

        JLabel lblUName = new JLabel(AuthSession.getFullName());
        lblUName.setFont(UIUtils.FONT_BOLD);
        lblUName.setForeground(Color.WHITE);

        JLabel lblURole = new JLabel(AuthSession.getRole());
        lblURole.setFont(UIUtils.FONT_SMALL);
        lblURole.setForeground(new Color(148, 163, 184));

        uInfo.add(lblUName);
        uInfo.add(lblURole);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UIUtils.FONT_SMALL);
        btnLogout.setForeground(new Color(248, 113, 113)); // Soft red
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> doLogout());

        userCard.add(uInfo, BorderLayout.CENTER);
        userCard.add(btnLogout, BorderLayout.EAST);

        sidebar.add(userCard, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel createTopNav() {
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setPreferredSize(new Dimension(800, 81));
        topNav.setBackground(Color.WHITE);
        topNav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 2, 2));
        titleBlock.setBackground(Color.WHITE);

        lblTopTitle.setFont(UIUtils.FONT_HEADER_TITLE);
        lblTopTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        lblTopSubtitle.setFont(UIUtils.FONT_SUBTITLE);
        lblTopSubtitle.setForeground(UIUtils.COLOR_TEXT_MUTED);

        titleBlock.add(lblTopTitle);
        titleBlock.add(lblTopSubtitle);

        headerActionCenter.setBackground(Color.WHITE);

        topNav.add(titleBlock, BorderLayout.WEST);
        topNav.add(headerActionCenter, BorderLayout.EAST);
        return topNav;
    }

    private void buildNavMenu() {
        navListPanel.removeAll();
        navButtons.clear();

        addNavItem("Dashboard", "dashboard");
        addNavItem("Point of Sale", "pos");
        addNavItem("View Inventory", "view-inventory");
        addNavItem("Manage Inventory", "inventory");
        addNavItem("Products & Pricing", "products");
        addNavItem("Transactions", "transactions");
        addNavItem("Sales Report", "sales-report");
        addNavItem("File Manager", "file-manager");
        addNavItem("Audit History", "audit");
        addNavItem("Users & Roles", "users");

        navListPanel.revalidate();
        navListPanel.repaint();
    }

    private void addNavItem(String label, String moduleKey) {
        if (!AuthSession.hasPermission(moduleKey)) {
            return; // Route guard: only display modules the user is granted access to
        }

        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(225, 40));
        btn.setPreferredSize(new Dimension(225, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> navigateTo(moduleKey));

        navButtons.put(moduleKey, btn);
        navListPanel.add(btn);
        navListPanel.add(Box.createVerticalStrut(4));
    }

    private void initCards() {
        dashboardPanel = new DashboardPanel();
        posPanel = new PosPanel();
        viewInventoryPanel = new ViewInventoryPanel();
        inventoryManagePanel = new InventoryManagePanel();
        productsPanel = new ProductsPanel();
        transactionsPanel = new TransactionsPanel();
        salesReportPanel = new SalesReportPanel();
        fileManagerPanel = new FileManagerPanel();
        auditPanel = new AuditPanel();
        usersPanel = new UsersPanel();

        cardsPanel.add(dashboardPanel, "dashboard");
        cardsPanel.add(posPanel, "pos");
        cardsPanel.add(viewInventoryPanel, "view-inventory");
        cardsPanel.add(inventoryManagePanel, "inventory");
        cardsPanel.add(productsPanel, "products");
        cardsPanel.add(transactionsPanel, "transactions");
        cardsPanel.add(salesReportPanel, "sales-report");
        cardsPanel.add(fileManagerPanel, "file-manager");
        cardsPanel.add(auditPanel, "audit");
        cardsPanel.add(usersPanel, "users");
    }

    public void navigateTo(String moduleKey) {
        if (!AuthSession.hasPermission(moduleKey)) {
            JOptionPane.showMessageDialog(this, "Access Denied: You do not have permission to access the " + moduleKey + " module.", "Unauthorized", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Highlight active nav button
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            if (entry.getKey().equals(moduleKey)) {
                b.setBackground(UIUtils.COLOR_SIDEBAR_ACTIVE);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(UIUtils.COLOR_SIDEBAR_BG);
                b.setForeground(new Color(203, 213, 225));
            }
        }

        headerActionCenter.removeAll();

        switch (moduleKey) {
            case "dashboard":
                lblTopTitle.setText("Dashboard");
                lblTopSubtitle.setText("Executive store overview, KPI metrics, and sales trend analytics.");
                JButton btnRefreshDash = UIUtils.createSecondaryButton("Refresh Analytics");
                btnRefreshDash.addActionListener(e -> dashboardPanel.refreshData());
                headerActionCenter.add(btnRefreshDash);
                dashboardPanel.refreshData();
                break;
            case "pos":
                lblTopTitle.setText("Point of Sale");
                lblTopSubtitle.setText("Process customer basket orders, multi-packaging calculation, and checkout receipts.");
                posPanel.refreshCatalog();
                break;
            case "view-inventory":
                lblTopTitle.setText("View Inventory");
                lblTopSubtitle.setText("Real-time aggregated warehouse balances and dynamic FIFO delivery lot drill-downs.");
                viewInventoryPanel.refreshData();
                break;
            case "inventory":
                lblTopTitle.setText("Manage Inventory");
                lblTopSubtitle.setText("Warehouse restocks, sack openings into loose inventory, and physical count corrections.");
                inventoryManagePanel.refreshData();
                break;
            case "products":
                lblTopTitle.setText("Products & Pricing");
                lblTopSubtitle.setText("Rice varieties catalog, pricing configuration across package sizes, and origin setup.");
                productsPanel.refreshData();
                break;
            case "transactions":
                lblTopTitle.setText("Transactions");
                lblTopSubtitle.setText("Searchable historical customer receipts log, breakdown inspection, and receipt reprinting.");
                transactionsPanel.refreshData();
                break;
            case "sales-report":
                lblTopTitle.setText("Sales Report");
                lblTopSubtitle.setText("Multi-period financial analytics (Daily, Weekly, Monthly, Quarterly, Yearly) and CSV exports.");
                salesReportPanel.generateReport();
                break;
            case "file-manager":
                lblTopTitle.setText("File Management");
                lblTopSubtitle.setText("Store document repository, invoice management, and automated one-click database backups.");
                fileManagerPanel.refreshData();
                break;
            case "audit":
                lblTopTitle.setText("Audit History");
                lblTopSubtitle.setText("System audit trail recording warehouse restocks, sack openings, repacking, and write-offs.");
                auditPanel.refreshData();
                break;
            case "users":
                lblTopTitle.setText("User Management");
                lblTopSubtitle.setText("Staff accounts, password configuration, role presets, and granular 10-module permissions.");
                usersPanel.refreshData();
                break;
        }

        headerActionCenter.revalidate();
        headerActionCenter.repaint();

        cardLayout.show(cardsPanel, moduleKey);
    }

    private void navigateToDefault() {
        if (AuthSession.hasPermission("dashboard")) {
            navigateTo("dashboard");
        } else if (AuthSession.hasPermission("pos")) {
            navigateTo("pos");
        } else if (AuthSession.hasPermission("view-inventory")) {
            navigateTo("view-inventory");
        } else {
            for (String mod : User.ALL_MODULES) {
                if (AuthSession.hasPermission(mod)) {
                    navigateTo(mod);
                    return;
                }
            }
        }
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            AuthSession.logout();
            dispose();
            RobericeApp.showLogin();
        }
    }
}
