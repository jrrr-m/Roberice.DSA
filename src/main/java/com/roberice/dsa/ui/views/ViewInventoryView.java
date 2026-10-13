package com.roberice.dsa.ui.views;

import com.roberice.dsa.model.InventoryBatch;
import com.roberice.dsa.model.Product;
import com.roberice.dsa.service.InventoryService;
import com.roberice.dsa.service.ProductService;
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

public class ViewInventoryView extends JPanel {

    private final List<Product> productList = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable inventoryTable;

    private JTextField searchField;
    private JComboBox<String> originCombo;
    private JCheckBox lowStockCheck;

    public ViewInventoryView() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_DARK);

        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Top Filter & Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        searchField = new JTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Filter inventory products...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterData(); }
            @Override public void removeUpdate(DocumentEvent e) { filterData(); }
            @Override public void changedUpdate(DocumentEvent e) { filterData(); }
        });

        originCombo = new JComboBox<>(new String[]{"All Origins", "Local", "Imported"});
        originCombo.addActionListener(e -> filterData());

        lowStockCheck = new JCheckBox("⚠️ Low Stock Only");
        lowStockCheck.setForeground(UIUtils.ACCENT_WARNING);
        lowStockCheck.setOpaque(false);
        lowStockCheck.addActionListener(e -> filterData());

        leftFilters.add(searchField);
        leftFilters.add(originCombo);
        leftFilters.add(lowStockCheck);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton openSackBtn = UIUtils.createWarningButton("🌾 Open Sack to Loose", e -> openSackDialog());
        JButton batchBtn = UIUtils.createSecondaryButton("🔍 View FIFO Batches", e -> viewBatchDialog());
        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh", e -> loadData());

        rightActions.add(openSackBtn);
        rightActions.add(batchBtn);
        rightActions.add(refreshBtn);

        topBar.add(leftFilters, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        content.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {
                "ID", "Product Name", "Variety", "Origin", "Sacks",
                "25kg", "20kg", "10kg", "5kg", "Loose (kg)", "Total (kg)", "Reorder (kg)", "Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        inventoryTable = UIUtils.createStyledTable(tableModel);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(inventoryTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                productList.clear();
                productList.addAll(ProductService.getProducts());
                filterData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void filterData() {
        String query = searchField.getText().trim().toLowerCase();
        String originFilter = (String) originCombo.getSelectedItem();
        boolean lowStockOnly = lowStockCheck.isSelected();

        tableModel.setRowCount(0);

        for (Product p : productList) {
            boolean matchOrigin = originFilter == null || "All Origins".equals(originFilter) ||
                    (p.getOrigin() != null && p.getOrigin().equalsIgnoreCase(originFilter));

            boolean matchSearch = query.isEmpty() ||
                    p.getProductName().toLowerCase().contains(query) ||
                    p.getRiceType().toLowerCase().contains(query) ||
                    p.getCategory().toLowerCase().contains(query);

            double totalKg = (p.getStockSacks() * p.getKgPerSack()) +
                    (p.getPackaged25kg() * 25.0) +
                    (p.getPackaged20kg() * 20.0) +
                    (p.getPackaged10kg() * 10.0) +
                    (p.getPackaged5kg() * 5.0) +
                    p.getStockKg();

            boolean isLow = totalKg <= p.getReorderLevelKg();
            if (lowStockOnly && !isLow) continue;

            if (matchOrigin && matchSearch) {
                String status = totalKg <= 0 ? "Out of Stock" : (isLow ? "Low Stock" : "In Stock");
                tableModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getProductName(),
                        p.getRiceType(),
                        p.getOrigin(),
                        p.getStockSacks(),
                        p.getPackaged25kg(),
                        p.getPackaged20kg(),
                        p.getPackaged10kg(),
                        p.getPackaged5kg(),
                        String.format("%.2f", p.getStockKg()),
                        String.format("%.2f", totalKg),
                        String.format("%.0f", p.getReorderLevelKg()),
                        status
                });
            }
        }
    }

    private void openSackDialog() {
        int selectedRow = inventoryTable.getSelectedRow();
        Product selectedProduct = null;
        if (selectedRow >= 0) {
            int pId = (int) tableModel.getValueAt(selectedRow, 0);
            for (Product p : productList) {
                if (p.getProductId() == pId) {
                    selectedProduct = p;
                    break;
                }
            }
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Open Whole Sack into Loose Bin", true);
        dlg.setSize(440, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnl = new JPanel(new GridLayout(4, 2, 10, 14));
        pnl.setBackground(UIUtils.BG_CARD);
        pnl.setBorder(new EmptyBorder(24, 28, 20, 28));

        JComboBox<Product> prodCombo = new JComboBox<>(productList.toArray(new Product[0]));
        if (selectedProduct != null) prodCombo.setSelectedItem(selectedProduct);

        JSpinner sacksSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));

        JLabel infoLbl = new JLabel("Each sack adds 50kg to loose stock");
        infoLbl.setFont(UIUtils.FONT_SMALL);
        infoLbl.setForeground(UIUtils.ACCENT_INFO);

        pnl.add(new JLabel("Select Rice Product:"));
        pnl.add(prodCombo);
        pnl.add(new JLabel("Number of Sacks to Open:"));
        pnl.add(sacksSpinner);
        pnl.add(new JLabel("Conversion Impact:"));
        pnl.add(infoLbl);

        JButton confirmBtn = UIUtils.createPrimaryButton("Confirm & Open Sack", e -> {
            Product p = (Product) prodCombo.getSelectedItem();
            if (p == null) return;
            int sacks = (int) sacksSpinner.getValue();
            try {
                String user = Session.getCurrentUser() != null ? Session.getCurrentUser().getFullName() : "Admin";
                InventoryService.openSack(p.getProductId(), null, sacks, user);
                UIUtils.showSuccess(dlg, "Successfully opened " + sacks + " sack(s) (" + (sacks * 50) + "kg) for " + p.getProductName() + ".");
                dlg.dispose();
                loadData();
            } catch (Exception ex) {
                UIUtils.showError(dlg, "Failed to open sack: " + ex.getMessage());
            }
        });

        dlg.add(pnl, BorderLayout.CENTER);
        dlg.add(confirmBtn, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void viewBatchDialog() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Please select a product from the table first.");
            return;
        }

        int pId = (int) tableModel.getValueAt(selectedRow, 0);
        String prodName = (String) tableModel.getValueAt(selectedRow, 1);

        List<InventoryBatch> batches = InventoryService.getBatchesForProduct(pId);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "FIFO Batch Explorer - " + prodName, true);
        dlg.setSize(750, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        String[] bCols = {"Batch Code", "Received Date", "Days Old", "Sacks", "25kg", "20kg", "10kg", "5kg", "Loose (kg)", "Total (kg)", "Status"};
        DefaultTableModel bModel = new DefaultTableModel(bCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (InventoryBatch b : batches) {
            bModel.addRow(new Object[]{
                    b.getBatchCode(),
                    b.getReceivedDate(),
                    b.getDaysOld() + " days",
                    b.getStockSacks(),
                    b.getPackaged25kg(),
                    b.getPackaged20kg(),
                    b.getPackaged10kg(),
                    b.getPackaged5kg(),
                    String.format("%.2f", b.getStockKg()),
                    String.format("%.2f", b.getTotalKg()),
                    b.getStatus()
            });
        }

        JTable bTable = UIUtils.createStyledTable(bModel);
        dlg.add(new JScrollPane(bTable), BorderLayout.CENTER);

        JButton closeBtn = UIUtils.createSecondaryButton("Close", e -> dlg.dispose());
        dlg.add(closeBtn, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
