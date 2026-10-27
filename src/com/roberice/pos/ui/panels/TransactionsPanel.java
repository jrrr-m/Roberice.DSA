package com.roberice.pos.ui.panels;

import com.roberice.pos.db.SalesDAO;
import com.roberice.pos.model.SalesTransaction;
import com.roberice.pos.model.TransactionItem;
import com.roberice.pos.ui.ReceiptDialog;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Historical Customer Receipts & Transaction Log Panel (Section 1.8 in User Manual).
 */
public class TransactionsPanel extends JPanel {
    private final SalesDAO salesDAO = new SalesDAO();

    private final JTextField txtSearch = new JTextField(15);
    private final JComboBox<Integer> cmbLimit = new JComboBox<>(new Integer[]{100, 250, 500, 1000});

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Transaction ID", "Receipt #", "Date & Time", "Cashier", "Payment Method", "Gross Total", "Received", "Change", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblTransactions = new JTable(tableModel);

    public TransactionsPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Completed Sales Transactions & Order Receipts");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Color.WHITE);

        actions.add(new JLabel("Limit:"));
        actions.add(cmbLimit);
        cmbLimit.addActionListener(e -> refreshData());

        JButton btnViewReceipt = UIUtils.createPrimaryButton("View / Reprint Receipt");
        btnViewReceipt.addActionListener(e -> viewReceipt());

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        actions.add(btnViewReceipt);
        actions.add(btnRefresh);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        UIUtils.styleTable(tblTransactions);
        JScrollPane scroll = new JScrollPane(tblTransactions);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
        refreshData();
    }

    public void refreshData() {
        try {
            int limit = (int) cmbLimit.getSelectedItem();
            List<SalesTransaction> list = salesDAO.getTransactions(limit, txtSearch.getText().trim());
            tableModel.setRowCount(0);

            for (SalesTransaction tx : list) {
                tableModel.addRow(new Object[]{
                        tx.getTransactionId(),
                        String.format("REC-%06d", tx.getTransactionId()),
                        UIUtils.formatDate(tx.getTransactionDate()),
                        tx.getCashierName(),
                        tx.getPaymentMethod(),
                        UIUtils.formatCurrency(tx.getTotalAmount()),
                        UIUtils.formatCurrency(tx.getPaymentAmount()),
                        UIUtils.formatCurrency(tx.getChangeAmount()),
                        tx.getTransactionStatus()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void viewReceipt() {
        int row = tblTransactions.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int txId = (int) tblTransactions.getValueAt(row, 0);
        try {
            // Load transaction details and items
            List<TransactionItem> items = salesDAO.getItemsForTransaction(txId);
            SalesTransaction tx = new SalesTransaction();
            tx.setTransactionId(txId);
            tx.setCashierName((String) tblTransactions.getValueAt(row, 3));
            tx.setPaymentMethod((String) tblTransactions.getValueAt(row, 4));

            // Parse currency string or compute from items
            double total = 0;
            for (TransactionItem it : items) total += it.getSubtotal();
            tx.setTotalAmount(total);
            tx.setPaymentAmount(total);
            tx.setChangeAmount(0);
            tx.setItems(items);

            ReceiptDialog dialog = new ReceiptDialog((Frame) SwingUtilities.getWindowAncestor(this), tx);
            dialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
