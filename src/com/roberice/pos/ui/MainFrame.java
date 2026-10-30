package com.roberice.pos.ui;

import com.roberice.pos.model.User;
import com.roberice.pos.service.AuthSession;
import com.roberice.pos.ui.common.UIUtils;
import com.roberice.pos.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Main Application Window for Roberice POS & Inventory Management System.
 * Styled in warm ivory, deep forest green accents, vector icons, and refined typography.
 */
public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);

    private final JLabel lblTopTitle = new JLabel("Dashboard");
    private final JLabel lblTopSubtitle = new JLabel("Welcome back, " + AuthSession.getFullName() + "! Here's your store overview.");
    private final JPanel headerActionCenter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

    private final JPanel navListPanel = new JPanel();
    private final Map<String, NavButton> navButtons = new HashMap<>();

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
        super("Roberice - POS & Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 840);
        setMinimumSize(new Dimension(960, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximized by default for POS/Desktop environments
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
        sidebar.setPreferredSize(new Dimension(225, 800));
        sidebar.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.COLOR_BORDER));

        // Brand Header with circular logo
        JPanel brandPanel = new JPanel(new BorderLayout(10, 0));
        brandPanel.setPreferredSize(new Dimension(225, 75));
        brandPanel.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        brandPanel.setBorder(new EmptyBorder(14, 16, 12, 16));

        JLabel lblLogo = new JLabel();
        ImageIcon logoIcon = loadLogoIcon(40, 40);
        if (logoIcon != null) {
            lblLogo.setIcon(logoIcon);
        }

        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 1));
        brandText.setOpaque(false);
        JLabel lblBrand = new JLabel("Roberice POS");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblBrand.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Sales & Inventory System");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        brandText.add(lblBrand);
        brandText.add(lblSub);

        brandPanel.add(lblLogo, BorderLayout.WEST);
        brandPanel.add(brandText, BorderLayout.CENTER);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Links
        navListPanel.setLayout(new BoxLayout(navListPanel, BoxLayout.Y_AXIS));
        navListPanel.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        navListPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        buildNavMenu();

        JScrollPane navScroll = new JScrollPane(navListPanel);
        navScroll.setBorder(null);
        navScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        navScroll.getViewport().setBackground(UIUtils.COLOR_SIDEBAR_BG);
        sidebar.add(navScroll, BorderLayout.CENTER);

        // User Profile & Logout (Sidebar Footer)
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JPanel userRow = new JPanel(new BorderLayout(8, 0));
        userRow.setOpaque(false);

        JPanel uInfo = new JPanel(new GridLayout(2, 1, 0, 1));
        uInfo.setOpaque(false);

        JLabel lblUName = new JLabel(AuthSession.getFullName());
        lblUName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUName.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel lblURole = new JLabel(AuthSession.getRole() + " (Full Access)");
        lblURole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblURole.setForeground(UIUtils.COLOR_TEXT_MUTED);

        uInfo.add(lblUName);
        uInfo.add(lblURole);

        JButton btnLogout = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.setColor(UIUtils.COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);

                // Draw door exit icon
                g2.setColor(UIUtils.COLOR_TEXT_MUTED);
                g2.drawRect(8, 7, 7, 11);
                g2.drawLine(15, 12, 18, 12);
                g2.drawLine(17, 10, 19, 12);
                g2.drawLine(17, 14, 19, 12);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnLogout.setForeground(UIUtils.COLOR_TEXT_MUTED);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(new EmptyBorder(4, 22, 4, 8));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> doLogout());

        userRow.add(uInfo, BorderLayout.CENTER);
        userRow.add(btnLogout, BorderLayout.EAST);

        JLabel lblCopy = new JLabel("© 2026 Roberice System v1.0");
        lblCopy.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblCopy.setForeground(new Color(160, 165, 160));
        lblCopy.setBorder(new EmptyBorder(6, 0, 0, 0));

        footerPanel.add(userRow);
        footerPanel.add(lblCopy);

        sidebar.add(footerPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private ImageIcon loadLogoIcon(int width, int height) {
        try {
            File f = new File("resources/logo.png");
            if (!f.exists()) f = new File("src/resources/logo.png");
            if (f.exists()) {
                Image img = ImageIO.read(f);
                return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
            InputStream is = getClass().getResourceAsStream("/resources/logo.png");
            if (is != null) {
                Image img = ImageIO.read(is);
                return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private JPanel createTopNav() {
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setPreferredSize(new Dimension(800, 68));
        topNav.setBackground(UIUtils.COLOR_BG);
        topNav.setBorder(new EmptyBorder(16, 24, 8, 24));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBlock.setOpaque(false);

        lblTopTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTopTitle.setForeground(new Color(27, 77, 50));

        lblTopSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTopSubtitle.setForeground(UIUtils.COLOR_TEXT_MUTED);

        titleBlock.add(lblTopTitle);
        titleBlock.add(lblTopSubtitle);

        headerActionCenter.setOpaque(false);

        topNav.add(titleBlock, BorderLayout.WEST);
        topNav.add(headerActionCenter, BorderLayout.EAST);
        return topNav;
    }

    private void buildNavMenu() {
        navListPanel.removeAll();
        navButtons.clear();

        addNavItem("Dashboard", "dashboard", "dashboard");
        addNavItem("Point of Sale", "pos", "pos");
        addNavItem("View Inventory", "view-inventory", "view");
        addNavItem("Manage Inventory", "inventory", "manage");
        addNavItem("Products & Pricing", "products", "products");
        addNavItem("Transaction History", "transactions", "transactions");
        addNavItem("Sales Report", "sales-report", "report");
        addNavItem("File Management", "file-manager", "files");
        addNavItem("Audit History", "audit", "audit");
        addNavItem("User Management", "users", "users");

        navListPanel.revalidate();
        navListPanel.repaint();
    }

    private void addNavItem(String label, String moduleKey, String iconType) {
        if (!AuthSession.hasPermission(moduleKey)) {
            return;
        }

        NavButton btn = new NavButton(label, iconType);
        btn.addActionListener(e -> navigateTo(moduleKey));

        navButtons.put(moduleKey, btn);
        navListPanel.add(btn);
        navListPanel.add(Box.createVerticalStrut(3));
    }

    private static class NavButton extends JButton {
        private boolean active = false;
        private final String iconType;

        public NavButton(String text, String iconType) {
            super(text);
            this.iconType = iconType;
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setForeground(new Color(80, 95, 85));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setMaximumSize(new Dimension(205, 36));
            setPreferredSize(new Dimension(205, 36));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(6, 36, 6, 12));
        }

        public void setActive(boolean active) {
            this.active = active;
            if (active) {
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(new Color(27, 77, 50));
            } else {
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                setForeground(new Color(80, 95, 85));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                g2.setColor(new Color(231, 241, 233));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(245, 242, 234));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }

            // Draw crisp vector icon at x=12, y=10
            Color icColor = active ? new Color(27, 77, 50) : new Color(120, 135, 125);
            g2.setColor(icColor);
            drawVectorIcon(g2, iconType, 12, 10);

            g2.dispose();
            super.paintComponent(g);
        }

        private void drawVectorIcon(Graphics2D g2, String type, int x, int y) {
            g2.setStroke(new BasicStroke(1.4f));
            switch (type) {
                case "dashboard": // 4 squares grid
                    g2.drawRoundRect(x, y, 6, 6, 2, 2);
                    g2.drawRoundRect(x + 8, y, 6, 6, 2, 2);
                    g2.drawRoundRect(x, y + 8, 6, 6, 2, 2);
                    g2.drawRoundRect(x + 8, y + 8, 6, 6, 2, 2);
                    break;
                case "pos": // Shopping cart
                    g2.drawLine(x, y + 2, x + 3, y + 2);
                    g2.drawLine(x + 3, y + 2, x + 5, y + 10);
                    g2.drawLine(x + 5, y + 10, x + 13, y + 10);
                    g2.drawLine(x + 13, y + 10, x + 14, y + 4);
                    g2.drawLine(x + 3, y + 4, x + 14, y + 4);
                    g2.drawOval(x + 5, y + 12, 2, 2);
                    g2.drawOval(x + 11, y + 12, 2, 2);
                    break;
                case "view": // Eye
                    g2.drawArc(x, y + 2, 15, 10, 0, 180);
                    g2.drawArc(x, y + 2, 15, 10, 180, 180);
                    g2.fillOval(x + 6, y + 6, 3, 3);
                    break;
                case "manage": // Box / Package
                    g2.drawRect(x + 1, y + 2, 13, 11);
                    g2.drawLine(x + 1, y + 5, x + 14, y + 5);
                    g2.drawLine(x + 7, y + 5, x + 7, y + 13);
                    break;
                case "products": // Tag / Price
                    g2.drawRoundRect(x + 1, y + 2, 13, 10, 3, 3);
                    g2.drawLine(x + 4, y + 7, x + 11, y + 7);
                    break;
                case "transactions": // Clock / History
                    g2.drawOval(x + 1, y + 1, 13, 13);
                    g2.drawLine(x + 7, y + 4, x + 7, y + 8);
                    g2.drawLine(x + 7, y + 8, x + 10, y + 8);
                    break;
                case "report": // Document / Sheet
                    g2.drawRect(x + 2, y + 1, 10, 13);
                    g2.drawLine(x + 4, y + 4, x + 10, y + 4);
                    g2.drawLine(x + 4, y + 7, x + 10, y + 7);
                    g2.drawLine(x + 4, y + 10, x + 8, y + 10);
                    break;
                case "files": // Folder
                    g2.drawLine(x + 1, y + 3, x + 5, y + 3);
                    g2.drawLine(x + 5, y + 3, x + 7, y + 5);
                    g2.drawLine(x + 7, y + 5, x + 13, y + 5);
                    g2.drawRect(x + 1, y + 5, 12, 9);
                    break;
                case "audit": // Clipboard
                    g2.drawRect(x + 2, y + 3, 11, 12);
                    g2.drawRect(x + 5, y + 1, 5, 3);
                    g2.drawLine(x + 5, y + 7, x + 10, y + 7);
                    g2.drawLine(x + 5, y + 10, x + 10, y + 10);
                    break;
                case "users": // Users silhouette
                    g2.drawOval(x + 5, y + 2, 5, 5);
                    g2.drawArc(x + 2, y + 8, 11, 8, 0, 180);
                    break;
            }
        }
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

        cardsPanel.setBackground(UIUtils.COLOR_BG);
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
        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(moduleKey));
        }

        headerActionCenter.removeAll();

        switch (moduleKey) {
            case "dashboard":
                lblTopTitle.setText("Dashboard");
                lblTopSubtitle.setText("Welcome back, " + AuthSession.getFullName() + "! Here's your store overview.");
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
                lblTopSubtitle.setText("Manage rice catalog, base sack pricing, and per-kg retail rates.");
                JButton btnAddNewRice = new JButton("+ Add New Rice Variety") {
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
                btnAddNewRice.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnAddNewRice.setForeground(Color.WHITE);
                btnAddNewRice.setContentAreaFilled(false);
                btnAddNewRice.setBorderPainted(false);
                btnAddNewRice.setFocusPainted(false);
                btnAddNewRice.setBorder(new EmptyBorder(8, 16, 8, 16));
                btnAddNewRice.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnAddNewRice.addActionListener(e -> productsPanel.triggerAddProduct());
                headerActionCenter.add(btnAddNewRice);
                productsPanel.refreshData();
                break;
            case "transactions":
                lblTopTitle.setText("Transaction History");
                lblTopSubtitle.setText("View sales records and customer transaction details.");
                transactionsPanel.refreshData();
                break;
            case "sales-report":
                lblTopTitle.setText("Sales Report & Analytics");
                lblTopSubtitle.setText("Comprehensive store revenue reports, variety sales volume, and print-ready statements.");
                
                JButton btnExportCsv = new JButton("📄 Export CSV");
                btnExportCsv.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                btnExportCsv.setForeground(new Color(40, 50, 45));
                btnExportCsv.setBackground(Color.WHITE);
                btnExportCsv.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 220, 210), 1),
                        new EmptyBorder(6, 12, 6, 12)
                ));
                btnExportCsv.setFocusPainted(false);
                btnExportCsv.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnExportCsv.addActionListener(e -> salesReportPanel.exportCsv());

                JButton btnPrintReport = new JButton("🖨 Print Sales Report") {
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
                btnPrintReport.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnPrintReport.setForeground(Color.WHITE);
                btnPrintReport.setContentAreaFilled(false);
                btnPrintReport.setBorderPainted(false);
                btnPrintReport.setFocusPainted(false);
                btnPrintReport.setBorder(new EmptyBorder(8, 16, 8, 16));
                btnPrintReport.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnPrintReport.addActionListener(e -> salesReportPanel.printStatement());

                headerActionCenter.add(btnExportCsv);
                headerActionCenter.add(btnPrintReport);
                salesReportPanel.generateReport();
                break;
            case "file-manager":
                lblTopTitle.setText("File & Document Management");
                lblTopSubtitle.setText("Organize store receipts, supplier delivery invoices, audit records, and backup files.");

                JButton btnOneClickBackup = new JButton("📦 One-Click Database Backup");
                btnOneClickBackup.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                btnOneClickBackup.setForeground(new Color(40, 50, 45));
                btnOneClickBackup.setBackground(Color.WHITE);
                btnOneClickBackup.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 220, 210), 1),
                        new EmptyBorder(6, 12, 6, 12)
                ));
                btnOneClickBackup.setFocusPainted(false);
                btnOneClickBackup.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnOneClickBackup.addActionListener(e -> fileManagerPanel.triggerBackup());

                JButton btnImportRestore = new JButton("⬆ Import / Restore DB");
                btnImportRestore.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                btnImportRestore.setForeground(new Color(40, 50, 45));
                btnImportRestore.setBackground(Color.WHITE);
                btnImportRestore.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 220, 210), 1),
                        new EmptyBorder(6, 12, 6, 12)
                ));
                btnImportRestore.setFocusPainted(false);
                btnImportRestore.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnImportRestore.addActionListener(e -> fileManagerPanel.triggerRestore());

                JButton btnUploadNewDoc = new JButton("+ Upload New Document") {
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
                btnUploadNewDoc.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnUploadNewDoc.setForeground(Color.WHITE);
                btnUploadNewDoc.setContentAreaFilled(false);
                btnUploadNewDoc.setBorderPainted(false);
                btnUploadNewDoc.setFocusPainted(false);
                btnUploadNewDoc.setBorder(new EmptyBorder(8, 16, 8, 16));
                btnUploadNewDoc.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnUploadNewDoc.addActionListener(e -> fileManagerPanel.uploadDocument());

                headerActionCenter.add(btnOneClickBackup);
                headerActionCenter.add(btnImportRestore);
                headerActionCenter.add(btnUploadNewDoc);

                fileManagerPanel.refreshData();
                break;
            case "audit":
                lblTopTitle.setText("Audit History & System Trail");
                lblTopSubtitle.setText("Chronological audit log tracking warehouse adjustments, sack openings, and batch depletions.");

                JButton btnExportAuditCsv = new JButton("📄 Export CSV");
                btnExportAuditCsv.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                btnExportAuditCsv.setForeground(new Color(40, 50, 45));
                btnExportAuditCsv.setBackground(Color.WHITE);
                btnExportAuditCsv.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 220, 210), 1),
                        new EmptyBorder(6, 12, 6, 12)
                ));
                btnExportAuditCsv.setFocusPainted(false);
                btnExportAuditCsv.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnExportAuditCsv.addActionListener(e -> auditPanel.exportCsv());

                JButton btnRefreshLog = new JButton("🔄 Refresh Log") {
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
                btnRefreshLog.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnRefreshLog.setForeground(Color.WHITE);
                btnRefreshLog.setContentAreaFilled(false);
                btnRefreshLog.setBorderPainted(false);
                btnRefreshLog.setFocusPainted(false);
                btnRefreshLog.setBorder(new EmptyBorder(8, 16, 8, 16));
                btnRefreshLog.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnRefreshLog.addActionListener(e -> auditPanel.refreshData());

                headerActionCenter.add(btnExportAuditCsv);
                headerActionCenter.add(btnRefreshLog);

                auditPanel.refreshData();
                break;
            case "users":
                lblTopTitle.setText("User Management & Permissions");
                lblTopSubtitle.setText("Add system users, assign roles, and configure granular module permissions.");
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
