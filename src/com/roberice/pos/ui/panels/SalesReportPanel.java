package com.roberice.pos.ui.panels;

import com.roberice.pos.service.ReportService;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.time.LocalDate;

/**
 * Multi-Period Sales Reports & Financial Audit Statements (Section 2.5 in User Manual).
 */
public class SalesReportPanel extends JPanel {
    private final ReportService reportService = new ReportService();
    private ReportService.ReportSummary currentReport = null;

    // Period Controls
    private final JComboBox<String> cmbPeriod = new JComboBox<>(new String[]{"All Time", "Daily", "Weekly", "Monthly", "Quarterly", "Yearly"});
    private final JTextField txtDateInput = new JTextField(10);
    private final JComboBox<String> cmbQuarter = new JComboBox<>(new String[]{"Q1 (Jan-Mar)", "Q2 (Apr-Jun)", "Q3 (Jul-Sep)", "Q4 (Oct-Dec)"});
    private final JComboBox<Integer> cmbYear = new JComboBox<>(new Integer[]{2026, 2025, 2024});

    // KPI Labels
    private final JLabel lblRevenue = new JLabel("₱ 0.00");
    private final JLabel lblOrders = new JLabel("0");
    private final JLabel lblSacks = new JLabel("0");
    private final JLabel lblKg = new JLabel("0.00 kg");
    private final JLabel lblAov = new JLabel("₱ 0.00");

    // Report Table
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Rice Variety", "Origin", "50kg Sacks", "25kg Bags", "20kg Bags", "10kg Bags", "5kg Bags", "Loose (Kg)", "Total Volume (Kg)", "Gross Revenue"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblReport = new JTable(tableModel);

    private final JLabel lblStatementTitle = new JLabel("OFFICIAL SALES SUMMARY STATEMENT");
    private final JLabel lblStatementPeriod = new JLabel("Period: All Recorded History");

    public SalesReportPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(UIUtils.COLOR_BG);

        // Filter / Controls Bar Card
        JPanel filterCard = UIUtils.createCard();
        filterCard.setLayout(new BorderLayout());
        filterCard.setMaximumSize(new Dimension(2000, 70));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setBackground(Color.WHITE);

        leftFilters.add(new JLabel("Report Period:"));
        leftFilters.add(cmbPeriod);

        JLabel lblDateTag = new JLabel("Date / Month (YYYY-MM-DD):");
        txtDateInput.setText(LocalDate.now().toString());
        txtDateInput.setPreferredSize(new Dimension(100, 30));
        leftFilters.add(lblDateTag);
        leftFilters.add(txtDateInput);

        leftFilters.add(cmbQuarter);
        leftFilters.add(cmbYear);
        cmbQuarter.setVisible(false);
        cmbYear.setVisible(false);

        JButton btnGenerate = UIUtils.createPrimaryButton("Generate Report");
        btnGenerate.addActionListener(e -> generateReport());
        leftFilters.add(btnGenerate);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setBackground(Color.WHITE);

        JButton btnExport = UIUtils.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> exportCsv());

        JButton btnPrint = UIUtils.createSecondaryButton("Print Statement");
        btnPrint.addActionListener(e -> printStatement());

        rightActions.add(btnExport);
        rightActions.add(btnPrint);

        filterCard.add(leftFilters, BorderLayout.WEST);
        filterCard.add(rightActions, BorderLayout.EAST);

        cmbPeriod.addActionListener(e -> {
            String p = (String) cmbPeriod.getSelectedItem();
            boolean isQ = "Quarterly".equals(p);
            boolean isY = "Yearly".equals(p);
            boolean isDaily = "Daily".equals(p);
            boolean isMonthly = "Monthly".equals(p);

            cmbQuarter.setVisible(isQ);
            cmbYear.setVisible(isQ || isY);
            txtDateInput.setVisible(isDaily || isMonthly);
            lblDateTag.setVisible(isDaily || isMonthly);
            if (isMonthly) {
                LocalDate now = LocalDate.now();
                txtDateInput.setText(String.format("%04d-%02d", now.getYear(), now.getMonthValue()));
            } else if (isDaily) {
                txtDateInput.setText(LocalDate.now().toString());
            }
            filterCard.revalidate();
            filterCard.repaint();
        });

        // KPI Summary Cards
        JPanel kpiRow = new JPanel(new GridLayout(1, 5, 12, 0));
        kpiRow.setBackground(UIUtils.COLOR_BG);
        kpiRow.setMaximumSize(new Dimension(2000, 85));

        kpiRow.add(createKpiCard("Gross Store Revenue", lblRevenue, UIUtils.COLOR_PRIMARY));
        kpiRow.add(createKpiCard("Completed Orders", lblOrders, UIUtils.COLOR_INFO));
        kpiRow.add(createKpiCard("Whole Sacks Sold", lblSacks, UIUtils.COLOR_SUCCESS));
        kpiRow.add(createKpiCard("Total Volume (Kg)", lblKg, UIUtils.COLOR_WARNING));
        kpiRow.add(createKpiCard("Avg Basket Size", lblAov, UIUtils.COLOR_TEXT_PRIMARY));

        // Report Table Card
        JPanel tableCard = UIUtils.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);

        lblStatementTitle.setFont(UIUtils.FONT_TITLE);
        lblStatementTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        lblStatementPeriod.setFont(UIUtils.FONT_SMALL);
        lblStatementPeriod.setForeground(UIUtils.COLOR_TEXT_MUTED);

        tableHeader.add(lblStatementTitle, BorderLayout.WEST);
        tableHeader.add(lblStatementPeriod, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        UIUtils.styleTable(tblReport);
        JScrollPane scroll = new JScrollPane(tblReport);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        tableCard.add(scroll, BorderLayout.CENTER);

        main.add(filterCard);
        main.add(Box.createVerticalStrut(14));
        main.add(kpiRow);
        main.add(Box.createVerticalStrut(14));
        main.add(tableCard);

        add(main, BorderLayout.CENTER);
        generateReport();
    }

    private JPanel createKpiCard(String title, JLabel val, Color color) {
        JPanel c = UIUtils.createCard();
        c.setLayout(new BorderLayout(0, 4));
        JLabel t = new JLabel(title);
        t.setFont(UIUtils.FONT_SMALL);
        t.setForeground(UIUtils.COLOR_TEXT_MUTED);
        val.setFont(new Font("Segoe UI", Font.BOLD, 17));
        val.setForeground(color);
        c.add(t, BorderLayout.NORTH);
        c.add(val, BorderLayout.CENTER);
        return c;
    }

    public void generateReport() {
        String p = (String) cmbPeriod.getSelectedItem();
        String dateStr = txtDateInput.getText().trim();
        int quarter = cmbQuarter.getSelectedIndex() + 1;
        int year = (int) cmbYear.getSelectedItem();

        try {
            currentReport = reportService.generateReport(p, dateStr, quarter, year);

            lblStatementTitle.setText(currentReport.periodTitle);
            lblStatementPeriod.setText(currentReport.periodLabel);

            lblRevenue.setText(UIUtils.formatCurrency(currentReport.totalRevenue));
            lblOrders.setText(String.valueOf(currentReport.transactionCount));
            lblSacks.setText(String.valueOf(currentReport.totalSacksSold));
            lblKg.setText(String.format("%,.1f kg", currentReport.totalKgSold));
            lblAov.setText(UIUtils.formatCurrency(currentReport.averageOrderValue));

            tableModel.setRowCount(0);
            for (ReportService.VarietySalesRow r : currentReport.varietyRows) {
                tableModel.addRow(new Object[]{
                        r.productName,
                        r.origin,
                        r.sacksSold,
                        r.p25Sold,
                        r.p20Sold,
                        r.p10Sold,
                        r.p5Sold,
                        String.format("%.2f", r.looseKgSold),
                        String.format("%.2f", r.totalVolumeKg),
                        UIUtils.formatCurrency(r.totalRevenue)
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage(), "Report Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        if (currentReport == null) return;
        try {
            File f = reportService.exportToCsv(currentReport);
            JOptionPane.showMessageDialog(this,
                    "Sales report exported successfully!\nFile: " + f.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printStatement() {
        try {
            tblReport.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat("ROBERICE - " + lblStatementTitle.getText()),
                    new java.text.MessageFormat("Page {0} • " + lblStatementPeriod.getText()));
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Print Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
