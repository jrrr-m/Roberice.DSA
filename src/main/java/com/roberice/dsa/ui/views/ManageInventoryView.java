package com.roberice.dsa.ui.views;

import com.roberice.dsa.model.AdjustmentLog;
import com.roberice.dsa.model.Product;
import com.roberice.dsa.service.InventoryService;
import com.roberice.dsa.service.ProductService;
import com.roberice.dsa.util.Session;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ManageInventoryView extends JPanel {

    private final List<Product> products = new ArrayList<>();
    private DefaultTableModel logTableModel;

    // Restock form components
    private JComboBox<Product> restockProdCombo;
    private JTextField restockBatchField;
    private JSpinner restockSacksSpinner;
    private JSpinner restock25kgSpinner;
    private JSpinner restock20kgSpinner;
    private JSpinner restock10kgSpinner;
    private JSpinner restock5kgSpinner;
    private JTextField restockLooseField;
    private JTextField restockNoteField;

    // Repack form components
    private JComboBox<Product> repackProdCombo;
    private JComboBox<String> repackSourceCombo;
    private JSpinner repackSourceQty;
    private JComboBox<String> repackTargetCombo;
    private JSpinner repackTargetQty;

    // Spoilage form components
    private JComboBox<Product> spoilProdCombo;
    private JSpinner spoilSacksSpinner;
    private JTextField spoilKgField;
    private JTextField spoilNoteField;

    public ManageInventoryView() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_DARK);

        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIUtils.BG_DARK);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Tabbed Pane for Operations
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIUtils.FONT_BOLD);
        tabs.setBackground(UIUtils.BG_CARD);

        tabs.addTab("📦  Stock In / Restock", createRestockPanel());
        tabs.addTab("🌾  Repackaging", createRepackPanel());
        tabs.addTab("⚠️  Spoilage Write-Off", createSpoilagePanel());

        content.add(tabs);
        content.add(Box.createVerticalStrut(20));

        // Recent Adjustments Table Card
        JPanel logCard = UIUtils.createCard();
        logCard.setLayout(new BorderLayout(0, 10));

        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setOpaque(false);
        JLabel logTitle = new JLabel("Recent Inventory Adjustments Audit Trail");
        logTitle.setFont(UIUtils.FONT_SUBHEADER);
        logTitle.setForeground(UIUtils.TEXT_PRIMARY);

        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh Log", e -> loadLogs());
        logHeader.add(logTitle, BorderLayout.WEST);
        logHeader.add(refreshBtn, BorderLayout.EAST);
        logCard.add(logHeader, BorderLayout.NORTH);

        String[] cols = {"Time", "Type", "Product", "Batch", "Quantity Impact", "Logged By", "Note"};
        logTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable logTable = UIUtils.createStyledTable(logTableModel);
        logTable.getColumnModel().getColumn(1).setCellRenderer((t, val, isSel, hasFocus, row, col) -> {
            String text = val != null ? String.valueOf(val) : "";
            Color bg, fg;
            if (text.contains("RESTOCK")) {
                bg = UIUtils.SUCCESS_BG; fg = UIUtils.SUCCESS_TEXT;
            } else if (text.contains("OPEN")) {
                bg = UIUtils.WARNING_BG; fg = UIUtils.WARNING_TEXT;
            } else if (text.contains("SPOILAGE")) {
                bg = UIUtils.DANGER_BG; fg = UIUtils.DANGER_TEXT;
            } else if (text.contains("REPACK")) {
                bg = new Color(243, 232, 255); fg = new Color(124, 58, 237);
            } else {
                bg = UIUtils.BG_CARD_ALT; fg = UIUtils.TEXT_MUTED;
            }
            return UIUtils.createBadge(text, bg, fg);
        });
        JScrollPane scroll = new JScrollPane(logTable);
        scroll.setPreferredSize(new Dimension(0, 240));
        logCard.add(scroll, BorderLayout.CENTER);

        content.add(logCard);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel createRestockPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(UIUtils.BG_CARD);
        pnl.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        restockProdCombo = new JComboBox<>();
        restockBatchField = new JTextField("BATCH-" + System.currentTimeMillis() % 100000);
        restockSacksSpinner = new JSpinner(new SpinnerNumberModel(20, 0, 1000, 1));
        restock25kgSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        restock20kgSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        restock10kgSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        restock5kgSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        restockLooseField = new JTextField("0.0");
        restockNoteField = new JTextField("Warehouse replenishment from supplier delivery.");

        int r = 0;
        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.2; pnl.add(new JLabel("Target Product:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; gbc.weightx = 0.8; pnl.add(restockProdCombo, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Batch Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(restockBatchField, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("50kg Sacks:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(restockSacksSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("25kg Packages:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(restock25kgSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("20kg Packages:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(restock20kgSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("10kg Packages:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(restock10kgSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("5kg Packages:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(restock5kgSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Loose Grain (kg):"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(restockLooseField, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Adjustment Note:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(restockNoteField, gbc);

        JButton submitBtn = UIUtils.createPrimaryButton("✓ Record Stock In", e -> submitRestock());
        gbc.gridx = 1; gbc.gridy = r; pnl.add(submitBtn, gbc);

        return pnl;
    }

    private JPanel createRepackPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(UIUtils.BG_CARD);
        pnl.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        repackProdCombo = new JComboBox<>();
        repackSourceCombo = new JComboBox<>(new String[]{"Sack", "25kg", "20kg", "10kg", "5kg"});
        repackSourceQty = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        repackTargetCombo = new JComboBox<>(new String[]{"5kg", "10kg", "20kg", "25kg"});
        repackTargetQty = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));

        int r = 0;
        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Select Rice Product:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(repackProdCombo, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Source Packaging Unit:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(repackSourceCombo, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Source Quantity to Deduct:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(repackSourceQty, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Target Retail Packaging:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(repackTargetCombo, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Target Output Quantity:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(repackTargetQty, gbc);

        JButton submitBtn = UIUtils.createPrimaryButton("🌾 Confirm Repacking", e -> submitRepack());
        gbc.gridx = 1; gbc.gridy = r; pnl.add(submitBtn, gbc);

        return pnl;
    }

    private JPanel createSpoilagePanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(UIUtils.BG_CARD);
        pnl.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        spoilProdCombo = new JComboBox<>();
        spoilSacksSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        spoilKgField = new JTextField("0.0");
        spoilNoteField = new JTextField("Water damage / torn bag write-off per store policy.");

        int r = 0;
        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Damaged Product:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(spoilProdCombo, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Spoiled Whole Sacks:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(spoilSacksSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Spoiled Loose Grain (kg):"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(spoilKgField, gbc);

        gbc.gridx = 0; gbc.gridy = r; pnl.add(new JLabel("Damage Reason / Note:"), gbc);
        gbc.gridx = 1; gbc.gridy = r++; pnl.add(spoilNoteField, gbc);

        JButton submitBtn = UIUtils.createDangerButton("⚠️ Record Spoilage Write-Off", e -> submitSpoilage());
        gbc.gridx = 1; gbc.gridy = r; pnl.add(submitBtn, gbc);

        return pnl;
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                products.clear();
                products.addAll(ProductService.getProducts());

                DefaultComboBoxModel<Product> m1 = new DefaultComboBoxModel<>(products.toArray(new Product[0]));
                DefaultComboBoxModel<Product> m2 = new DefaultComboBoxModel<>(products.toArray(new Product[0]));
                DefaultComboBoxModel<Product> m3 = new DefaultComboBoxModel<>(products.toArray(new Product[0]));

                restockProdCombo.setModel(m1);
                repackProdCombo.setModel(m2);
                spoilProdCombo.setModel(m3);

                loadLogs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadLogs() {
        try {
            List<AdjustmentLog> logs = InventoryService.getRecentAdjustments(25);
            logTableModel.setRowCount(0);
            for (AdjustmentLog l : logs) {
                logTableModel.addRow(new Object[]{
                        l.getFormattedDate(),
                        l.getType().toUpperCase(),
                        l.getProductName(),
                        l.getBatchCode(),
                        l.getQtyImpact(),
                        l.getUser(),
                        l.getNote()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitRestock() {
        Product p = (Product) restockProdCombo.getSelectedItem();
        if (p == null) return;

        try {
            int sacks = (int) restockSacksSpinner.getValue();
            int p25 = (int) restock25kgSpinner.getValue();
            int p20 = (int) restock20kgSpinner.getValue();
            int p10 = (int) restock10kgSpinner.getValue();
            int p5 = (int) restock5kgSpinner.getValue();
            double loose = Double.parseDouble(restockLooseField.getText().trim());
            String batch = restockBatchField.getText().trim();
            String note = restockNoteField.getText().trim();
            String user = Session.getCurrentUser() != null ? Session.getCurrentUser().getFullName() : "Admin";

            InventoryService.restock(p.getProductId(), batch, LocalDate.now().toString(), sacks, p25, p20, p10, p5, loose, note, user);
            UIUtils.showSuccess(this, "Stock in successfully recorded for " + p.getProductName() + ".");
            loadData();
        } catch (Exception ex) {
            UIUtils.showError(this, "Restock error: " + ex.getMessage());
        }
    }

    private void submitRepack() {
        Product p = (Product) repackProdCombo.getSelectedItem();
        if (p == null) return;

        try {
            String srcUnit = (String) repackSourceCombo.getSelectedItem();
            int srcQty = (int) repackSourceQty.getValue();
            String tgtUnit = (String) repackTargetCombo.getSelectedItem();
            int tgtQty = (int) repackTargetQty.getValue();
            String user = Session.getCurrentUser() != null ? Session.getCurrentUser().getFullName() : "Admin";

            InventoryService.repack(p.getProductId(), null, srcUnit, srcQty, tgtUnit, tgtQty, "Repacked into retail packages", user);
            UIUtils.showSuccess(this, "Repacking operation completed successfully.");
            loadData();
        } catch (Exception ex) {
            UIUtils.showError(this, "Repack error: " + ex.getMessage());
        }
    }

    private void submitSpoilage() {
        Product p = (Product) spoilProdCombo.getSelectedItem();
        if (p == null) return;

        try {
            int sacks = (int) spoilSacksSpinner.getValue();
            double kg = Double.parseDouble(spoilKgField.getText().trim());
            String note = spoilNoteField.getText().trim();
            String user = Session.getCurrentUser() != null ? Session.getCurrentUser().getFullName() : "Admin";

            InventoryService.recordSpoilage(p.getProductId(), null, sacks, kg, note, user);
            UIUtils.showSuccess(this, "Spoilage write-off successfully recorded.");
            loadData();
        } catch (Exception ex) {
            UIUtils.showError(this, "Spoilage write-off error: " + ex.getMessage());
        }
    }
}
