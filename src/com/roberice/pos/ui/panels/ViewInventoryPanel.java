package com.roberice.pos.ui.panels;

import com.roberice.pos.db.InventoryDAO;
import com.roberice.pos.model.Inventory;
import com.roberice.pos.model.InventoryBatch;
import com.roberice.pos.service.AuthSession;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * View Inventory Panel with FIFO Batch Lot Drill-Down and Contextual Actions (Section 1.7 in User Manual).
 */
public class ViewInventoryPanel extends JPanel {
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    // Summary Table
    private final DefaultTableModel summaryModel = new DefaultTableModel(
            new String[]{"ID", "Rice Variety", "Origin", "Grain Type", "50kg Sacks", "25kg", "20kg", "10kg", "5kg", "Loose (Kg)", "Total (Kg)", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblSummary = new JTable(summaryModel);

    // Batch Drill-Down Table
    private final DefaultTableModel batchModel = new DefaultTableModel(
            new String[]{"Batch Lot Code", "Received Date", "Storage Age", "50kg Sacks", "25kg", "20kg", "10kg", "5kg", "Loose (Kg)", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblBatches = new JTable(batchModel);

    private final JLabel lblSelectedProduct = new JLabel("Select a rice variety row above to inspect FIFO delivery lot batches.");

    public ViewInventoryPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // TOP: Live Inventory Summary Card
        JPanel topCard = UIUtils.createCard();
        topCard.setLayout(new BorderLayout(0, 10));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(Color.WHITE);
        JLabel lblTopTitle = new JLabel("Live Warehouse Inventory Balances");
        lblTopTitle.setFont(UIUtils.FONT_TITLE);
        lblTopTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topActions.setBackground(Color.WHITE);

        JButton btnOpenSack = UIUtils.createPrimaryButton("Open Sack for Loose Rice");
        btnOpenSack.addActionListener(e -> openSackForSelected());

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh Table");
        btnRefresh.addActionListener(e -> refreshData());

        topActions.add(btnOpenSack);
        topActions.add(btnRefresh);

        topHeader.add(lblTopTitle, BorderLayout.WEST);
        topHeader.add(topActions, BorderLayout.EAST);
        topCard.add(topHeader, BorderLayout.NORTH);

        UIUtils.styleTable(tblSummary);
        JScrollPane scrollSummary = new JScrollPane(tblSummary);
        scrollSummary.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        scrollSummary.setPreferredSize(new Dimension(800, 250));
        topCard.add(scrollSummary, BorderLayout.CENTER);

        // BOTTOM: FIFO Batch Drill-Down Card
        JPanel bottomCard = UIUtils.createCard();
        bottomCard.setLayout(new BorderLayout(0, 10));

        JPanel bHeader = new JPanel(new BorderLayout());
        bHeader.setBackground(Color.WHITE);

        JLabel lblBatchTitle = new JLabel("FIFO Batch Lot Breakdown");
        lblBatchTitle.setFont(UIUtils.FONT_TITLE);
        lblBatchTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        lblSelectedProduct.setFont(UIUtils.FONT_SMALL);
        lblSelectedProduct.setForeground(UIUtils.COLOR_TEXT_MUTED);

        bHeader.add(lblBatchTitle, BorderLayout.WEST);
        bHeader.add(lblSelectedProduct, BorderLayout.EAST);
        bottomCard.add(bHeader, BorderLayout.NORTH);

        UIUtils.styleTable(tblBatches);
        JScrollPane scrollBatches = new JScrollPane(tblBatches);
        scrollBatches.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        scrollBatches.setPreferredSize(new Dimension(800, 180));
        bottomCard.add(scrollBatches, BorderLayout.CENTER);

        // Selection listener to load batches
        tblSummary.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedBatches();
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topCard, bottomCard);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
        refreshData();
    }

    public void refreshData() {
        try {
            List<Inventory> list = inventoryDAO.getInventorySummary();
            summaryModel.setRowCount(0);

            for (Inventory inv : list) {
                summaryModel.addRow(new Object[]{
                        inv.getProductId(),
                        inv.getProductName(),
                        inv.getOrigin(),
                        inv.getRiceType(),
                        inv.getStockSacks(),
                        inv.getPackaged25kg(),
                        inv.getPackaged20kg(),
                        inv.getPackaged10kg(),
                        inv.getPackaged5kg(),
                        String.format("%.2f", inv.getStockKg()),
                        String.format("%.2f", inv.getTotalStockKg()),
                        inv.getStockStatus()
                });
            }

            if (tblSummary.getRowCount() > 0) {
                tblSummary.setRowSelectionInterval(0, 0);
                loadSelectedBatches();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadSelectedBatches() {
        int row = tblSummary.getSelectedRow();
        if (row < 0) return;

        int productId = (int) tblSummary.getValueAt(row, 0);
        String name = (String) tblSummary.getValueAt(row, 1);
        lblSelectedProduct.setText("Showing active delivery lots for: " + name);

        try {
            List<InventoryBatch> batches = inventoryDAO.getBatchesForProduct(productId, false);
            batchModel.setRowCount(0);

            for (InventoryBatch b : batches) {
                long days = b.getAgeInDays();
                String ageStr = days + " days" + (b.isOldStock() ? " (Aging >60d)" : "");

                batchModel.addRow(new Object[]{
                        b.getBatchCode(),
                        b.getReceivedDate() != null ? b.getReceivedDate().toString() : "-",
                        ageStr,
                        b.getStockSacks(),
                        b.getPackaged25kg(),
                        b.getPackaged20kg(),
                        b.getPackaged10kg(),
                        b.getPackaged5kg(),
                        String.format("%.2f", b.getStockKg()),
                        b.getStatus()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openSackForSelected() {
        int row = tblSummary.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a rice variety row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) tblSummary.getValueAt(row, 0);
        String name = (String) tblSummary.getValueAt(row, 1);

        String input = JOptionPane.showInputDialog(this,
                "How many 50kg whole sacks of " + name + " do you want to open into loose inventory?",
                "Open Whole Sack", JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int sacks = Integer.parseInt(input.trim());
                if (sacks > 0) {
                    boolean ok = inventoryDAO.openSack(productId, "AUTO", sacks, AuthSession.getFullName());
                    if (ok) {
                        JOptionPane.showMessageDialog(this,
                                "Successfully opened " + sacks + "x 50kg whole sack(s) of " + name + " into loose inventory.",
                                "Sack Opened", JOptionPane.INFORMATION_MESSAGE);
                        refreshData();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error Opening Sack", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
