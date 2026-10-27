package com.roberice.pos.ui.panels;

import com.roberice.pos.db.DatabaseManager;
import com.roberice.pos.db.InventoryDAO;
import com.roberice.pos.db.ProductDAO;
import com.roberice.pos.db.SalesDAO;
import com.roberice.pos.model.Inventory;
import com.roberice.pos.model.Product;
import com.roberice.pos.model.SalesTransaction;
import com.roberice.pos.ui.common.CustomCharts;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Executive Store Dashboard & Analytics Panel (Section 2.2 in User Manual).
 */
public class DashboardPanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final SalesDAO salesDAO = new SalesDAO();

    // Metric Labels
    private final JLabel lblVarieties = new JLabel("0");
    private final JLabel lblInventoryValue = new JLabel("₱ 0.00");
    private final JLabel lbl7DaySales = new JLabel("₱ 0.00");
    private final JLabel lblLowStock = new JLabel("0");
    private final JLabel lblAgingStock = new JLabel("0");

    // Charts
    private final CustomCharts.SalesTrendBarChart barChart = new CustomCharts.SalesTrendBarChart();
    private final CustomCharts.VarietyDonutChart donutChart = new CustomCharts.VarietyDonutChart();

    // Recent Transactions Table
    private final DefaultTableModel recentTableModel = new DefaultTableModel(
            new String[]{"Receipt #", "Date & Time", "Cashier", "Payment", "Total Amount"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblRecent = new JTable(recentTableModel);

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIUtils.COLOR_BG);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. KPI Cards Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 5, 14, 0));
        kpiRow.setBackground(UIUtils.COLOR_BG);
        kpiRow.setMaximumSize(new Dimension(2000, 95));

        kpiRow.add(createKpiCard("Active Varieties", lblVarieties, UIUtils.COLOR_PRIMARY));
        kpiRow.add(createKpiCard("Inventory Value", lblInventoryValue, UIUtils.COLOR_SUCCESS));
        kpiRow.add(createKpiCard("7-Day Sales", lbl7DaySales, UIUtils.COLOR_INFO));
        kpiRow.add(createKpiCard("Low Stock Alerts", lblLowStock, UIUtils.COLOR_WARNING));
        kpiRow.add(createKpiCard("Aging Stock (>60d)", lblAgingStock, UIUtils.COLOR_DANGER));

        // 2. Charts Row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setBackground(UIUtils.COLOR_BG);
        chartsRow.setMaximumSize(new Dimension(2000, 280));

        // Bar Chart Card
        JPanel barCard = UIUtils.createCard();
        barCard.setLayout(new BorderLayout(0, 10));
        JLabel barTitle = new JLabel("7-Day Sales Trend (PHP)");
        barTitle.setFont(UIUtils.FONT_TITLE);
        barTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        barCard.add(barTitle, BorderLayout.NORTH);
        barCard.add(barChart, BorderLayout.CENTER);

        // Donut Chart Card
        JPanel donutCard = UIUtils.createCard();
        donutCard.setLayout(new BorderLayout(0, 10));
        JLabel donutTitle = new JLabel("Variety Sales Distribution");
        donutTitle.setFont(UIUtils.FONT_TITLE);
        donutTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        donutCard.add(donutTitle, BorderLayout.NORTH);
        donutCard.add(donutChart, BorderLayout.CENTER);

        chartsRow.add(barCard);
        chartsRow.add(donutCard);

        // 3. Recent Transactions Table Card
        JPanel tableCard = UIUtils.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel tblTitle = new JLabel("Recent Store Transactions");
        tblTitle.setFont(UIUtils.FONT_TITLE);
        tblTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        tableCard.add(tblTitle, BorderLayout.NORTH);

        UIUtils.styleTable(tblRecent);
        JScrollPane scrollTable = new JScrollPane(tblRecent);
        scrollTable.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        tableCard.add(scrollTable, BorderLayout.CENTER);

        content.add(kpiRow);
        content.add(Box.createVerticalStrut(18));
        content.add(chartsRow);
        content.add(Box.createVerticalStrut(18));
        content.add(tableCard);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createKpiCard(String title, JLabel valueLbl, Color accentColor) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(0, 4));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIUtils.FONT_SMALL);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MUTED);

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLbl.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        try {
            // 1. KPI: Products & Inventory Value
            List<Product> products = productDAO.getAllProducts(true);
            lblVarieties.setText(String.valueOf(products.size()));

            List<Inventory> inventories = inventoryDAO.getInventorySummary();
            double totalValue = 0;
            int lowStockCount = 0;

            for (Inventory inv : inventories) {
                for (Product p : products) {
                    if (p.getProductId() == inv.getProductId()) {
                        totalValue += (inv.getStockSacks() * p.getPriceSack())
                                + (inv.getPackaged25kg() * p.getPrice25kg())
                                + (inv.getPackaged20kg() * p.getPrice20kg())
                                + (inv.getPackaged10kg() * p.getPrice10kg())
                                + (inv.getPackaged5kg() * p.getPrice5kg())
                                + (inv.getStockKg() * p.getPriceKg());
                        break;
                    }
                }
                if ("Low Stock".equalsIgnoreCase(inv.getStockStatus()) || "Out of Stock".equalsIgnoreCase(inv.getStockStatus())) {
                    lowStockCount++;
                }
            }
            lblInventoryValue.setText(UIUtils.formatCurrency(totalValue));
            lblLowStock.setText(String.valueOf(lowStockCount));

            // 2. 7-Day Sales & Bar Chart Data
            LocalDate today = LocalDate.now();
            List<CustomCharts.BarItem> barItems = new ArrayList<>();
            double total7Day = 0;

            try (Connection conn = DatabaseManager.getConnection()) {
                String q7 = "SELECT COALESCE(SUM(total_amount), 0) FROM sales_transaction WHERE transaction_status = 'Completed' AND DATE(transaction_date) = ?";
                try (PreparedStatement ps = conn.prepareStatement(q7)) {
                    for (int i = 6; i >= 0; i--) {
                        LocalDate d = today.minusDays(i);
                        ps.setString(1, d.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            double daySum = rs.next() ? rs.getDouble(1) : 0;
                            total7Day += daySum;
                            String dayLbl = (i == 0) ? "Today" : d.getMonthValue() + "/" + d.getDayOfMonth();
                            barItems.add(new CustomCharts.BarItem(dayLbl, daySum));
                        }
                    }
                }
            }

            lbl7DaySales.setText(UIUtils.formatCurrency(total7Day));
            barChart.setData(barItems);

            // 3. Variety Sales Donut Chart
            List<CustomCharts.SliceItem> slices = new ArrayList<>();
            String donutSql = """
                SELECT p.product_name, COALESCE(SUM(ti.subtotal), 0) AS total_sales
                FROM product p
                JOIN transaction_item ti ON p.product_id = ti.product_id
                GROUP BY p.product_id, p.product_name
                HAVING total_sales > 0
                ORDER BY total_sales DESC
                LIMIT 8
            """;
            try (Connection conn = DatabaseManager.getConnection();
                 Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(donutSql)) {
                int cIdx = 0;
                while (rs.next()) {
                    String name = rs.getString("product_name");
                    double val = rs.getDouble("total_sales");
                    Color c = CustomCharts.VarietyDonutChart.PALETTE[cIdx % CustomCharts.VarietyDonutChart.PALETTE.length];
                    slices.add(new CustomCharts.SliceItem(name, val, c));
                    cIdx++;
                }
            }
            donutChart.setData(slices);

            // 4. Aging Stock (>60 Days)
            String agingSql = "SELECT COUNT(*) FROM inventory_batch WHERE status = 'Active' AND received_date <= " +
                    (DatabaseManager.isMySql() ? "DATE_SUB(NOW(), INTERVAL 60 DAY)" : "date('now', '-60 days')");
            try (Connection conn = DatabaseManager.getConnection();
                 Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(agingSql)) {
                if (rs.next()) {
                    lblAgingStock.setText(String.valueOf(rs.getInt(1)));
                }
            }

            // 5. Recent Transactions
            List<SalesTransaction> recent = salesDAO.getTransactions(10, null);
            recentTableModel.setRowCount(0);
            for (SalesTransaction tx : recent) {
                recentTableModel.addRow(new Object[]{
                        String.format("REC-%06d", tx.getTransactionId()),
                        UIUtils.formatDate(tx.getTransactionDate()),
                        tx.getCashierName(),
                        tx.getPaymentMethod(),
                        UIUtils.formatCurrency(tx.getTotalAmount())
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
