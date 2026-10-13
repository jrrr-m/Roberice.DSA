package com.roberice.dsa.ui.views;

import com.roberice.dsa.model.SalesTransaction;
import com.roberice.dsa.model.TransactionItem;
import com.roberice.dsa.service.TransactionService;
import com.roberice.dsa.util.Session;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionsView extends JPanel {

    private final List<SalesTransaction> transactions = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable txTable;

    private JTextField searchField;
    private JComboBox<String> statusCombo;

    public TransactionsView() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_DARK);

        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Top Controls
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Search transaction ID or cashier...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadData(); }
            @Override public void removeUpdate(DocumentEvent e) { loadData(); }
            @Override public void changedUpdate(DocumentEvent e) { loadData(); }
        });

        statusCombo = new JComboBox<>(new String[]{"All", "Completed", "Cancelled"});
        statusCombo.addActionListener(e -> loadData());

        leftFilters.add(searchField);
        leftFilters.add(statusCombo);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton viewBtn = UIUtils.createPrimaryButton("🔍 View Order Details", e -> viewSelectedDetails());
        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh", e -> loadData());

        rightActions.add(viewBtn);
        rightActions.add(refreshBtn);

        topBar.add(leftFilters, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        content.add(topBar, BorderLayout.NORTH);

        // Transactions Table
        String[] cols = {"Tx ID", "Date & Time", "Cashier", "Payment Method", "Total Amount", "Paid", "Change", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        txTable = UIUtils.createStyledTable(tableModel);
        txTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(txTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                String search = searchField.getText().trim();
                String status = (String) statusCombo.getSelectedItem();

                transactions.clear();
                transactions.addAll(TransactionService.getTransactions(search, status, 100, 0));

                tableModel.setRowCount(0);
                for (SalesTransaction t : transactions) {
                    tableModel.addRow(new Object[]{
                            "#" + t.getTransactionId(),
                            t.getTransactionDate(),
                            t.getCashierName(),
                            t.getPaymentMethod(),
                            UIUtils.formatCurrency(t.getTotalAmount()),
                            UIUtils.formatCurrency(t.getPaymentAmount()),
                            UIUtils.formatCurrency(t.getChangeAmount()),
                            t.getTransactionStatus()
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void viewSelectedDetails() {
        int r = txTable.getSelectedRow();
        if (r < 0) {
            UIUtils.showError(this, "Please select a transaction row from the table.");
            return;
        }

        String idStr = (String) tableModel.getValueAt(r, 0);
        int txId = Integer.parseInt(idStr.replace("#", ""));
        SalesTransaction tx = TransactionService.getTransactionDetails(txId);
        if (tx == null) {
            UIUtils.showError(this, "Could not load transaction details.");
            return;
        }

        showDetailsModal(tx);
    }

    private void showDetailsModal(SalesTransaction tx) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Order Details - Invoice #" + tx.getTransactionId(), true);
        dlg.setSize(580, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnl = new JPanel(new BorderLayout(0, 14));
        pnl.setBackground(UIUtils.BG_CARD);
        pnl.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header info
        JPanel metaPanel = new JPanel(new GridLayout(2, 2, 8, 4));
        metaPanel.setOpaque(false);
        metaPanel.add(new JLabel("Date: " + tx.getTransactionDate()));
        metaPanel.add(new JLabel("Cashier: " + tx.getCashierName()));
        metaPanel.add(new JLabel("Method: " + tx.getPaymentMethod()));
        metaPanel.add(new JLabel("Status: " + tx.getTransactionStatus()));
        pnl.add(metaPanel, BorderLayout.NORTH);

        // Line items table
        String[] itemCols = {"Item", "Unit", "Qty", "Unit Price", "Subtotal"};
        DefaultTableModel iModel = new DefaultTableModel(itemCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (TransactionItem it : tx.getItems()) {
            iModel.addRow(new Object[]{
                    it.getProductName(),
                    it.getUnit(),
                    it.getQuantity(),
                    UIUtils.formatCurrency(it.getUnitPrice()),
                    UIUtils.formatCurrency(it.getSubtotal())
            });
        }
        JTable iTable = UIUtils.createStyledTable(iModel);
        pnl.add(new JScrollPane(iTable), BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JLabel totalLbl = new JLabel("Total: " + UIUtils.formatCurrency(tx.getTotalAmount()) + "  •  Paid: " + UIUtils.formatCurrency(tx.getPaymentAmount()) + "  •  Change: " + UIUtils.formatCurrency(tx.getChangeAmount()));
        totalLbl.setFont(UIUtils.FONT_BOLD);
        totalLbl.setForeground(UIUtils.ACCENT_SUCCESS);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        if (!"Cancelled".equalsIgnoreCase(tx.getTransactionStatus()) && Session.hasPermission("transactions")) {
            JButton voidBtn = UIUtils.createDangerButton("Void / Cancel Order", e -> {
                if (UIUtils.showConfirm(dlg, "Are you sure you want to cancel Invoice #" + tx.getTransactionId() + "? Sold rice quantities will be automatically returned to inventory.", "Confirm Void")) {
                    try {
                        String user = Session.getCurrentUser() != null ? Session.getCurrentUser().getFullName() : "Admin";
                        TransactionService.cancelTransaction(tx.getTransactionId(), "Voided by user", user);
                        UIUtils.showSuccess(dlg, "Transaction voided and inventory restored.");
                        dlg.dispose();
                        loadData();
                    } catch (Exception ex) {
                        UIUtils.showError(dlg, "Failed to void transaction: " + ex.getMessage());
                    }
                }
            });
            btnPanel.add(voidBtn);
        }

        JButton closeBtn = UIUtils.createSecondaryButton("Close", e -> dlg.dispose());
        btnPanel.add(closeBtn);

        footer.add(totalLbl, BorderLayout.WEST);
        footer.add(btnPanel, BorderLayout.EAST);
        pnl.add(footer, BorderLayout.SOUTH);

        dlg.add(pnl, BorderLayout.CENTER);
        dlg.setVisible(true);
    }
}
