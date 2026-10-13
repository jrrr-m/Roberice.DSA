package com.roberice.dsa.ui.views;

import com.roberice.dsa.db.Database;
import com.roberice.dsa.model.SalesTransaction;
import com.roberice.dsa.service.SalesReportService;
import com.roberice.dsa.service.TransactionService;
import com.roberice.dsa.ui.chart.ChartPanel;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DashboardView extends JPanel {

    private final Consumer<String> onNavigate;
    private final JLabel todaySalesVal = new JLabel("₱0.00");
    private final JLabel todayTxVal = new JLabel("0");
    private final JLabel totalSacksVal = new JLabel("0");
    private final JLabel lowStockVal = new JLabel("0");

    private final ChartPanel barChart = new ChartPanel(ChartPanel.ChartType.BAR, "7-Day Sales Trend (PHP)");
    private final ChartPanel donutChart = new ChartPanel(ChartPanel.ChartType.DONUT, "Rice Variety Sales Share");

    private DefaultTableModel txTableModel;

    public DashboardView(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_DARK);

        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIUtils.BG_DARK);
        contentPanel.setBorder(new EmptyBorder(20, 24, 24, 24));

        // 1. KPI Cards Grid (4 columns)
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        kpiGrid.add(createKpiCard("TODAY'S REVENUE", todaySalesVal, "💰", UIUtils.ACCENT_PRIMARY));
        kpiGrid.add(createKpiCard("TRANSACTIONS", todayTxVal, "🧾", UIUtils.ACCENT_INFO));
        kpiGrid.add(createKpiCard("WAREHOUSE SACKS", totalSacksVal, "🌾", UIUtils.ACCENT_SUCCESS));
        kpiGrid.add(createKpiCard("LOW STOCK ALERTS", lowStockVal, "⚠️", UIUtils.ACCENT_WARNING));

        contentPanel.add(kpiGrid);
        contentPanel.add(Box.createVerticalStrut(20));

        // 2. Charts Row (2 columns)
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        chartsRow.setPreferredSize(new Dimension(0, 260));

        chartsRow.add(barChart);
        chartsRow.add(donutChart);

        contentPanel.add(chartsRow);
        contentPanel.add(Box.createVerticalStrut(20));

        // 3. Quick Actions & Recent Transactions
        JPanel bottomRow = new JPanel(new BorderLayout(16, 0));
        bottomRow.setOpaque(false);

        // Recent Transactions Panel
        JPanel recentPanel = UIUtils.createCard();
        recentPanel.setLayout(new BorderLayout());

        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);
        JLabel recentTitle = new JLabel("Recent Store Transactions");
        recentTitle.setFont(UIUtils.FONT_SUBHEADER);
        recentTitle.setForeground(UIUtils.TEXT_PRIMARY);

        JButton viewAllBtn = UIUtils.createSecondaryButton("View All →", e -> {
            if (onNavigate != null) onNavigate.accept("transactions");
        });

        recentHeader.add(recentTitle, BorderLayout.WEST);
        recentHeader.add(viewAllBtn, BorderLayout.EAST);
        recentPanel.add(recentHeader, BorderLayout.NORTH);

        String[] cols = {"Tx ID", "Date & Time", "Cashier", "Method", "Amount", "Status"};
        txTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable txTable = UIUtils.createStyledTable(txTableModel);
        JScrollPane scroll = new JScrollPane(txTable);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        scroll.setPreferredSize(new Dimension(0, 200));

        recentPanel.add(scroll, BorderLayout.CENTER);
        bottomRow.add(recentPanel, BorderLayout.CENTER);

        // Quick Shortcuts Panel
        JPanel shortcutsPanel = UIUtils.createCard();
        shortcutsPanel.setLayout(new BoxLayout(shortcutsPanel, BoxLayout.Y_AXIS));
        shortcutsPanel.setPreferredSize(new Dimension(240, 200));

        JLabel shortcutsTitle = new JLabel("Quick Actions");
        shortcutsTitle.setFont(UIUtils.FONT_SUBHEADER);
        shortcutsTitle.setForeground(UIUtils.TEXT_PRIMARY);
        shortcutsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton posBtn = UIUtils.createPrimaryButton("🛒  New Sale (POS)", e -> {
            if (onNavigate != null) onNavigate.accept("pos");
        });
        posBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        posBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton restockBtn = UIUtils.createSuccessButton("📦  Stock In / Sacks", e -> {
            if (onNavigate != null) onNavigate.accept("inventory");
        });
        restockBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        restockBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton reportBtn = UIUtils.createSecondaryButton("📊  Sales Analytics", e -> {
            if (onNavigate != null) onNavigate.accept("sales-report");
        });
        reportBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        reportBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        shortcutsPanel.add(shortcutsTitle);
        shortcutsPanel.add(Box.createVerticalStrut(14));
        shortcutsPanel.add(posBtn);
        shortcutsPanel.add(Box.createVerticalStrut(8));
        shortcutsPanel.add(restockBtn);
        shortcutsPanel.add(Box.createVerticalStrut(8));
        shortcutsPanel.add(reportBtn);

        bottomRow.add(shortcutsPanel, BorderLayout.EAST);
        contentPanel.add(bottomRow);

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String label, JLabel valueLabel, String icon, Color accentColor) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout());

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_SMALL);
        lbl.setForeground(UIUtils.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(UIUtils.TEXT_PRIMARY);

        left.add(lbl);
        left.add(Box.createVerticalStrut(4));
        left.add(valueLabel);

        JLabel icn = new JLabel(icon);
        icn.setFont(new Font("Segoe UI", Font.PLAIN, 26));

        card.add(left, BorderLayout.CENTER);
        card.add(icn, BorderLayout.EAST);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(14, 16, 14, 16)
        ));

        return card;
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Today's sales
                Map<String, Object> todayStats = Database.queryForMap(
                        "SELECT COUNT(*) as tx_count, COALESCE(SUM(total_amount), 0.0) as rev " +
                        "FROM sales_transaction WHERE DATE(transaction_date) = CURRENT_DATE AND transaction_status = 'Completed'"
                );
                if (todayStats != null) {
                    double rev = todayStats.get("rev") != null ? ((Number) todayStats.get("rev")).doubleValue() : 0.0;
                    int tx = todayStats.get("tx_count") != null ? ((Number) todayStats.get("tx_count")).intValue() : 0;
                    todaySalesVal.setText(UIUtils.formatCurrency(rev));
                    todayTxVal.setText(String.valueOf(tx));
                }

                // Total sacks & low stock
                Map<String, Object> sackStats = Database.queryForMap(
                        "SELECT SUM(stock_sacks) AS sacks FROM inventory"
                );
                if (sackStats != null && sackStats.get("sacks") != null) {
                    totalSacksVal.setText(String.valueOf(((Number) sackStats.get("sacks")).intValue()));
                }

                Map<String, Object> lowStock = Database.queryForMap(
                        "SELECT COUNT(*) AS c FROM product p JOIN inventory i ON p.product_id = i.product_id " +
                        "WHERE p.product_status = 'Active' AND " +
                        "((i.stock_sacks * COALESCE(p.kg_per_sack, 50)) + (i.packaged_25kg * 25) + (i.packaged_20kg * 20) + (i.packaged_10kg * 10) + (i.packaged_5kg * 5) + i.stock_kg) <= i.reorder_level_kg"
                );
                if (lowStock != null && lowStock.get("c") != null) {
                    lowStockVal.setText(String.valueOf(((Number) lowStock.get("c")).intValue()));
                }

                // 7-day trend
                List<Map<String, Object>> trend = Database.queryForList(
                        "SELECT DATE(transaction_date) AS sale_date, SUM(total_amount) AS revenue " +
                        "FROM sales_transaction WHERE transaction_date >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY) AND transaction_status = 'Completed' " +
                        "GROUP BY DATE(transaction_date) ORDER BY sale_date ASC"
                );
                barChart.setData(trend, "sale_date", "revenue");

                // Variety share
                List<Map<String, Object>> varieties = Database.queryForList(
                        "SELECT COALESCE(p.rice_type, p.category) AS variety, SUM(ti.subtotal) AS variety_sales " +
                        "FROM transaction_item ti " +
                        "JOIN sales_transaction t ON ti.transaction_id = t.transaction_id " +
                        "JOIN product p ON ti.product_id = p.product_id " +
                        "WHERE t.transaction_status = 'Completed' " +
                        "GROUP BY COALESCE(p.rice_type, p.category) " +
                        "ORDER BY variety_sales DESC"
                );
                donutChart.setData(varieties, "variety", "variety_sales");

                // Recent transactions
                List<SalesTransaction> recent = TransactionService.getTransactions(null, "Completed", 5, 0);
                txTableModel.setRowCount(0);
                for (SalesTransaction t : recent) {
                    txTableModel.addRow(new Object[]{
                            "#" + t.getTransactionId(),
                            t.getTransactionDate(),
                            t.getCashierName(),
                            t.getPaymentMethod(),
                            UIUtils.formatCurrency(t.getTotalAmount()),
                            t.getTransactionStatus()
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
