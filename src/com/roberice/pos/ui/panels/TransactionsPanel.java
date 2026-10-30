package com.roberice.pos.ui.panels;

import com.roberice.pos.db.DatabaseManager;
import com.roberice.pos.db.SalesDAO;
import com.roberice.pos.model.SalesTransaction;
import com.roberice.pos.model.TransactionItem;
import com.roberice.pos.ui.ReceiptDialog;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Modern Transaction History Panel.
 * Matches the uploaded design with KPI summary cards (Transactions Count, Total Revenue, Cash Collected),
 * filter bar (Search, Cashier, Payment Method), warm ivory table layout,
 * summarized items purchased list, payment method pills, total amount, and View Receipt action.
 */
public class TransactionsPanel extends JPanel {
    private final SalesDAO salesDAO = new SalesDAO();

    // Metric Labels
    private final JLabel lblTxnCount = new JLabel("0");
    private final JLabel lblTotalRevenue = new JLabel("₱0.00");
    private final JLabel lblCashCollected = new JLabel("₱0.00");

    // Filters
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cmbCashier = new JComboBox<>(new String[]{"All Cashiers"});
    private final JComboBox<String> cmbPaymentMethod = new JComboBox<>(new String[]{"All Methods", "Cash", "GCash", "Maya"});

    // Transactions Data & Cache
    private List<SalesTransaction> allTransactions = new ArrayList<>();
    private final Map<Integer, List<TransactionItem>> itemsCache = new HashMap<>();

    // Container for dynamic table rows
    private final JPanel rowsContainer = new JPanel();

    // Design Colors
    private static final Color COLOR_APP_BG = new Color(246, 243, 233);
    private static final Color COLOR_CARD_BG = new Color(255, 253, 246);
    private static final Color COLOR_CARD_BORDER = new Color(234, 230, 218);
    private static final Color COLOR_TH_BG = new Color(242, 234, 213);

    public TransactionsPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(COLOR_APP_BG);
        setBorder(new EmptyBorder(10, 16, 16, 16));

        JPanel mainLayout = new JPanel();
        mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
        mainLayout.setOpaque(false);

        // 1. KPI Cards Row (Transactions Count, Total Revenue, Cash Collected)
        JPanel kpiRow = new JPanel(new GridLayout(1, 3, 14, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(3000, 96));
        kpiRow.setPreferredSize(new Dimension(1000, 96));

        kpiRow.add(createKpiCard("TRANSACTIONS COUNT", lblTxnCount, new Color(237, 243, 236), "receipt"));
        kpiRow.add(createKpiCard("TOTAL REVENUE", lblTotalRevenue, new Color(209, 250, 229), "peso"));
        kpiRow.add(createKpiCard("CASH COLLECTED", lblCashCollected, new Color(243, 232, 255), "cash"));

        mainLayout.add(kpiRow);
        mainLayout.add(Box.createVerticalStrut(14));

        // 2. Main Card containing Filters & Table
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
        card.setBorder(new EmptyBorder(14, 16, 16, 16));

        // Filter Bar (Search + Cashier + Payment Method)
        JPanel filterBar = createFilterBar();
        card.add(filterBar, BorderLayout.NORTH);

        // Table Panel (Header + Scrollable Rows)
        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setOpaque(false);

        JPanel tableHeader = createTableHeader();
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        rowsContainer.setLayout(new BoxLayout(rowsContainer, BoxLayout.Y_AXIS));
        rowsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(rowsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        card.add(tablePanel, BorderLayout.CENTER);
        mainLayout.add(card);

        add(mainLayout, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createKpiCard(String tag, JLabel valLbl, Color badgeBg, String iconType) {
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

        left.add(lblTag);
        left.add(Box.createVerticalStrut(4));
        left.add(valLbl);

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
                    case "receipt":
                        g2.setColor(new Color(27, 77, 50));
                        g2.drawRoundRect(cx - 5, cy - 6, 10, 13, 2, 2);
                        g2.drawLine(cx - 3, cy - 2, cx + 3, cy - 2);
                        g2.drawLine(cx - 3, cy + 2, cx + 3, cy + 2);
                        break;
                    case "peso":
                        g2.setColor(new Color(5, 150, 105));
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        String p = "₱";
                        int pw = g2.getFontMetrics().stringWidth(p);
                        g2.drawString(p, cx - pw / 2, cy + 5);
                        break;
                    case "cash":
                        g2.setColor(new Color(147, 51, 234));
                        g2.drawRoundRect(cx - 7, cy - 5, 14, 10, 2, 2);
                        g2.drawOval(cx - 2, cy - 2, 4, 4);
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

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);

        // Search Input
        JPanel searchBox = new JPanel(new BorderLayout(6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(COLOR_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(420, 34));
        searchBox.setBorder(new EmptyBorder(4, 10, 4, 10));

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lblSearchIcon.setForeground(new Color(150, 155, 150));

        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by Txn ID, cashier, or item...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { rebuildRows(); }
            public void removeUpdate(DocumentEvent e) { rebuildRows(); }
            public void changedUpdate(DocumentEvent e) { rebuildRows(); }
        });

        searchBox.add(lblSearchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        // Right dropdowns
        JPanel rightFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightFilters.setOpaque(false);

        styleDropdown(cmbCashier);
        styleDropdown(cmbPaymentMethod);

        cmbCashier.addActionListener(e -> rebuildRows());
        cmbPaymentMethod.addActionListener(e -> rebuildRows());

        rightFilters.add(cmbCashier);
        rightFilters.add(cmbPaymentMethod);

        bar.add(searchBox, BorderLayout.CENTER);
        bar.add(rightFilters, BorderLayout.EAST);
        return bar;
    }

    private void styleDropdown(JComboBox<String> cmb) {
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cmb.setBackground(Color.WHITE);
        cmb.setFocusable(false);
    }

    private JPanel createTableHeader() {
        JPanel th = new JPanel(new GridBagLayout());
        th.setBackground(COLOR_TH_BG);
        th.setPreferredSize(new Dimension(1000, 36));
        th.setMinimumSize(new Dimension(800, 36));
        th.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 218, 198)));

        // Columns: TXN ID, DATE / TIME, CASHIER, ITEMS PURCHASED, PAYMENT METHOD, TOTAL AMOUNT, ACTION
        double[] weights = {0.10, 0.16, 0.12, 0.30, 0.11, 0.11, 0.10};
        String[] titles = {
            "TXN ID", "DATE / TIME", "CASHIER", "ITEMS PURCHASED", "PAYMENT METHOD", "TOTAL AMOUNT", "ACTION"
        };

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weighty = 1.0;

        for (int i = 0; i < titles.length; i++) {
            c.gridx = i;
            c.weightx = weights[i];

            JLabel lbl = new JLabel(titles[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lbl.setForeground(new Color(70, 75, 70));
            lbl.setBorder(new EmptyBorder(0, 4, 0, 4));

            if (i == 4) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (i == 5) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (i == 6) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
            }

            th.add(lbl, c);
        }
        UIUtils.alignTableContentLeft(th);
        return th;
    }

    public void refreshData() {
        try {
            allTransactions = salesDAO.getTransactions(250, null);
            itemsCache.clear();

            // Populate items and compute KPI stats
            double revenue = 0.0;
            double cash = 0.0;
            Set<String> cashiers = new TreeSet<>();

            for (SalesTransaction tx : allTransactions) {
                revenue += tx.getTotalAmount();
                if ("Cash".equalsIgnoreCase(tx.getPaymentMethod())) {
                    cash += tx.getTotalAmount();
                }
                if (tx.getCashierName() != null && !tx.getCashierName().isEmpty()) {
                    cashiers.add(tx.getCashierName());
                }

                List<TransactionItem> items = salesDAO.getItemsForTransaction(tx.getTransactionId());
                itemsCache.put(tx.getTransactionId(), items);
            }

            lblTxnCount.setText(String.valueOf(allTransactions.size()));
            lblTotalRevenue.setText(UIUtils.formatCurrency(revenue));
            lblCashCollected.setText(UIUtils.formatCurrency(cash));

            // Populate cashier filter options
            String currentSelected = (String) cmbCashier.getSelectedItem();
            cmbCashier.removeAllItems();
            cmbCashier.addItem("All Cashiers");
            for (String c : cashiers) {
                cmbCashier.addItem(c);
            }
            if (currentSelected != null && !currentSelected.isEmpty()) {
                cmbCashier.setSelectedItem(currentSelected);
            }

            rebuildRows();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void rebuildRows() {
        rowsContainer.removeAll();

        String q = txtSearch.getText().trim().toLowerCase();
        String selectedCashier = (String) cmbCashier.getSelectedItem();
        String selectedMethod = (String) cmbPaymentMethod.getSelectedItem();

        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");

        for (SalesTransaction tx : allTransactions) {
            String txnCode = String.format("TXN-%04d", tx.getTransactionId());
            String dateStr = tx.getTransactionDate() != null ? df.format(tx.getTransactionDate()) : "-";
            String cashier = tx.getCashierName() != null ? tx.getCashierName() : "Cashier";
            String method = tx.getPaymentMethod() != null ? tx.getPaymentMethod() : "Cash";

            List<TransactionItem> items = itemsCache.getOrDefault(tx.getTransactionId(), Collections.emptyList());
            String itemsSummary = buildItemsSummary(items);

            // Filter checks
            if (!q.isEmpty()) {
                boolean match = txnCode.toLowerCase().contains(q)
                        || cashier.toLowerCase().contains(q)
                        || method.toLowerCase().contains(q)
                        || itemsSummary.toLowerCase().contains(q);
                if (!match) continue;
            }

            if (selectedCashier != null && !"All Cashiers".equals(selectedCashier)) {
                if (!selectedCashier.equalsIgnoreCase(cashier)) continue;
            }

            if (selectedMethod != null && !"All Methods".equals(selectedMethod)) {
                if (!selectedMethod.equalsIgnoreCase(method)) continue;
            }

            // Create row
            JPanel row = createTransactionRow(tx, txnCode, dateStr, cashier, itemsSummary, method, items);
            rowsContainer.add(row);

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(240, 235, 222));
            rowsContainer.add(sep);
        }

        rowsContainer.revalidate();
        rowsContainer.repaint();
    }

    private String buildItemsSummary(List<TransactionItem> items) {
        if (items == null || items.isEmpty()) return "General Rice Purchase";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            TransactionItem it = items.get(i);
            String qtyStr = (it.getQuantity() == (long) it.getQuantity())
                    ? String.valueOf((long) it.getQuantity())
                    : String.format("%.1f", it.getQuantity());

            String unitStr = it.getUnit() != null ? it.getUnit() : "unit";
            if (unitStr.equalsIgnoreCase("sack")) unitStr = "sack";
            else if (unitStr.equalsIgnoreCase("tinatakal")) unitStr = "tinatakal";

            sb.append(it.getProductName()).append(" (").append(qtyStr).append(" ").append(unitStr).append(")");
            if (i < items.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private JPanel createTransactionRow(SalesTransaction tx, String txnCode, String dateStr, String cashier, String itemsSummary, String method, List<TransactionItem> items) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(1000, 52));
        row.setMinimumSize(new Dimension(800, 52));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        double[] weights = {0.10, 0.16, 0.12, 0.30, 0.11, 0.11, 0.10};

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weighty = 1.0;

        // 1. TXN ID
        JLabel lblId = new JLabel(txnCode);
        lblId.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblId.setForeground(new Color(40, 50, 45));
        lblId.setBorder(new EmptyBorder(0, 4, 0, 4));

        // 2. Date / Time
        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(new Color(60, 70, 65));
        lblDate.setBorder(new EmptyBorder(0, 4, 0, 4));

        // 3. Cashier
        JLabel lblCashier = new JLabel(cashier);
        lblCashier.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCashier.setForeground(new Color(20, 30, 25));
        lblCashier.setBorder(new EmptyBorder(0, 4, 0, 4));

        // 4. Items Purchased
        JLabel lblItems = new JLabel(itemsSummary);
        lblItems.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblItems.setForeground(new Color(70, 80, 75));
        lblItems.setToolTipText(itemsSummary);
        lblItems.setBorder(new EmptyBorder(0, 4, 0, 4));

        // 5. Payment Method Pill
        JPanel pMethod = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pMethod.setOpaque(false);

        Color mBg, mFg;
        if ("Cash".equalsIgnoreCase(method)) {
            mBg = new Color(224, 242, 254);
            mFg = new Color(14, 116, 144);
        } else if ("GCash".equalsIgnoreCase(method) || "Maya".equalsIgnoreCase(method)) {
            mBg = new Color(243, 232, 255);
            mFg = new Color(126, 34, 206);
        } else {
            mBg = new Color(220, 252, 231);
            mFg = new Color(22, 101, 52);
        }
        JLabel badgeMethod = createBadgePill(method, mBg, mFg);
        pMethod.add(badgeMethod);

        // 6. Total Amount
        JLabel lblTotal = new JLabel(UIUtils.formatCurrency(tx.getTotalAmount()));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotal.setForeground(new Color(20, 30, 25));
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotal.setBorder(new EmptyBorder(0, 2, 0, 10));

        // 7. View Receipt Action Button
        JPanel pAction = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pAction.setOpaque(false);

        JButton btnReceipt = new JButton("View Receipt");
        btnReceipt.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnReceipt.setForeground(new Color(40, 50, 45));
        btnReceipt.setBackground(Color.WHITE);
        btnReceipt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 220, 210), 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        btnReceipt.setFocusPainted(false);
        btnReceipt.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReceipt.addActionListener(e -> showReceipt(tx, items));
        pAction.add(btnReceipt);

        Component[] cells = {lblId, lblDate, lblCashier, lblItems, pMethod, lblTotal, pAction};
        for (int i = 0; i < cells.length; i++) {
            c.gridx = i;
            c.weightx = weights[i];
            row.add(cells[i], c);
        }

        UIUtils.alignTableContentLeft(row);
        return row;
    }

    private JLabel createBadgePill(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(1, 6, 1, 6));
        return lbl;
    }

    private void showReceipt(SalesTransaction tx, List<TransactionItem> items) {
        tx.setItems(items);
        ReceiptDialog dialog = new ReceiptDialog((Frame) SwingUtilities.getWindowAncestor(this), tx);
        dialog.setVisible(true);
    }
}
