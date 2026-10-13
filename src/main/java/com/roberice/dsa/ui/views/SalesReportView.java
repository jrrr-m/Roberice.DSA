package com.roberice.dsa.ui.views;

import com.roberice.dsa.service.SalesReportService;
import com.roberice.dsa.ui.chart.ChartPanel;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SalesReportView extends JPanel {

    private String selectedPeriod = "This Month";
    private SalesReportService.ReportSummary currentSummary;

    private final JLabel revVal = new JLabel("₱0.00");
    private final JLabel txVal = new JLabel("0");
    private final JLabel aovVal = new JLabel("₱0.00");
    private final JLabel volVal = new JLabel("0.0 kg");

    private final ChartPanel trendChart = new ChartPanel(ChartPanel.ChartType.BAR, "Daily Revenue Trend (PHP)");
    private final ChartPanel varietyChart = new ChartPanel(ChartPanel.ChartType.DONUT, "Sales by Rice Variety");

    private DefaultTableModel productTableModel;

    public SalesReportView() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_DARK);

        initComponents();
        loadReport();
    }

    private void initComponents() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIUtils.BG_DARK);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Header Bar: Period Selector & CSV Export
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel periodButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        periodButtons.setOpaque(false);

        String[] periods = {"Today", "This Week", "This Month", "This Year"};
        List<JButton> pBtns = new ArrayList<>();
        for (String p : periods) {
            JButton btn = new JButton(p);
            btn.setFont(UIUtils.FONT_BOLD);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                selectedPeriod = p;
                for (int i = 0; i < periods.length; i++) {
                    if (periods[i].equals(p)) {
                        pBtns.get(i).setBackground(UIUtils.PRIMARY);
                        pBtns.get(i).setForeground(Color.WHITE);
                    } else {
                        pBtns.get(i).setBackground(UIUtils.BG_INPUT);
                        pBtns.get(i).setForeground(UIUtils.TEXT_MAIN);
                    }
                }
                loadReport();
            });
            if (p.equals(selectedPeriod)) {
                btn.setBackground(UIUtils.PRIMARY);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(UIUtils.BG_INPUT);
                btn.setForeground(UIUtils.TEXT_MAIN);
            }
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIUtils.BORDER_COLOR, 1, true),
                    new EmptyBorder(7, 14, 7, 14)
            ));
            pBtns.add(btn);
            periodButtons.add(btn);
        }

        JButton exportBtn = UIUtils.createSuccessButton("📥 Export CSV Report", e -> exportCsv());

        topBar.add(periodButtons, BorderLayout.WEST);
        topBar.add(exportBtn, BorderLayout.EAST);
        content.add(topBar);
        content.add(Box.createVerticalStrut(18));

        // KPI Summary Cards (4 cols)
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        kpiGrid.add(createKpiCard("TOTAL REVENUE", revVal, "💰", UIUtils.PRIMARY));
        kpiGrid.add(createKpiCard("TRANSACTIONS", txVal, "🧾", UIUtils.ACCENT_INFO));
        kpiGrid.add(createKpiCard("AVG ORDER VALUE", aovVal, "📈", UIUtils.ACCENT_SUCCESS));
        kpiGrid.add(createKpiCard("VOLUME SOLD", volVal, "🌾", UIUtils.BRAND_GOLD));

        content.add(kpiGrid);
        content.add(Box.createVerticalStrut(20));

        // Charts Row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        chartsRow.setPreferredSize(new Dimension(0, 260));

        chartsRow.add(trendChart);
        chartsRow.add(varietyChart);

        content.add(chartsRow);
        content.add(Box.createVerticalStrut(20));

        // Product Breakdown Table Card
        JPanel tableCard = UIUtils.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel tblTitle = new JLabel("Product Variety Performance");
        tblTitle.setFont(UIUtils.FONT_SUBHEADER);
        tblTitle.setForeground(UIUtils.TEXT_PRIMARY);
        tableCard.add(tblTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Product Name", "Category", "Rice Type", "Sacks Sold", "Total Volume (kg)", "Gross Revenue"};
        productTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = UIUtils.createStyledTable(productTableModel);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, 240));
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard);

        JScrollPane mainScroll = new JScrollPane(content);
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

    public void loadReport() {
        SwingUtilities.invokeLater(() -> {
            try {
                currentSummary = SalesReportService.generateReport(selectedPeriod, null, null, null);

                revVal.setText(UIUtils.formatCurrency(currentSummary.totalRevenue));
                txVal.setText(String.valueOf(currentSummary.totalTransactions));
                aovVal.setText(UIUtils.formatCurrency(currentSummary.averageOrderValue));
                volVal.setText(String.format("%,.1f kg", currentSummary.totalKgSold));

                trendChart.setData(currentSummary.dailyTrend, "sale_date", "daily_revenue");
                varietyChart.setData(currentSummary.categoryBreakdown, "variety", "variety_revenue");

                productTableModel.setRowCount(0);
                for (Map<String, Object> r : currentSummary.productSales) {
                    productTableModel.addRow(new Object[]{
                            r.get("product_id"),
                            r.get("product_name"),
                            r.get("category"),
                            r.get("rice_type"),
                            r.get("sacks_sold"),
                            String.format("%,.2f kg", ((Number) r.get("total_kg_sold")).doubleValue()),
                            UIUtils.formatCurrency(((Number) r.get("product_revenue")).doubleValue())
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void exportCsv() {
        if (currentSummary == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Roberice_Sales_Report_" + selectedPeriod.replace(" ", "_") + ".csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                File target = chooser.getSelectedFile();
                SalesReportService.exportReportToCsv(currentSummary, target);
                UIUtils.showSuccess(this, "Sales report successfully exported to:\n" + target.getAbsolutePath());
            } catch (Exception ex) {
                UIUtils.showError(this, "Failed to export CSV: " + ex.getMessage());
            }
        }
    }
}
