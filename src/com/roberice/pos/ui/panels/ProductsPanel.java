package com.roberice.pos.ui.panels;

import com.roberice.pos.db.ProductDAO;
import com.roberice.pos.model.Product;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Products & Pricing Configuration Panel (Section 2.3 in User Manual).
 */
public class ProductsPanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Variety Name", "Category", "Origin", "Rice Type", "50kg Sack", "25kg", "20kg", "10kg", "5kg", "Per Kg", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblProducts = new JTable(tableModel);

    public ProductsPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Rice Varieties Catalog & Pricing Setup");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Color.WHITE);

        JButton btnAdd = UIUtils.createPrimaryButton("+ Add New Rice Variety");
        btnAdd.addActionListener(e -> showAddEditDialog(null));

        JButton btnEdit = UIUtils.createSecondaryButton("Edit Variety");
        btnEdit.addActionListener(e -> editSelected());

        JButton btnToggle = UIUtils.createSecondaryButton("Toggle Active/Inactive");
        btnToggle.addActionListener(e -> toggleStatus());

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnToggle);
        actions.add(btnRefresh);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        UIUtils.styleTable(tblProducts);
        JScrollPane scroll = new JScrollPane(tblProducts);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
        refreshData();
    }

    public void refreshData() {
        try {
            List<Product> list = productDAO.getAllProducts(false);
            tableModel.setRowCount(0);

            for (Product p : list) {
                tableModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getProductName(),
                        p.getCategory(),
                        p.getOrigin(),
                        p.getRiceType(),
                        UIUtils.formatCurrency(p.getPriceSack()),
                        UIUtils.formatCurrency(p.getPrice25kg()),
                        UIUtils.formatCurrency(p.getPrice20kg()),
                        UIUtils.formatCurrency(p.getPrice10kg()),
                        UIUtils.formatCurrency(p.getPrice5kg()),
                        UIUtils.formatCurrency(p.getPriceKg()),
                        p.getProductStatus()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Product getSelectedProduct() {
        int row = tblProducts.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tblProducts.getValueAt(row, 0);
        try {
            return productDAO.getProductById(id);
        } catch (Exception ex) {
            return null;
        }
    }

    private void editSelected() {
        Product p = getSelectedProduct();
        if (p != null) {
            showAddEditDialog(p);
        }
    }

    private void toggleStatus() {
        Product p = getSelectedProduct();
        if (p != null) {
            String newStatus = "Active".equalsIgnoreCase(p.getProductStatus()) ? "Inactive" : "Active";
            try {
                productDAO.setProductStatus(p.getProductId(), newStatus);
                JOptionPane.showMessageDialog(this, p.getProductName() + " is now " + newStatus + ".", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddEditDialog(Product existing) {
        boolean isEdit = (existing != null);
        String title = isEdit ? "Edit Rice Variety - " + existing.getProductName() : "Add New Rice Variety";

        JTextField txtName = new JTextField(isEdit ? existing.getProductName() : "");
        JComboBox<String> cmbOrigin = new JComboBox<>(new String[]{"Local", "Imported"});
        if (isEdit) cmbOrigin.setSelectedItem(existing.getOrigin());

        JTextField txtType = new JTextField(isEdit ? existing.getRiceType() : "White Rice");
        JTextField txtCategory = new JTextField(isEdit ? existing.getCategory() : "Commercial Grade");
        JTextField txtSack = new JTextField(isEdit ? String.valueOf(existing.getPriceSack()) : "2100.00");
        JTextField txtKg = new JTextField(isEdit ? String.valueOf(existing.getPriceKg()) : "48.00");
        JTextField txtDesc = new JTextField(isEdit ? existing.getDescription() : "Standard quality commercial rice.");

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.add(new JLabel("Variety Name:"));
        form.add(txtName);
        form.add(new JLabel("Origin:"));
        form.add(cmbOrigin);
        form.add(new JLabel("Grain Type / Variety:"));
        form.add(txtType);
        form.add(new JLabel("Category:"));
        form.add(txtCategory);
        form.add(new JLabel("Wholesale Sack Price (50kg):"));
        form.add(txtSack);
        form.add(new JLabel("Retail Rate per Kg (Tinatakal):"));
        form.add(txtKg);
        form.add(new JLabel("Description / Notes:"));
        form.add(txtDesc);

        int res = JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Variety name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double pSack = Double.parseDouble(txtSack.getText().trim());
                double pKg = Double.parseDouble(txtKg.getText().trim());

                Product p = isEdit ? existing : new Product();
                p.setProductName(name);
                p.setOrigin((String) cmbOrigin.getSelectedItem());
                p.setRiceType(txtType.getText().trim());
                p.setCategory(txtCategory.getText().trim());
                p.setPriceSack(pSack);
                p.setPriceKg(pKg);
                p.setPrice25kg(pSack / 2.0);
                p.setPrice20kg(pKg * 20.0);
                p.setPrice10kg(pKg * 10.0);
                p.setPrice5kg(pKg * 5.0);
                p.setDescription(txtDesc.getText().trim());
                p.setProductStatus("Active");

                boolean ok = isEdit ? productDAO.updateProduct(p) : productDAO.addProduct(p);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Product " + (isEdit ? "updated" : "added") + " successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error Saving Product", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
