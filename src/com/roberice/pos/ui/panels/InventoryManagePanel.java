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
import java.time.LocalDate;
import java.util.List;

/**
 * Manage Inventory Panel: Restocking, Sack Openings, Repacking, and Physical Audits (Section 2.4 in User Manual).
 */
public class InventoryManagePanel extends JPanel {
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Rice Variety", "Origin", "Grain Type", "50kg Sacks", "25kg", "20kg", "10kg", "5kg", "Loose (Kg)", "Total Vol (Kg)", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblInventory = new JTable(tableModel);

    public InventoryManagePanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(0, 14));

        // Header & Actions
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Warehouse Inventory & Stock Operations");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Color.WHITE);

        JButton btnRestock = UIUtils.createPrimaryButton("+ Restock Shipment");
        btnRestock.addActionListener(e -> showRestockDialog());

        JButton btnOpenSack = UIUtils.createSecondaryButton("Open Sack");
        btnOpenSack.addActionListener(e -> showOpenSackDialog());

        JButton btnRepack = UIUtils.createSecondaryButton("Repack Bags");
        btnRepack.addActionListener(e -> showRepackDialog());

        JButton btnCorrect = UIUtils.createSecondaryButton("Physical Count");
        btnCorrect.addActionListener(e -> showCorrectionDialog());

        JButton btnSpoilage = UIUtils.createDangerButton("Log Spoilage / Damage");
        btnSpoilage.addActionListener(e -> showSpoilageDialog());

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        actions.add(btnRestock);
        actions.add(btnOpenSack);
        actions.add(btnRepack);
        actions.add(btnCorrect);
        actions.add(btnSpoilage);
        actions.add(btnRefresh);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        UIUtils.styleTable(tblInventory);
        JScrollPane scroll = new JScrollPane(tblInventory);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
        refreshData();
    }

    public void refreshData() {
        try {
            List<Inventory> list = inventoryDAO.getInventorySummary();
            tableModel.setRowCount(0);

            for (Inventory inv : list) {
                tableModel.addRow(new Object[]{
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getSelectedProductId() {
        int row = tblInventory.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a rice variety row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return (int) tblInventory.getValueAt(row, 0);
    }

    private String getSelectedProductName() {
        int row = tblInventory.getSelectedRow();
        return row >= 0 ? (String) tblInventory.getValueAt(row, 1) : "";
    }

    private void showRestockDialog() {
        int pId = getSelectedProductId();
        if (pId == -1) return;
        String pName = getSelectedProductName();

        JTextField txtBatch = new JTextField("BATCH-" + System.currentTimeMillis() % 10000);
        JTextField txtDate = new JTextField(LocalDate.now().toString());
        JTextField txtSacks = new JTextField("25");
        JTextField txtP25 = new JTextField("0");
        JTextField txtP20 = new JTextField("0");
        JTextField txtP10 = new JTextField("0");
        JTextField txtP5 = new JTextField("0");
        JTextField txtKg = new JTextField("0");
        JTextField txtNote = new JTextField("Shipment delivery from Central Luzon Mills with quality certificate.");

        JPanel form = new JPanel(new GridLayout(9, 2, 8, 8));
        form.add(new JLabel("Rice Variety:"));
        form.add(new JLabel("<b>" + pName + "</b>"));
        form.add(new JLabel("Batch / Lot Code:"));
        form.add(txtBatch);
        form.add(new JLabel("Delivery Date (YYYY-MM-DD):"));
        form.add(txtDate);
        form.add(new JLabel("Whole Sacks (50kg):"));
        form.add(txtSacks);
        form.add(new JLabel("25kg Half Sacks:"));
        form.add(txtP25);
        form.add(new JLabel("20kg Bags:"));
        form.add(txtP20);
        form.add(new JLabel("10kg Bags:"));
        form.add(txtP10);
        form.add(new JLabel("5kg Bags:"));
        form.add(txtP5);
        form.add(new JLabel("Supplier / Notes:"));
        form.add(txtNote);

        int res = JOptionPane.showConfirmDialog(this, form, "Restock Shipment - " + pName, JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int sacks = Integer.parseInt(txtSacks.getText().trim());
                int p25 = Integer.parseInt(txtP25.getText().trim());
                int p20 = Integer.parseInt(txtP20.getText().trim());
                int p10 = Integer.parseInt(txtP10.getText().trim());
                int p5 = Integer.parseInt(txtP5.getText().trim());
                double kg = Double.parseDouble(txtKg.getText().trim());

                boolean ok = inventoryDAO.restock(pId, txtBatch.getText().trim(), txtDate.getText().trim(), sacks, p25, p20, p10, p5, kg, txtNote.getText().trim(), AuthSession.getFullName());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Restock shipment recorded successfully!", "Restock Complete", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showOpenSackDialog() {
        int pId = getSelectedProductId();
        if (pId == -1) return;
        String pName = getSelectedProductName();

        String input = JOptionPane.showInputDialog(this,
                "Enter number of 50kg whole sacks to open into loose inventory for " + pName + ":",
                "Open Whole Sack", JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int sacks = Integer.parseInt(input.trim());
                if (sacks > 0) {
                    boolean ok = inventoryDAO.openSack(pId, "AUTO", sacks, AuthSession.getFullName());
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Successfully opened " + sacks + " whole sack(s) into loose inventory.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshData();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRepackDialog() {
        int pId = getSelectedProductId();
        if (pId == -1) return;
        String pName = getSelectedProductName();

        JTextField txtP25 = new JTextField("0");
        JTextField txtP20 = new JTextField("0");
        JTextField txtP10 = new JTextField("0");
        JTextField txtP5 = new JTextField("0");
        JTextField txtNote = new JTextField("Routine packaging for weekend retail rush.");

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Rice Variety:"));
        form.add(new JLabel("<b>" + pName + "</b>"));
        form.add(new JLabel("25kg Bags to Repack:"));
        form.add(txtP25);
        form.add(new JLabel("20kg Bags to Repack:"));
        form.add(txtP20);
        form.add(new JLabel("10kg Bags to Repack:"));
        form.add(txtP10);
        form.add(new JLabel("5kg Bags to Repack:"));
        form.add(txtP5);
        form.add(new JLabel("Reason / Note:"));
        form.add(txtNote);

        int res = JOptionPane.showConfirmDialog(this, form, "Repack Loose Rice into Bags - " + pName, JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int p25 = Integer.parseInt(txtP25.getText().trim());
                int p20 = Integer.parseInt(txtP20.getText().trim());
                int p10 = Integer.parseInt(txtP10.getText().trim());
                int p5 = Integer.parseInt(txtP5.getText().trim());

                boolean ok = inventoryDAO.repack(pId, p25, p20, p10, p5, txtNote.getText().trim(), AuthSession.getFullName());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Repacking completed successfully!", "Repack Complete", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Repack Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCorrectionDialog() {
        int pId = getSelectedProductId();
        if (pId == -1) return;
        String pName = getSelectedProductName();

        JTextField txtSacks = new JTextField("0");
        JTextField txtKg = new JTextField("0.0");
        JTextField txtNote = new JTextField("Physical inventory count reconciliation.");

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Rice Variety:"));
        form.add(new JLabel("<b>" + pName + "</b>"));
        form.add(new JLabel("Whole Sacks Adjustment (+/-):"));
        form.add(txtSacks);
        form.add(new JLabel("Loose Kg Adjustment (+/-):"));
        form.add(txtKg);
        form.add(new JLabel("Auditor Reason / Note:"));
        form.add(txtNote);

        int res = JOptionPane.showConfirmDialog(this, form, "Physical Count Correction - " + pName, JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int sacks = Integer.parseInt(txtSacks.getText().trim());
                double kg = Double.parseDouble(txtKg.getText().trim());

                boolean ok = inventoryDAO.recordAdjustment(pId, "correction", sacks, 0, 0, 0, 0, kg, txtNote.getText().trim(), AuthSession.getFullName());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Inventory correction recorded.", "Correction Complete", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Correction Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showSpoilageDialog() {
        int pId = getSelectedProductId();
        if (pId == -1) return;
        String pName = getSelectedProductName();

        JTextField txtSacks = new JTextField("0");
        JTextField txtKg = new JTextField("0.0");
        JTextField txtNote = new JTextField("Water damage / weevil contamination write-off.");

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Rice Variety:"));
        form.add(new JLabel("<b>" + pName + "</b>"));
        form.add(new JLabel("Damaged Sacks to Deduct:"));
        form.add(txtSacks);
        form.add(new JLabel("Damaged Loose Kg to Deduct:"));
        form.add(txtKg);
        form.add(new JLabel("Damage / Spoilage Reason:"));
        form.add(txtNote);

        int res = JOptionPane.showConfirmDialog(this, form, "Log Spoilage / Damage - " + pName, JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int sacks = Integer.parseInt(txtSacks.getText().trim());
                double kg = Double.parseDouble(txtKg.getText().trim());

                boolean ok = inventoryDAO.recordAdjustment(pId, "spoilage", sacks, 0, 0, 0, 0, kg, txtNote.getText().trim(), AuthSession.getFullName());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Spoilage written off and logged in system audit trail.", "Spoilage Logged", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Spoilage Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
