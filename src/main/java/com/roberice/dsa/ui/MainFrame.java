package com.roberice.dsa.ui;

import com.roberice.dsa.ui.components.Sidebar;
import com.roberice.dsa.ui.components.Topbar;
import com.roberice.dsa.ui.views.*;
import com.roberice.dsa.util.Session;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private Sidebar sidebar;
    private Topbar topbar;
    private JPanel cardsPanel;
    private CardLayout cardLayout;

    // View panels
    private DashboardView dashboardView;
    private PosView posView;
    private ViewInventoryView viewInventoryView;
    private ManageInventoryView manageInventoryView;
    private ProductsView productsView;
    private TransactionsView transactionsView;
    private SalesReportView salesReportView;
    private FileManagerView fileManagerView;
    private AuditView auditView;
    private UsersView usersView;

    private String currentViewId = "dashboard";

    public MainFrame() {
        setTitle("Roberice DSA - Rice Trading & Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG_MAIN);

        initComponents();

        // Navigate to default authorized view
        if (Session.hasPermission("dashboard")) {
            navigateTo("dashboard");
        } else if (Session.hasPermission("pos")) {
            navigateTo("pos");
        } else {
            navigateTo("view-inventory");
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Sidebar
        sidebar = new Sidebar(this::navigateTo, this::handleLogout);
        add(sidebar, BorderLayout.WEST);

        // Right side container (Topbar + Cards)
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setBackground(UIUtils.BG_MAIN);

        topbar = new Topbar(this::refreshCurrentView);
        rightContainer.add(topbar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(UIUtils.BG_MAIN);

        // Instantiate view panels
        dashboardView = new DashboardView(this::navigateTo);
        posView = new PosView();
        viewInventoryView = new ViewInventoryView();
        manageInventoryView = new ManageInventoryView();
        productsView = new ProductsView();
        transactionsView = new TransactionsView();
        salesReportView = new SalesReportView();
        fileManagerView = new FileManagerView();
        auditView = new AuditView();
        usersView = new UsersView();

        cardsPanel.add(dashboardView, "dashboard");
        cardsPanel.add(posView, "pos");
        cardsPanel.add(viewInventoryView, "view-inventory");
        cardsPanel.add(manageInventoryView, "inventory");
        cardsPanel.add(productsView, "products");
        cardsPanel.add(transactionsView, "transactions");
        cardsPanel.add(salesReportView, "sales-report");
        cardsPanel.add(fileManagerView, "file-manager");
        cardsPanel.add(auditView, "audit");
        cardsPanel.add(usersView, "users");

        rightContainer.add(cardsPanel, BorderLayout.CENTER);
        add(rightContainer, BorderLayout.CENTER);
    }

    public void navigateTo(String viewId) {
        if (!Session.hasPermission(viewId)) {
            UIUtils.showError(this, "Access Denied: You do not have permission to access this module.");
            return;
        }

        this.currentViewId = viewId;
        sidebar.setActive(viewId);
        cardLayout.show(cardsPanel, viewId);

        // Update Topbar
        switch (viewId) {
            case "dashboard":
                topbar.setHeader("Dashboard Overview", "Real-time key metrics, revenue trends, and operational alerts");
                dashboardView.loadData();
                break;
            case "pos":
                topbar.setHeader("Point of Sale (POS)", "Direct retail order processing, grain package selection, and instant invoicing");
                posView.loadProducts();
                break;
            case "view-inventory":
                topbar.setHeader("Warehouse Inventory", "Multi-tier stock levels across whole sacks, retail pouches, and loose grains");
                viewInventoryView.loadData();
                break;
            case "inventory":
                topbar.setHeader("Inventory Operations", "Stock replenishment (stock in), grain repacking, and spoilage write-offs");
                manageInventoryView.loadData();
                break;
            case "products":
                topbar.setHeader("Product Master List", "Rice variety catalog, origin classifications, and tiered price schedules");
                productsView.loadData();
                break;
            case "transactions":
                topbar.setHeader("Sales Transactions", "Comprehensive ledger of store orders, payment records, and invoice details");
                transactionsView.loadData();
                break;
            case "sales-report":
                topbar.setHeader("Sales Analytics & Reports", "Financial metrics, volume analysis, variety shares, and CSV reporting");
                salesReportView.loadReport();
                break;
            case "file-manager":
                topbar.setHeader("Document & File Manager", "System storage for digital invoices, stock reports, and database backups");
                fileManagerView.loadFiles();
                break;
            case "audit":
                topbar.setHeader("Audit Trail & Activity Log", "Chronological history of inventory adjustments and point-of-sale transactions");
                auditView.loadAuditLogs();
                break;
            case "users":
                topbar.setHeader("User & Access Management", "Staff credentials, role-based access control, and security permissions");
                usersView.loadUsers();
                break;
        }
    }

    private void refreshCurrentView() {
        navigateTo(currentViewId);
    }

    private void handleLogout() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
