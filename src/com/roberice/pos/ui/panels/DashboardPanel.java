package com.roberice.pos.ui.panels;

import com.roberice.pos.db.DatabaseManager;
import com.roberice.pos.db.InventoryDAO;
import com.roberice.pos.db.ProductDAO;
import com.roberice.pos.db.SalesDAO;
import com.roberice.pos.model.Inventory;
import com.roberice.pos.model.Product;
import com.roberice.pos.model.SalesTransaction;
import com.roberice.pos.service.AuthSession;
import com.roberice.pos.ui.common.CustomCharts;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Executive Store Dashboard & Analytics Panel.
 * Matches the warm ivory/green palette with KPI badge cards, sales trend,
 * product category donut, top selling products list, and low stock alerts.
 */
public class DashboardPanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final SalesDAO salesDAO = new SalesDAO();

    // Metric Labels
    private final JLabel lblVarieties = new JLabel("0");
    private final JLabel lblInventoryValue = new JLabel("₱ 0.00");
    private final JLabel lbl7DaySalesCount = new JLabel("0");
    private final JLabel lbl7DaySalesTotal = new JLabel("₱ 0.00 total");
    private final JLabel lblLowStock = new JLabel("0");
    private final JLabel lblAgingStock = new JLabel("0");

    // Charts
    private final CustomCharts.SalesTrendBarChart barChart = new CustomCharts.SalesTrendBarChart();
    private final CustomCharts.VarietyDonutChart donutChart = new CustomCharts.VarietyDonutChart();

    // Dynamic Lists
    private final JPanel topSellingContainer = new JPanel();
    private final JPanel lowStockContainer = new JPanel();

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIUtils.COLOR_BG);
        content.setBorder(new EmptyBorder(16, 22, 24, 22));

        // 1. KPI Cards Grid (4 in top row, 1 in row 2)
        JPanel kpiRow1 = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow1.setOpaque(false);
        kpiRow1.setMaximumSize(new Dimension(3000, 100));
        kpiRow1.setPreferredSize(new Dimension(1000, 100));

        // Total Varieties Card
        kpiRow1.add(createKpiCard(
                "TOTAL VARIETIES",
                lblVarieties,
                "Active Rice Items",
                new Color(237, 243, 236), // Soft mint bg
                "box"
        ));

        // Inventory Value Card
        kpiRow1.add(createKpiCard(
                "INVENTORY VALUE",
                lblInventoryValue,
                "Estimated Wholesale",
                new Color(209, 250, 229), // Emerald tint
                "peso"
        ));

        // Sales (7 Days) Card
        kpiRow1.add(createKpiCardDual(
                "SALES (7 DAYS)",
                lbl7DaySalesCount,
                lbl7DaySalesTotal,
                new Color(243, 232, 255), // Purple tint
                "cart"
        ));

        // Low Stock Items Card
        kpiRow1.add(createKpiCard(
                "LOW STOCK ITEMS",
                lblLowStock,
                "Needs Restocking",
                new Color(254, 243, 199), // Amber tint
                "warning"
        ));

        // Row 2 of KPIs (Old Stock Alert)
        JPanel kpiRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        kpiRow2.setOpaque(false);
        kpiRow2.setMaximumSize(new Dimension(3000, 100));
        kpiRow2.setPreferredSize(new Dimension(1000, 100));

        JPanel oldStockCard = createKpiCard(
                "OLD STOCK ALERT",
                lblAgingStock,
                "> 60 Days Old (FIFO Action)",
                new Color(254, 226, 226), // Rose/red tint
                "clock"
        );
        oldStockCard.setPreferredSize(new Dimension(280, 100));
        kpiRow2.add(oldStockCard);

        // 2. Charts Row (Sales Trend & Donut)
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setMaximumSize(new Dimension(3000, 320));
        chartsRow.setPreferredSize(new Dimension(1000, 290));

        // Left: Sales Trend
        JPanel barCard = UIUtils.createCard();
        barCard.setLayout(new BorderLayout(0, 10));
        JLabel barTitle = new JLabel("Sales Trend (Last 7 Days)");
        barTitle.setFont(UIUtils.FONT_TITLE);
        barTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        barCard.add(barTitle, BorderLayout.NORTH);
        barCard.add(barChart, BorderLayout.CENTER);

        // Right: Product Categories / Variety Sales
        JPanel donutCard = UIUtils.createCard();
        donutCard.setLayout(new BorderLayout(0, 10));
        JLabel donutTitle = new JLabel("Product Categories / Variety Sales");
        donutTitle.setFont(UIUtils.FONT_TITLE);
        donutTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        donutCard.add(donutTitle, BorderLayout.NORTH);
        donutCard.add(donutChart, BorderLayout.CENTER);

        chartsRow.add(barCard);
        chartsRow.add(donutCard);

        // 3. Bottom Row: Top Selling Products (Left) & Low Stock Alerts (Right)
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(3000, 300));
        bottomRow.setPreferredSize(new Dimension(1000, 250));

        // Top Selling Card
        JPanel topSellingCard = UIUtils.createCard();
        topSellingCard.setLayout(new BorderLayout(0, 10));

        JPanel tsHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tsHeader.setOpaque(false);
        JLabel tsIcon = new JLabel("↗");
        tsIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tsIcon.setForeground(new Color(22, 163, 74));
        JLabel tsTitle = new JLabel("Top Selling Products");
        tsTitle.setFont(UIUtils.FONT_TITLE);
        tsTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        tsHeader.add(tsIcon);
        tsHeader.add(tsTitle);
        topSellingCard.add(tsHeader, BorderLayout.NORTH);

        topSellingContainer.setLayout(new BoxLayout(topSellingContainer, BoxLayout.Y_AXIS));
        topSellingContainer.setOpaque(false);
        topSellingCard.add(topSellingContainer, BorderLayout.CENTER);

        // Low Stock Alerts Card
        JPanel lowStockCard = UIUtils.createCard();
        lowStockCard.setLayout(new BorderLayout(0, 10));

        JPanel lsHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        lsHeader.setOpaque(false);
        JLabel lsIcon = new JLabel("⚠️");
        lsIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        JLabel lsTitle = new JLabel("Low Stock Alerts");
        lsTitle.setFont(UIUtils.FONT_TITLE);
        lsTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        lsHeader.add(lsIcon);
        lsHeader.add(lsTitle);
        lowStockCard.add(lsHeader, BorderLayout.NORTH);

        lowStockContainer.setLayout(new BoxLayout(lowStockContainer, BoxLayout.Y_AXIS));
        lowStockContainer.setOpaque(false);
        lowStockCard.add(lowStockContainer, BorderLayout.CENTER);

        bottomRow.add(topSellingCard);
        bottomRow.add(lowStockCard);

        // Add rows to content
        content.add(kpiRow1);
        content.add(Box.createVerticalStrut(14));
        content.add(kpiRow2);
        content.add(Box.createVerticalStrut(16));
        content.add(chartsRow);
        content.add(Box.createVerticalStrut(16));
        content.add(bottomRow);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.setBackground(UIUtils.COLOR_BG);
        mainScroll.getViewport().setBackground(UIUtils.COLOR_BG);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createKpiCard(String tag, JLabel valLbl, String subtext, Color badgeBg, String iconSymbol) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(8, 0));

        // Left text block
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTag = new JLabel(tag);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(UIUtils.COLOR_TEXT_MUTED);

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel lblSub = new JLabel(subtext);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        left.add(lblTag);
        left.add(Box.createVerticalStrut(3));
        left.add(valLbl);
        left.add(Box.createVerticalStrut(2));
        left.add(lblSub);

        // Right icon badge
        JPanel badge = createIconBadge(iconSymbol, badgeBg);

        card.add(left, BorderLayout.CENTER);
        card.add(badge, BorderLayout.EAST);
        return card;
    }

    private JPanel createKpiCardDual(String tag, JLabel valLbl, JLabel subValLbl, Color badgeBg, String iconSymbol) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(8, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTag = new JLabel(tag);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(UIUtils.COLOR_TEXT_MUTED);

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        subValLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subValLbl.setForeground(UIUtils.COLOR_TEXT_MUTED);

        left.add(lblTag);
        left.add(Box.createVerticalStrut(3));
        left.add(valLbl);
        left.add(Box.createVerticalStrut(2));
        left.add(subValLbl);

        JPanel badge = createIconBadge(iconSymbol, badgeBg);

        card.add(left, BorderLayout.CENTER);
        card.add(badge, BorderLayout.EAST);
        return card;
    }

    private JPanel createIconBadge(String symbol, Color bg) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Draw vector icon inside badge
                drawBadgeIcon(g2, symbol, getWidth(), getHeight());

                g2.dispose();
                super.paintComponent(g);
            }

            private void drawBadgeIcon(Graphics2D g2, String sym, int w, int h) {
                g2.setStroke(new BasicStroke(1.5f));
                int cx = w / 2;
                int cy = h / 2;

                switch (sym) {
                    case "box":
                        g2.setColor(new Color(27, 77, 50));
                        g2.drawRect(cx - 6, cy - 6, 12, 12);
                        g2.drawLine(cx - 6, cy - 2, cx + 6, cy - 2);
                        g2.drawLine(cx, cy - 2, cx, cy + 6);
                        break;
                    case "peso":
                        g2.setColor(new Color(5, 150, 105));
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        String p = "₱";
                        int pw = g2.getFontMetrics().stringWidth(p);
                        g2.drawString(p, cx - pw / 2, cy + 5);
                        break;
                    case "cart":
                        g2.setColor(new Color(147, 51, 234));
                        g2.drawLine(cx - 7, cy - 5, cx - 4, cy - 5);
                        g2.drawLine(cx - 4, cy - 5, cx - 2, cy + 3);
                        g2.drawLine(cx - 2, cy + 3, cx + 5, cy + 3);
                        g2.drawLine(cx + 5, cy + 3, cx + 6, cy - 3);
                        g2.drawLine(cx - 4, cy - 3, cx + 6, cy - 3);
                        g2.fillOval(cx - 2, cy + 5, 2, 2);
                        g2.fillOval(cx + 4, cy + 5, 2, 2);
                        break;
                    case "warning":
                        g2.setColor(new Color(217, 119, 6));
                        // Draw triangle warning
                        int[] xPts = {cx, cx - 7, cx + 7};
                        int[] yPts = {cy - 6, cy + 6, cy + 6};
                        g2.drawPolygon(xPts, yPts, 3);
                        g2.drawLine(cx, cy - 2, cx, cy + 2);
                        g2.drawLine(cx, cy + 4, cx, cy + 4);
                        break;
                    case "clock":
                        g2.setColor(new Color(220, 38, 38));
                        g2.drawOval(cx - 6, cy - 6, 12, 12);
                        g2.drawLine(cx, cy - 4, cx, cy);
                        g2.drawLine(cx, cy, cx + 3, cy);
                        break;
                }
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(34, 34));
        badge.setMaximumSize(new Dimension(34, 34));

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        wrap.setOpaque(false);
        wrap.add(badge);
        return wrap;
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
            int count7Day = 0;
            DateTimeFormatter barDateFormatter = DateTimeFormatter.ofPattern("MMM dd");

            try (Connection conn = DatabaseManager.getConnection()) {
                String q7 = "SELECT COUNT(*), COALESCE(SUM(total_amount), 0) FROM sales_transaction WHERE transaction_status = 'Completed' AND DATE(transaction_date) = ?";
                try (PreparedStatement ps = conn.prepareStatement(q7)) {
                    for (int i = 6; i >= 0; i--) {
                        LocalDate d = today.minusDays(i);
                        ps.setString(1, d.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            int dayCount = 0;
                            double daySum = 0;
                            if (rs.next()) {
                                dayCount = rs.getInt(1);
                                daySum = rs.getDouble(2);
                            }
                            count7Day += dayCount;
                            total7Day += daySum;
                            String dayLbl = d.format(barDateFormatter);
                            barItems.add(new CustomCharts.BarItem(dayLbl, daySum));
                        }
                    }
                }
            }

            lbl7DaySalesCount.setText(String.valueOf(count7Day));
            lbl7DaySalesTotal.setText(UIUtils.formatCurrency(total7Day) + " total");
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

            // 5. Populate Top Selling Products list
            populateTopSellingProducts();

            // 6. Populate Low Stock Alerts list
            populateLowStockAlerts(inventories, products);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void populateTopSellingProducts() {
        topSellingContainer.removeAll();

        String sql = """
            SELECT p.product_name, p.category, 
                   COALESCE(SUM(ti.quantity), 0) as total_qty,
                   COALESCE(SUM(ti.subtotal), 0) as total_amount
            FROM product p
            JOIN transaction_item ti ON p.product_id = ti.product_id
            GROUP BY p.product_id, p.product_name, p.category
            ORDER BY total_amount DESC
            LIMIT 3
        """;

        boolean hasItems = false;
        try (Connection conn = DatabaseManager.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {
                hasItems = true;
                String name = rs.getString("product_name");
                String cat = rs.getString("category");
                double qty = rs.getDouble("total_qty");
                double amount = rs.getDouble("total_amount");

                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBackground(new Color(248, 246, 240));
                row.setBorder(new EmptyBorder(8, 12, 8, 12));
                row.setMaximumSize(new Dimension(2000, 52));

                JPanel left = new JPanel(new GridLayout(2, 1, 2, 2));
                left.setOpaque(false);
                JLabel lblName = new JLabel(name);
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblName.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

                JLabel lblMeta = new JLabel("Category: " + (cat != null ? cat : "General") + " | Sold: " + (int) qty + " units");
                lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                lblMeta.setForeground(UIUtils.COLOR_TEXT_MUTED);

                left.add(lblName);
                left.add(lblMeta);

                JLabel lblAmount = new JLabel(UIUtils.formatCurrency(amount));
                lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblAmount.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

                row.add(left, BorderLayout.CENTER);
                row.add(lblAmount, BorderLayout.EAST);

                topSellingContainer.add(row);
                topSellingContainer.add(Box.createVerticalStrut(6));
            }
        } catch (Exception ignored) {
        }

        if (!hasItems) {
            JLabel empty = new JLabel("No sales recorded yet.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            empty.setForeground(UIUtils.COLOR_TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            topSellingContainer.add(Box.createVerticalStrut(20));
            topSellingContainer.add(empty);
        }

        topSellingContainer.revalidate();
        topSellingContainer.repaint();
    }

    private void populateLowStockAlerts(List<Inventory> inventories, List<Product> products) {
        lowStockContainer.removeAll();
        int count = 0;

        for (Inventory inv : inventories) {
            if ("Low Stock".equalsIgnoreCase(inv.getStockStatus()) || "Out of Stock".equalsIgnoreCase(inv.getStockStatus())) {
                count++;
                if (count > 3) break;

                String pName = "Product #" + inv.getProductId();
                for (Product p : products) {
                    if (p.getProductId() == inv.getProductId()) {
                        pName = p.getProductName();
                        break;
                    }
                }

                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBackground(new Color(254, 242, 242));
                row.setBorder(new EmptyBorder(8, 12, 8, 12));
                row.setMaximumSize(new Dimension(2000, 50));

                JLabel lblName = new JLabel(pName);
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblName.setForeground(new Color(153, 27, 27));

                JLabel lblRemain = new JLabel(inv.getStockSacks() + " sacks remaining");
                lblRemain.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblRemain.setForeground(new Color(185, 28, 28));

                row.add(lblName, BorderLayout.WEST);
                row.add(lblRemain, BorderLayout.EAST);

                lowStockContainer.add(row);
                lowStockContainer.add(Box.createVerticalStrut(6));
            }
        }

        if (count == 0) {
            JLabel empty = new JLabel("All items are well stocked!");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(new Color(100, 115, 105));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel centerWrap = new JPanel(new GridBagLayout());
            centerWrap.setOpaque(false);
            centerWrap.add(empty);

            lowStockContainer.add(centerWrap);
        }

        lowStockContainer.revalidate();
        lowStockContainer.repaint();
    }
}
