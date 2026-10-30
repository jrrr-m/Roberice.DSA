package com.roberice.pos.ui.panels;

import com.roberice.pos.db.DatabaseManager;
import com.roberice.pos.service.ReportService;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Modern Sales Report & Analytics Panel.
 * Matches the uploaded design with period pill buttons (Daily, Weekly, Monthly, Quarterly, Yearly, Custom Range, All Time),
 * secondary filter row (Report Type, Target Date picker / prev/today buttons, Rice Origin, Cashier, Payment, Apply, Reset),
 * KPI cards (Total Gross Sales, Transactions Count, Sacks & Volume Sold, Packaging Breakdown),
 * and Sales Volume & Revenue by Rice Variety table with progress % share indicators.
 */
public class SalesReportPanel extends JPanel {
    private final ReportService reportService = new ReportService();
    private ReportService.ReportSummary currentReport = null;

    // Period Selection Buttons
    private String selectedPeriod = "Daily Report";
    private final List<PeriodButton> periodButtons = new ArrayList<>();

    // Secondary Filters
    private final JComboBox<String> cmbReportType = new JComboBox<>(new String[]{"Daily Report", "Weekly Report", "Monthly Report", "Quarterly Report", "Yearly Report", "Custom Range", "All Time"});
    private final JTextField txtTargetDate = new JTextField();
    private final JComboBox<String> cmbOrigin = new JComboBox<>(new String[]{"All Origins", "Local", "Imported"});
    private final JComboBox<String> cmbCashier = new JComboBox<>(new String[]{"All Cashiers"});
    private final JComboBox<String> cmbPayment = new JComboBox<>(new String[]{"All Methods", "Cash", "GCash", "Maya"});

    // Header Period Banner Label
    private final JLabel lblPeriodBadge = new JLabel("DAILY REPORT");
    private final JLabel lblPeriodDetail = new JLabel("Period: Daily: Monday, October 12, 2026");

    // KPI Metric Labels
    private final JLabel lblGrossSales = new JLabel("₱0.00");
    private final JLabel lblCashNonCash = new JLabel("Cash: ₱0.00 | Non-Cash: ₱0.00");

    private final JLabel lblTxnCount = new JLabel("0 completed");
    private final JLabel lblAvgOrder = new JLabel("Avg: ₱0.00 / txn");

    private final JLabel lblSacksSold = new JLabel("0 sacks");
    private final JLabel lblVolTotal = new JLabel("0.00 kg total equivalent");

    private final JLabel lblPackBags = new JLabel("0 pre-packed bags");
    private final JLabel lblLooseSold = new JLabel("0.0 kg loose tinatakal");

    // Dynamic Variety Table Rows Container
    private final JPanel rowsContainer = new ScrollableRowsPanel();

    private static class ScrollableRowsPanel extends JPanel implements Scrollable {
        public ScrollableRowsPanel() {
            super();
        }
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // Design Colors
    private static final Color COLOR_APP_BG = new Color(246, 243, 233);
    private static final Color COLOR_CARD_BG = new Color(255, 253, 246);
    private static final Color COLOR_CARD_BORDER = new Color(234, 230, 218);
    private static final Color COLOR_TH_BG = new Color(242, 234, 213);
    private static final Color COLOR_PRIMARY_GREEN = new Color(27, 77, 50);

    private LocalDate selectedDate = LocalDate.now();

    public SalesReportPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(COLOR_APP_BG);
        setBorder(new EmptyBorder(10, 16, 16, 16));

        JPanel mainLayout = new JPanel();
        mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
        mainLayout.setOpaque(false);

        // 1. Report Filters & Period Selection Card
        JPanel filterCard = createFilterSelectionCard();
        mainLayout.add(filterCard);
        mainLayout.add(Box.createVerticalStrut(14));

        // 2. KPI Cards Row (4 cards)
        JPanel kpiRow = createKpiRow();
        mainLayout.add(kpiRow);
        mainLayout.add(Box.createVerticalStrut(14));

        // 3. Sales Volume & Revenue by Rice Variety Card
        JPanel tableCard = createVarietyTableCard();
        mainLayout.add(tableCard);

        add(mainLayout, BorderLayout.CENTER);

        loadCashierList();
        generateReport();
    }

    private JPanel createFilterSelectionCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(COLOR_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setMaximumSize(new Dimension(3000, 140));

        // Top Row: Title + Period Pills + Right Badge Banner
        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTitle.setOpaque(false);

        JLabel lblSec = new JLabel("Report Filters & Period Selection");
        lblSec.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSec.setForeground(new Color(20, 30, 25));
        leftTitle.add(lblSec);

        // Period pills
        String[] periods = {"Daily Report", "Weekly Report", "Monthly Report", "Quarterly Report", "Yearly Report", "Custom Range", "All Time"};
        String[] pIcons = {"📅", "🗓️", "📆", "⏱", "📊", "📋", "🕒"};

        for (int i = 0; i < periods.length; i++) {
            PeriodButton pb = new PeriodButton(pIcons[i] + " " + periods[i], periods[i]);
            periodButtons.add(pb);
            leftTitle.add(pb);
        }
        periodButtons.get(0).setSelected(true);

        // Right banner badge
        JPanel rightBanner = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightBanner.setOpaque(false);

        lblPeriodBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblPeriodBadge.setForeground(new Color(14, 116, 144));
        lblPeriodBadge.setBackground(new Color(224, 242, 254));
        lblPeriodBadge.setOpaque(true);
        lblPeriodBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        lblPeriodDetail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPeriodDetail.setForeground(new Color(70, 80, 75));

        rightBanner.add(lblPeriodBadge);
        rightBanner.add(lblPeriodDetail);

        topRow.add(leftTitle, BorderLayout.WEST);
        topRow.add(rightBanner, BorderLayout.EAST);

        // Bottom Filter Controls Row
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomRow.setOpaque(false);

        // Report Type
        JPanel pReportType = createLabeledControl("Report Type", cmbReportType);
        styleDropdown(cmbReportType);
        cmbReportType.setPreferredSize(new Dimension(110, 30));
        cmbReportType.addActionListener(e -> {
            String sel = (String) cmbReportType.getSelectedItem();
            selectPeriod(sel);
        });

        // Target Date with Prev / Today controls
        JPanel dateGroup = new JPanel();
        dateGroup.setLayout(new BoxLayout(dateGroup, BoxLayout.Y_AXIS));
        dateGroup.setOpaque(false);

        JLabel lblDateLabel = new JLabel("Target Date");
        lblDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDateLabel.setForeground(new Color(120, 130, 125));

        JPanel dateControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        dateControls.setOpaque(false);

        JButton btnPrev = createSmallFilterButton("Prev Day");
        btnPrev.addActionListener(e -> {
            selectedDate = selectedDate.minusDays(1);
            txtTargetDate.setText(selectedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            generateReport();
        });

        txtTargetDate.setText(selectedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        txtTargetDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtTargetDate.setPreferredSize(new Dimension(85, 30));
        txtTargetDate.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1),
                new EmptyBorder(2, 6, 2, 6)
        ));

        JButton btnToday = createSmallFilterButton("Today");
        btnToday.addActionListener(e -> {
            selectedDate = LocalDate.now();
            txtTargetDate.setText(selectedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            generateReport();
        });

        dateControls.add(btnPrev);
        dateControls.add(txtTargetDate);
        dateControls.add(btnToday);

        dateGroup.add(lblDateLabel);
        dateGroup.add(Box.createVerticalStrut(2));
        dateGroup.add(dateControls);

        // Rice Origin
        styleDropdown(cmbOrigin);
        cmbOrigin.setPreferredSize(new Dimension(100, 30));
        JPanel pOrigin = createLabeledControl("Rice Origin", cmbOrigin);

        // Cashier
        styleDropdown(cmbCashier);
        cmbCashier.setPreferredSize(new Dimension(110, 30));
        JPanel pCashier = createLabeledControl("Cashier", cmbCashier);

        // Payment
        styleDropdown(cmbPayment);
        cmbPayment.setPreferredSize(new Dimension(100, 30));
        JPanel pPayment = createLabeledControl("Payment", cmbPayment);

        // Apply & Reset buttons
        JButton btnApply = new JButton("Apply Filter") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_PRIMARY_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnApply.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnApply.setForeground(Color.WHITE);
        btnApply.setContentAreaFilled(false);
        btnApply.setBorderPainted(false);
        btnApply.setFocusPainted(false);
        btnApply.setPreferredSize(new Dimension(90, 30));
        btnApply.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApply.addActionListener(e -> generateReport());

        JButton btnReset = createSmallFilterButton("Reset");
        btnReset.setPreferredSize(new Dimension(65, 30));
        btnReset.addActionListener(e -> {
            cmbReportType.setSelectedIndex(0);
            selectedDate = LocalDate.now();
            txtTargetDate.setText(selectedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            cmbOrigin.setSelectedIndex(0);
            cmbCashier.setSelectedIndex(0);
            cmbPayment.setSelectedIndex(0);
            selectPeriod("Daily Report");
        });

        JPanel pActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 14));
        pActions.setOpaque(false);
        pActions.add(btnApply);
        pActions.add(btnReset);

        bottomRow.add(pReportType);
        bottomRow.add(dateGroup);
        bottomRow.add(pOrigin);
        bottomRow.add(pCashier);
        bottomRow.add(pPayment);
        bottomRow.add(pActions);

        card.add(topRow, BorderLayout.NORTH);
        card.add(bottomRow, BorderLayout.CENTER);
        return card;
    }

    private JPanel createLabeledControl(String label, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(new Color(120, 130, 125));

        p.add(lbl);
        p.add(Box.createVerticalStrut(2));
        p.add(comp);
        return p;
    }

    private JButton createSmallFilterButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(new Color(60, 70, 65));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleDropdown(JComboBox<String> cmb) {
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cmb.setBackground(Color.WHITE);
        cmb.setFocusable(false);
    }

    private class PeriodButton extends JButton {
        private boolean isSel = false;
        private final String periodName;

        public PeriodButton(String title, String periodName) {
            super(title);
            this.periodName = periodName;
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setForeground(new Color(70, 80, 75));
            setBackground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(5, 10, 5, 10));

            addActionListener(e -> selectPeriod(periodName));
        }

        public void setSelected(boolean b) {
            this.isSel = b;
            if (isSel) {
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else {
                setForeground(new Color(70, 80, 75));
                setFont(new Font("Segoe UI", Font.PLAIN, 11));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSel) {
                g2.setColor(COLOR_PRIMARY_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.setColor(COLOR_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private void selectPeriod(String periodName) {
        this.selectedPeriod = periodName;
        for (PeriodButton pb : periodButtons) {
            pb.setSelected(pb.periodName.equalsIgnoreCase(periodName));
        }
        cmbReportType.setSelectedItem(periodName);
        generateReport();
    }

    private JPanel createKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(3000, 96));
        row.setPreferredSize(new Dimension(1000, 96));

        row.add(createKpiCardDual("TOTAL GROSS SALES", lblGrossSales, lblCashNonCash, new Color(209, 250, 229), "peso"));
        row.add(createKpiCardDual("TRANSACTIONS COUNT", lblTxnCount, lblAvgOrder, new Color(237, 243, 236), "receipt"));
        row.add(createKpiCardDual("SACKS & VOLUME SOLD", lblSacksSold, lblVolTotal, new Color(243, 232, 255), "package"));
        row.add(createKpiCardDual("PACKAGING BREAKDOWN", lblPackBags, lblLooseSold, new Color(254, 243, 199), "bag"));

        return row;
    }

    private JPanel createKpiCardDual(String tag, JLabel valLbl, JLabel subValLbl, Color badgeBg, String iconType) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(8, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTag = new JLabel(tag);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(UIUtils.COLOR_TEXT_MUTED);

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        subValLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subValLbl.setForeground(UIUtils.COLOR_TEXT_MUTED);

        left.add(lblTag);
        left.add(Box.createVerticalStrut(3));
        left.add(valLbl);
        left.add(Box.createVerticalStrut(2));
        left.add(subValLbl);

        // Right icon badge
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setStroke(new BasicStroke(1.5f));

                switch (iconType) {
                    case "peso":
                        g2.setColor(new Color(5, 150, 105));
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        String p = "₱";
                        int pw = g2.getFontMetrics().stringWidth(p);
                        g2.drawString(p, cx - pw / 2, cy + 5);
                        break;
                    case "receipt":
                        g2.setColor(new Color(27, 77, 50));
                        g2.drawRoundRect(cx - 5, cy - 6, 10, 13, 2, 2);
                        g2.drawLine(cx - 3, cy - 2, cx + 3, cy - 2);
                        g2.drawLine(cx - 3, cy + 2, cx + 3, cy + 2);
                        break;
                    case "package":
                        g2.setColor(new Color(147, 51, 234));
                        g2.drawRect(cx - 6, cy - 6, 12, 12);
                        g2.drawLine(cx - 6, cy - 2, cx + 6, cy - 2);
                        g2.drawLine(cx, cy - 2, cx, cy + 6);
                        break;
                    case "bag":
                        g2.setColor(new Color(180, 83, 9));
                        g2.drawArc(cx - 3, cy - 6, 6, 6, 0, 180);
                        g2.drawRoundRect(cx - 6, cy - 2, 12, 10, 2, 2);
                        break;
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(34, 34));
        badge.setMaximumSize(new Dimension(34, 34));

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        wrap.setOpaque(false);
        wrap.add(badge);

        card.add(left, BorderLayout.CENTER);
        card.add(wrap, BorderLayout.EAST);
        return card;
    }

    private JPanel createVarietyTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(COLOR_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 16, 16));

        // Card Title
        JLabel lblTableTitle = new JLabel("Sales Volume & Revenue by Rice Variety");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTableTitle.setForeground(new Color(20, 30, 25));

        // Header Row
        JPanel tableHeader = createVarietyTableHeader();

        JPanel topGroup = new JPanel(new BorderLayout(0, 6));
        topGroup.setOpaque(false);
        topGroup.add(lblTableTitle, BorderLayout.NORTH);
        topGroup.add(tableHeader, BorderLayout.CENTER);
        card.add(topGroup, BorderLayout.NORTH);

        rowsContainer.setLayout(new BoxLayout(rowsContainer, BoxLayout.Y_AXIS));
        rowsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(rowsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel createVarietyTableHeader() {
        JPanel th = new JPanel(new GridBagLayout());
        th.setBackground(COLOR_TH_BG);
        th.setPreferredSize(new Dimension(1000, 36));
        th.setMinimumSize(new Dimension(800, 36));
        th.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        th.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 218, 198)),
                new EmptyBorder(8, 16, 8, 16)
        ));

        double[] weights = {0.16, 0.08, 0.08, 0.07, 0.07, 0.07, 0.07, 0.07, 0.09, 0.10, 0.11, 0.08};
        String[] titles = {
            "RICE VARIETY", "ORIGIN", "GRAIN TYPE", "50KG SACKS", "25KG HALF", "20KG PACKS",
            "10KG PACKS", "5KG PACKS", "TINATAKAL (KG)", "TOTAL VOLUME", "TOTAL REVENUE", "% SHARE"
        };

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 4, 0, 4);
        c.weighty = 1.0;

        for (int i = 0; i < titles.length; i++) {
            c.gridx = i;
            c.weightx = weights[i];

            JLabel lbl = new JLabel(titles[i], SwingConstants.LEFT);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lbl.setForeground(new Color(70, 75, 70));
            th.add(lbl, c);
        }
        UIUtils.alignTableContentLeft(th);
        return th;
    }

    private void loadCashierList() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT DISTINCT cashier_name FROM sales_transaction WHERE cashier_name IS NOT NULL AND cashier_name != ''")) {
            while (rs.next()) {
                String c = rs.getString(1);
                cmbCashier.addItem(c);
            }
        } catch (Exception ignored) {
        }
    }

    public void generateReport() {
        try {
            String pType = selectedPeriod.replace(" Report", "");
            String dateParam = selectedDate.toString();
            int quarter = (selectedDate.getMonthValue() - 1) / 3 + 1;
            int year = selectedDate.getYear();

            currentReport = reportService.generateReport(pType, dateParam, quarter, year);

            // Update Period Header Banner
            lblPeriodBadge.setText(selectedPeriod.toUpperCase());
            DateTimeFormatter bannerFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
            lblPeriodDetail.setText("Period: " + selectedPeriod + ": " + selectedDate.format(bannerFmt));

            // Compute KPIs
            double gross = currentReport.totalRevenue;
            lblGrossSales.setText(UIUtils.formatCurrency(gross));

            // Non-cash vs cash approximation from report
            double cashSum = 0.0;
            double nonCashSum = 0.0;
            try (Connection conn = DatabaseManager.getConnection();
                 Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT payment_method, COALESCE(SUM(total_amount), 0) FROM sales_transaction WHERE transaction_status = 'Completed' GROUP BY payment_method")) {
                while (rs.next()) {
                    String m = rs.getString(1);
                    double val = rs.getDouble(2);
                    if ("Cash".equalsIgnoreCase(m)) cashSum += val;
                    else nonCashSum += val;
                }
            } catch (Exception ignored) {}

            lblCashNonCash.setText("Cash: " + UIUtils.formatCurrency(cashSum) + " | Non-Cash: " + UIUtils.formatCurrency(nonCashSum));

            int count = currentReport.transactionCount;
            lblTxnCount.setText(count + " completed");
            lblAvgOrder.setText("Avg: " + UIUtils.formatCurrency(currentReport.averageOrderValue) + " / txn");

            lblSacksSold.setText(currentReport.totalSacksSold + " sacks");
            lblVolTotal.setText(String.format("%,.2f kg total equivalent", currentReport.totalKgSold));

            int prepackedCount = 0;
            double looseSum = 0.0;
            for (ReportService.VarietySalesRow r : currentReport.varietyRows) {
                prepackedCount += r.p25Sold + r.p20Sold + r.p10Sold + r.p5Sold;
                looseSum += r.looseKgSold;
            }
            lblPackBags.setText(prepackedCount + " pre-packed bags");
            lblLooseSold.setText(String.format("%.1f kg loose tinatakal", looseSum));

            rebuildVarietyRows();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void rebuildVarietyRows() {
        rowsContainer.removeAll();
        if (currentReport == null) return;

        double[] weights = {0.16, 0.08, 0.09, 0.07, 0.07, 0.07, 0.07, 0.07, 0.09, 0.10, 0.11, 0.08};
        double grandTotal = currentReport.totalRevenue > 0 ? currentReport.totalRevenue : 1.0;

        String originFilter = (String) cmbOrigin.getSelectedItem();

        for (ReportService.VarietySalesRow r : currentReport.varietyRows) {
            if (!"All Origins".equals(originFilter) && !r.origin.equalsIgnoreCase(originFilter)) {
                continue;
            }

            JPanel row = new JPanel(new GridBagLayout());
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(6, 16, 6, 16));
            row.setPreferredSize(new Dimension(1000, 48));
            row.setMinimumSize(new Dimension(800, 48));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            row.setAlignmentX(Component.CENTER_ALIGNMENT);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 4, 0, 4);
            c.weighty = 1.0;

            // 1. Variety Name (Wrapped with maximum width, excess text to second line)
            JPanel cell1 = new JPanel();
            cell1.setLayout(new BoxLayout(cell1, BoxLayout.Y_AXIS));
            cell1.setOpaque(false);
            cell1.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblName = new JLabel("<html><body style='margin:0;padding:0;'><div style='width:125px;line-height:1.15;'>" 
                    + r.productName + "</div></body></html>");
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblName.setForeground(new Color(20, 30, 25));
            lblName.setToolTipText(r.productName);
            lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
            cell1.add(lblName);

            // 2. Origin
            JPanel pOrigin = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pOrigin.setOpaque(false);
            boolean isLocal = "Local".equalsIgnoreCase(r.origin);
            JLabel badgeOrigin = createBadgePill(r.origin,
                    isLocal ? new Color(240, 253, 244) : new Color(224, 242, 254),
                    isLocal ? new Color(22, 101, 52) : new Color(14, 116, 144));
            pOrigin.add(badgeOrigin);

            // 3. Grain Type
            JLabel lblGrain = new JLabel(r.origin != null ? r.origin : "Rice", SwingConstants.LEFT);
            lblGrain.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblGrain.setForeground(new Color(70, 80, 75));

            // 4. 50kg Sacks
            JLabel lbl50 = createAlignedNumber(String.valueOf(r.sacksSold));
            // 5. 25kg Half
            JLabel lbl25 = createAlignedNumber(String.valueOf(r.p25Sold));
            // 6. 20kg Packs
            JLabel lbl20 = createAlignedNumber(String.valueOf(r.p20Sold));
            // 7. 10kg Packs
            JLabel lbl10 = createAlignedNumber(String.valueOf(r.p10Sold));
            // 8. 5kg Packs
            JLabel lbl5 = createAlignedNumber(String.valueOf(r.p5Sold));

            // 9. Tinatakal (Kg)
            JLabel lblLoose = createAlignedNumber(String.format("%.1f kg", r.looseKgSold));

            // 10. Total Volume
            JLabel lblVol = createAlignedNumber(String.format("%,.1f kg", r.totalVolumeKg));
            lblVol.setFont(new Font("Segoe UI", Font.BOLD, 11));

            // 11. Total Revenue
            JLabel lblRev = new JLabel(UIUtils.formatCurrency(r.totalRevenue), SwingConstants.LEFT);
            lblRev.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblRev.setForeground(new Color(20, 30, 25));

            // 12. % Share with mini progress bar (left-aligned)
            double sharePct = (r.totalRevenue / grandTotal) * 100.0;
            JPanel pShare = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            pShare.setOpaque(false);

            JPanel bar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(new Color(230, 225, 215));
                    g.fillRect(0, 2, 28, 4);
                    g.setColor(COLOR_PRIMARY_GREEN);
                    int fillW = (int) Math.round((sharePct / 100.0) * 28.0);
                    g.fillRect(0, 2, Math.max(1, fillW), 4);
                }
            };
            bar.setPreferredSize(new Dimension(28, 8));
            bar.setOpaque(false);

            JLabel lblPct = new JLabel(String.format("%.1f%%", sharePct), SwingConstants.LEFT);
            lblPct.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            lblPct.setForeground(new Color(70, 80, 75));

            pShare.add(bar);
            pShare.add(lblPct);

            Component[] cells = {cell1, pOrigin, lblGrain, lbl50, lbl25, lbl20, lbl10, lbl5, lblLoose, lblVol, lblRev, pShare};
            for (int i = 0; i < cells.length; i++) {
                c.gridx = i;
                c.weightx = weights[i];
                row.add(cells[i], c);
            }

            UIUtils.alignTableContentLeft(row);

            rowsContainer.add(row);

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(240, 235, 222));
            sep.setAlignmentX(Component.CENTER_ALIGNMENT);
            rowsContainer.add(sep);
        }

        rowsContainer.revalidate();
        rowsContainer.repaint();
    }

    private JLabel createAlignedNumber(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.LEFT);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(40, 50, 45));
        return lbl;
    }

    private JLabel createCenteredNumber(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(40, 50, 45));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private JLabel createBadgePill(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(1, 5, 1, 5));
        return lbl;
    }

    public void exportCsv() {
        if (currentReport == null) return;
        try {
            File f = reportService.exportToCsv(currentReport);
            JOptionPane.showMessageDialog(this, "Sales report successfully exported to CSV:\n" + f.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void printStatement() {
        if (currentReport == null) return;
        try {
            JTextArea area = new JTextArea();
            area.setFont(new Font("Monospaced", Font.PLAIN, 10));

            StringBuilder sb = new StringBuilder();
            sb.append("=================================================================\n");
            sb.append("              ROBERICE - SALES & INVENTORY SYSTEM               \n");
            sb.append("                 OFFICIAL SALES AUDIT STATEMENT                  \n");
            sb.append("=================================================================\n\n");
            sb.append(currentReport.periodTitle).append("\n");
            sb.append("Period: ").append(currentReport.periodLabel).append("\n");
            sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
            sb.append("-----------------------------------------------------------------\n");
            sb.append("KEY PERFORMANCE INDICATORS\n");
            sb.append(String.format("Total Gross Sales:       %s\n", UIUtils.formatCurrency(currentReport.totalRevenue)));
            sb.append(String.format("Completed Orders:        %d\n", currentReport.transactionCount));
            sb.append(String.format("Total Whole Sacks Sold:  %d sacks\n", currentReport.totalSacksSold));
            sb.append(String.format("Total Volume Sold:       %.2f kg\n", currentReport.totalKgSold));
            sb.append(String.format("Average Basket Size:     %s\n", UIUtils.formatCurrency(currentReport.averageOrderValue)));
            sb.append("-----------------------------------------------------------------\n\n");
            sb.append(String.format("%-24s %-8s %-8s %-12s %-12s\n", "Variety", "Sacks", "Loose", "Volume (Kg)", "Revenue"));
            sb.append("-----------------------------------------------------------------\n");
            for (ReportService.VarietySalesRow r : currentReport.varietyRows) {
                sb.append(String.format("%-24s %-8d %-8.1f %-12.2f %-12s\n",
                        r.productName, r.sacksSold, r.looseKgSold, r.totalVolumeKg, UIUtils.formatCurrency(r.totalRevenue)));
            }
            sb.append("=================================================================\n");

            area.setText(sb.toString());
            area.print();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Printer Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
