package com.roberice.dsa.ui.views;

import com.roberice.dsa.model.Product;
import com.roberice.dsa.service.ProductService;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProductsView extends JPanel {

    private final List<Product> products = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable productTable;

    private JTextField searchField;
    private JComboBox<String> originCombo;

    public ProductsView() {
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
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Search rice varieties...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterData(); }
            @Override public void removeUpdate(DocumentEvent e) { filterData(); }
            @Override public void changedUpdate(DocumentEvent e) { filterData(); }
        });

        originCombo = new JComboBox<>(new String[]{"All Origins", "Local", "Imported"});
        originCombo.addActionListener(e -> filterData());

        leftFilters.add(searchField);
        leftFilters.add(originCombo);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton addBtn = UIUtils.createPrimaryButton("+ Add New Product", e -> showProductDialog(null));
        JButton editBtn = UIUtils.createSecondaryButton("✏️ Edit Selected", e -> editSelectedProduct());
        JButton deleteBtn = UIUtils.createDangerButton("🗑️ Deactivate", e -> deactivateSelectedProduct());
        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh", e -> loadData());

        rightActions.add(addBtn);
        rightActions.add(editBtn);
        rightActions.add(deleteBtn);
        rightActions.add(refreshBtn);

        topBar.add(leftFilters, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        content.add(topBar, BorderLayout.NORTH);

        // Product Table
        String[] cols = {
                "ID", "Product Name", "Variety", "Origin",
                "Price 50kg", "Price 25kg", "Price 20kg", "Price 10kg", "Price 5kg", "Price 1kg",
                "Reorder (kg)", "Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        productTable = UIUtils.createStyledTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        productTable.getColumnModel().getColumn(3).setCellRenderer((t, val, isSel, hasFocus, row, col) -> {
            String text = val != null ? String.valueOf(val) : "";
            Color bg = "Imported".equalsIgnoreCase(text) ? UIUtils.WARNING_BG : UIUtils.SUCCESS_BG;
            Color fg = "Imported".equalsIgnoreCase(text) ? UIUtils.WARNING_TEXT : UIUtils.SUCCESS_TEXT;
            return UIUtils.createBadge(text, bg, fg);
        });

        productTable.getColumnModel().getColumn(11).setCellRenderer((t, val, isSel, hasFocus, row, col) -> {
            String text = val != null ? String.valueOf(val) : "";
            Color bg = "Active".equalsIgnoreCase(text) ? UIUtils.SUCCESS_BG : UIUtils.DANGER_BG;
            Color fg = "Active".equalsIgnoreCase(text) ? UIUtils.SUCCESS_TEXT : UIUtils.DANGER_TEXT;
            return UIUtils.createBadge(text, bg, fg);
        });

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                products.clear();
                products.addAll(ProductService.getProducts());
                filterData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void filterData() {
        String query = searchField.getText().trim().toLowerCase();
        String originFilter = (String) originCombo.getSelectedItem();

        tableModel.setRowCount(0);
        for (Product p : products) {
            boolean matchOrigin = originFilter == null || "All Origins".equals(originFilter) ||
                    (p.getOrigin() != null && p.getOrigin().equalsIgnoreCase(originFilter));

            boolean matchSearch = query.isEmpty() ||
                    p.getProductName().toLowerCase().contains(query) ||
                    p.getRiceType().toLowerCase().contains(query);

            if (matchOrigin && matchSearch) {
                tableModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getProductName(),
                        p.getRiceType(),
                        p.getOrigin(),
                        UIUtils.formatCurrency(p.getPriceSack()),
                        UIUtils.formatCurrency(p.getPrice25kg()),
                        UIUtils.formatCurrency(p.getPrice20kg()),
                        UIUtils.formatCurrency(p.getPrice10kg()),
                        UIUtils.formatCurrency(p.getPrice5kg()),
                        UIUtils.formatCurrency(p.getPriceKg()),
                        String.format("%.0f", p.getReorderLevelKg()),
                        p.getProductStatus()
                });
            }
        }
    }

    private Product getSelectedProduct() {
        int r = productTable.getSelectedRow();
        if (r < 0) return null;
        int pId = (int) tableModel.getValueAt(r, 0);
        for (Product p : products) {
            if (p.getProductId() == pId) return p;
        }
        return null;
    }

    private void editSelectedProduct() {
        Product p = getSelectedProduct();
        if (p == null) {
            UIUtils.showError(this, "Please select a product from the table first.");
            return;
        }
        showProductDialog(p);
    }

    private void deactivateSelectedProduct() {
        Product p = getSelectedProduct();
        if (p == null) {
            UIUtils.showError(this, "Please select a product from the table first.");
            return;
        }

        if (UIUtils.showConfirm(this, "Deactivate " + p.getProductName() + "? It will no longer appear in POS sales.", "Confirm Deactivation")) {
            try {
                ProductService.deleteProduct(p.getProductId());
                UIUtils.showSuccess(this, "Product deactivated successfully.");
                loadData();
            } catch (Exception ex) {
                UIUtils.showError(this, "Error deactivating product: " + ex.getMessage());
            }
        }
    }

    private void showProductDialog(Product existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Product Details" : "Add New Rice Product", true);
        dlg.setSize(520, 580);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(12, 2, 8, 8));
        form.setBackground(UIUtils.BG_CARD);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField nameField = new JTextField(isEdit ? existing.getProductName() : "");
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Premium", "Well-Milled", "Regular-Milled", "Special", "Brown", "Glutinous"});
        if (isEdit && existing.getCategory() != null) categoryCombo.setSelectedItem(existing.getCategory());

        JComboBox<String> originField = new JComboBox<>(new String[]{"Local", "Imported"});
        if (isEdit && existing.getOrigin() != null) originField.setSelectedItem(existing.getOrigin());

        JTextField riceTypeField = new JTextField(isEdit ? existing.getRiceType() : "White Rice");
        JTextField sackPrice = new JTextField(isEdit ? String.valueOf(existing.getPriceSack()) : "2600.00");
        JTextField p25Price = new JTextField(isEdit ? String.valueOf(existing.getPrice25kg()) : "1300.00");
        JTextField p20Price = new JTextField(isEdit ? String.valueOf(existing.getPrice20kg()) : "1060.00");
        JTextField p10Price = new JTextField(isEdit ? String.valueOf(existing.getPrice10kg()) : "540.00");
        JTextField p5Price = new JTextField(isEdit ? String.valueOf(existing.getPrice5kg()) : "275.00");
        JTextField kgPrice = new JTextField(isEdit ? String.valueOf(existing.getPriceKg()) : "56.00");
        JTextField reorderField = new JTextField(isEdit ? String.valueOf(existing.getReorderLevelKg()) : "200.0");
        JTextField descField = new JTextField(isEdit ? existing.getDescription() : "");

        form.add(new JLabel("Product Name:")); form.add(nameField);
        form.add(new JLabel("Category:")); form.add(categoryCombo);
        form.add(new JLabel("Origin:")); form.add(originField);
        form.add(new JLabel("Rice Type / Variety:")); form.add(riceTypeField);
        form.add(new JLabel("Price per 50kg Sack (₱):")); form.add(sackPrice);
        form.add(new JLabel("Price per 25kg Pack (₱):")); form.add(p25Price);
        form.add(new JLabel("Price per 20kg Pack (₱):")); form.add(p20Price);
        form.add(new JLabel("Price per 10kg Pack (₱):")); form.add(p10Price);
        form.add(new JLabel("Price per 5kg Pack (₱):")); form.add(p5Price);
        form.add(new JLabel("Price per 1kg Loose (₱):")); form.add(kgPrice);
        form.add(new JLabel("Reorder Alert Threshold (kg):")); form.add(reorderField);
        form.add(new JLabel("Description:")); form.add(descField);

        JButton saveBtn = UIUtils.createPrimaryButton(isEdit ? "Save Changes" : "Create Product", e -> {
            try {
                String name = nameField.getText().trim();
                String cat = (String) categoryCombo.getSelectedItem();
                String orig = (String) originField.getSelectedItem();
                String type = riceTypeField.getText().trim();
                double pSack = Double.parseDouble(sackPrice.getText().trim());
                double p25 = Double.parseDouble(p25Price.getText().trim());
                double p20 = Double.parseDouble(p20Price.getText().trim());
                double p10 = Double.parseDouble(p10Price.getText().trim());
                double p5 = Double.parseDouble(p5Price.getText().trim());
                double pKg = Double.parseDouble(kgPrice.getText().trim());
                double reorder = Double.parseDouble(reorderField.getText().trim());
                String desc = descField.getText().trim();

                if (isEdit) {
                    ProductService.updateProduct(existing.getProductId(), name, cat, orig, type, desc,
                            pSack, p25, pKg, p5, p10, p20, reorder);
                    UIUtils.showSuccess(dlg, "Product updated successfully.");
                } else {
                    ProductService.addProduct(name, cat, orig, type, pSack, p25, pKg, p5, p10, p20,
                            0, 0, 0, 0, 0, 0, null, null, reorder);
                    UIUtils.showSuccess(dlg, "Product created successfully.");
                }
                dlg.dispose();
                loadData();
            } catch (Exception ex) {
                UIUtils.showError(dlg, "Failed to save: " + ex.getMessage());
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(saveBtn, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
