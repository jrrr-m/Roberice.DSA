package com.roberice.pos.ui.panels;

import com.roberice.pos.db.InventoryDAO;
import com.roberice.pos.db.ProductDAO;
import com.roberice.pos.model.Inventory;
import com.roberice.pos.model.Product;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Modern Products & Pricing Panel.
 * Matches the warm cream catalog layout with comprehensive pricing per packaging tier
 * (50kg sack, tinatakal kg, 5kg, 10kg, 20kg), current warehouse stock pills,
 * low-stock alert thresholds, active status badge, and Edit/Delete controls.
 */
public class ProductsPanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    private List<Product> allProducts = new ArrayList<>();
    private List<Inventory> allInventories = new ArrayList<>();

    // Container for dynamic table rows
    private final JPanel rowsContainer = new JPanel();

    // Design Colors
    private static final Color COLOR_APP_BG = new Color(246, 243, 233);
    private static final Color COLOR_CARD_BG = new Color(255, 253, 246);
    private static final Color COLOR_CARD_BORDER = new Color(234, 230, 218);
    private static final Color COLOR_TH_BG = new Color(242, 234, 213);
    private static final Color COLOR_BTN_DELETE = new Color(220, 38, 38);
    private static final Color COLOR_BTN_DELETE_HOVER = new Color(185, 28, 28);

    public ProductsPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(COLOR_APP_BG);
        setBorder(new EmptyBorder(10, 16, 16, 16));

        // Outer rounded card
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

        // Table Panel (Header + Scrollable Dynamic Rows)
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
        add(card, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createTableHeader() {
        JPanel th = new JPanel(new GridBagLayout());
        th.setBackground(COLOR_TH_BG);
        th.setPreferredSize(new Dimension(1000, 36));
        th.setMinimumSize(new Dimension(800, 36));
        th.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 218, 198)));

        double[] weights = {0.14, 0.07, 0.08, 0.08, 0.07, 0.07, 0.07, 0.07, 0.14, 0.08, 0.05, 0.08};
        String[] titles = {
            "RICE VARIETY", "ORIGIN", "RICE TYPE", "SACK PRICE", "TINATAKAL (KG)",
            "5KG PACK", "10KG PACK", "20KG PACK", "CURRENT STOCK", "LOW-STOCK ALERT", "STATUS", "ACTIONS"
        };

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;

        for (int i = 0; i < titles.length; i++) {
            c.gridx = i;
            c.weightx = weights[i];

            JLabel lbl = new JLabel(titles[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lbl.setForeground(new Color(70, 75, 70));
            lbl.setBorder(new EmptyBorder(0, 4, 0, 4));

            if (i >= 3 && i <= 7) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (i == 8) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (i == 9) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (i >= 10) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
            }

            th.add(lbl, c);
        }
        return th;
    }

    public void refreshData() {
        try {
            allProducts = productDAO.getAllProducts(false);
            allInventories = inventoryDAO.getInventorySummary();
            rebuildRows();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void rebuildRows() {
        rowsContainer.removeAll();

        for (Product p : allProducts) {
            Inventory inv = findInventory(p.getProductId());
            JPanel row = createProductRow(p, inv);
            rowsContainer.add(row);

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(240, 235, 222));
            rowsContainer.add(sep);
        }

        rowsContainer.revalidate();
        rowsContainer.repaint();
    }

    private Inventory findInventory(int productId) {
        for (Inventory i : allInventories) {
            if (i.getProductId() == productId) return i;
        }
        return null;
    }

    private JPanel createProductRow(Product p, Inventory inv) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(1000, 68));
        row.setMinimumSize(new Dimension(800, 68));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        double[] weights = {0.14, 0.07, 0.08, 0.08, 0.07, 0.07, 0.07, 0.07, 0.14, 0.08, 0.05, 0.08};

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;

        // 1. Variety Name & Subtitle
        JPanel cell1 = new JPanel(new GridLayout(2, 1, 0, 1));
        cell1.setOpaque(false);
        JLabel lblName = new JLabel(p.getProductName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(new Color(20, 30, 25));

        JLabel lblSub = new JLabel(p.getCategory() != null ? p.getCategory() : "Commercial");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(130, 140, 135));

        cell1.add(lblName);
        cell1.add(lblSub);
        cell1.setBorder(new EmptyBorder(0, 4, 0, 4));

        // 2. Origin Badge
        JPanel cell2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 18));
        cell2.setOpaque(false);
        boolean isLocal = "Local".equalsIgnoreCase(p.getOrigin());
        JLabel badgeOrigin = createBadgePill(p.getOrigin(),
                isLocal ? new Color(240, 253, 244) : new Color(224, 242, 254),
                isLocal ? new Color(22, 101, 52) : new Color(14, 116, 144));
        cell2.add(badgeOrigin);

        // 3. Rice Type
        JLabel cell3 = new JLabel(p.getRiceType() != null ? p.getRiceType() : "-");
        cell3.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cell3.setForeground(new Color(40, 50, 45));
        cell3.setBorder(new EmptyBorder(0, 4, 0, 4));

        // 4. Sack Price
        JLabel cell4 = new JLabel(UIUtils.formatCurrency(p.getPriceSack()));
        cell4.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cell4.setForeground(new Color(20, 30, 25));
        cell4.setHorizontalAlignment(SwingConstants.RIGHT);
        cell4.setBorder(new EmptyBorder(0, 2, 0, 8));

        // 5. Tinatakal (Kg)
        JLabel cell5 = new JLabel(UIUtils.formatCurrency(p.getPriceKg()));
        cell5.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cell5.setForeground(new Color(20, 30, 25));
        cell5.setHorizontalAlignment(SwingConstants.RIGHT);
        cell5.setBorder(new EmptyBorder(0, 2, 0, 8));

        // 6. 5kg Pack
        JLabel cell6 = new JLabel(UIUtils.formatCurrency(p.getPrice5kg()));
        cell6.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cell6.setForeground(new Color(40, 50, 45));
        cell6.setHorizontalAlignment(SwingConstants.RIGHT);
        cell6.setBorder(new EmptyBorder(0, 2, 0, 8));

        // 7. 10kg Pack
        JLabel cell7 = new JLabel(UIUtils.formatCurrency(p.getPrice10kg()));
        cell7.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cell7.setForeground(new Color(40, 50, 45));
        cell7.setHorizontalAlignment(SwingConstants.RIGHT);
        cell7.setBorder(new EmptyBorder(0, 2, 0, 8));

        // 8. 20kg Pack
        JLabel cell8 = new JLabel(UIUtils.formatCurrency(p.getPrice20kg()));
        cell8.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cell8.setForeground(new Color(40, 50, 45));
        cell8.setHorizontalAlignment(SwingConstants.RIGHT);
        cell8.setBorder(new EmptyBorder(0, 2, 0, 8));

        // 9. Current Stock (Sacks, loose kg + 4 pill boxes)
        JPanel cell9 = new JPanel();
        cell9.setLayout(new BoxLayout(cell9, BoxLayout.Y_AXIS));
        cell9.setOpaque(false);

        int sacks = inv != null ? inv.getStockSacks() : 0;
        double looseKg = inv != null ? inv.getStockKg() : 0.0;
        int p25 = inv != null ? inv.getPackaged25kg() : 0;
        int p20 = inv != null ? inv.getPackaged20kg() : 0;
        int p10 = inv != null ? inv.getPackaged10kg() : 0;
        int p5 = inv != null ? inv.getPackaged5kg() : 0;

        JLabel lblStockTop = new JLabel("<html><b>" + sacks + " sacks</b>, " + String.format("%.1f kg", looseKg) + "</html>");
        lblStockTop.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblStockTop.setForeground(new Color(20, 30, 25));
        lblStockTop.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel stockPillsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        stockPillsRow.setOpaque(false);
        stockPillsRow.add(createMiniStockPill("25k: " + p25));
        stockPillsRow.add(createMiniStockPill("20k: " + p20));
        stockPillsRow.add(createMiniStockPill("10k: " + p10));
        stockPillsRow.add(createMiniStockPill("5k: " + p5));
        stockPillsRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        cell9.add(lblStockTop);
        cell9.add(Box.createVerticalStrut(2));
        cell9.add(stockPillsRow);

        // 10. Low-Stock Alert
        double alertKg = inv != null ? inv.getReorderLevelKg() : 250;
        JLabel cell10 = new JLabel((int) alertKg + " kg");
        cell10.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cell10.setForeground(new Color(70, 80, 75));
        cell10.setHorizontalAlignment(SwingConstants.RIGHT);
        cell10.setBorder(new EmptyBorder(0, 2, 0, 8));

        // 11. Status Badge
        JPanel cell11 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
        cell11.setOpaque(false);
        boolean isActive = "Active".equalsIgnoreCase(p.getProductStatus());
        JLabel badgeStatus = createBadgePill(isActive ? "Active" : "Inactive",
                isActive ? new Color(220, 252, 231) : new Color(241, 245, 249),
                isActive ? new Color(22, 101, 52) : new Color(100, 116, 139));
        cell11.add(badgeStatus);

        // 12. Actions: Edit + Delete buttons
        JPanel cell12 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 18));
        cell12.setOpaque(false);

        JButton btnEdit = new JButton("Edit");
        btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnEdit.setForeground(new Color(40, 50, 45));
        btnEdit.setBackground(Color.WHITE);
        btnEdit.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 220), 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        btnEdit.setFocusPainted(false);
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> showAddEditDialog(p));

        JButton btnDelete = new JButton("Delete") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(COLOR_BTN_DELETE_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(COLOR_BTN_DELETE_HOVER);
                } else {
                    g2.setColor(COLOR_BTN_DELETE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setContentAreaFilled(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setFocusPainted(false);
        btnDelete.setPreferredSize(new Dimension(54, 24));
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> triggerDeleteProduct(p));

        cell12.add(btnEdit);
        cell12.add(btnDelete);

        Component[] cells = {cell1, cell2, cell3, cell4, cell5, cell6, cell7, cell8, cell9, cell10, cell11, cell12};
        for (int i = 0; i < cells.length; i++) {
            c.gridx = i;
            c.weightx = weights[i];
            row.add(cells[i], c);
        }

        return row;
    }

    private JLabel createBadgePill(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(1, 6, 1, 6));
        return lbl;
    }

    private JLabel createMiniStockPill(String text) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(248, 246, 240));
        lbl.setForeground(new Color(110, 120, 115));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 226, 218), 1),
                new EmptyBorder(1, 3, 1, 3)
        ));
        return lbl;
    }

    public void triggerAddProduct() {
        showAddEditDialog(null);
    }

    private void triggerDeleteProduct(Product p) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete variety '" + p.getProductName() + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = productDAO.setProductStatus(p.getProductId(), "Inactive");
                if (ok) {
                    JOptionPane.showMessageDialog(this, p.getProductName() + " has been archived/deactivated.", "Product Archived", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting variety: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        JTextField txt5kg = new JTextField(isEdit ? String.valueOf(existing.getPrice5kg()) : "240.00");
        JTextField txt10kg = new JTextField(isEdit ? String.valueOf(existing.getPrice10kg()) : "470.00");
        JTextField txt20kg = new JTextField(isEdit ? String.valueOf(existing.getPrice20kg()) : "930.00");
        JTextField txtDesc = new JTextField(isEdit ? existing.getDescription() : "Standard quality commercial rice.");

        JPanel form = new JPanel(new GridLayout(9, 2, 8, 8));
        form.add(new JLabel("Variety Name:"));
        form.add(txtName);
        form.add(new JLabel("Origin:"));
        form.add(cmbOrigin);
        form.add(new JLabel("Grain Type / Variety:"));
        form.add(txtType);
        form.add(new JLabel("Category / Subtitle:"));
        form.add(txtCategory);
        form.add(new JLabel("50kg Sack Wholesale Price:"));
        form.add(txtSack);
        form.add(new JLabel("Per Kg Retail Rate (Tinatakal):"));
        form.add(txtKg);
        form.add(new JLabel("5kg Pack Price:"));
        form.add(txt5kg);
        form.add(new JLabel("10kg Pack Price:"));
        form.add(txt10kg);
        form.add(new JLabel("20kg Pack Price:"));
        form.add(txt20kg);

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
                double p5 = Double.parseDouble(txt5kg.getText().trim());
                double p10 = Double.parseDouble(txt10kg.getText().trim());
                double p20 = Double.parseDouble(txt20kg.getText().trim());

                Product p = isEdit ? existing : new Product();
                p.setProductName(name);
                p.setOrigin((String) cmbOrigin.getSelectedItem());
                p.setRiceType(txtType.getText().trim());
                p.setCategory(txtCategory.getText().trim());
                p.setPriceSack(pSack);
                p.setPriceKg(pKg);
                p.setPrice25kg(pSack / 2.0);
                p.setPrice20kg(p20);
                p.setPrice10kg(p10);
                p.setPrice5kg(p5);
                p.setDescription(txtDesc.getText().trim());
                p.setProductStatus("Active");

                boolean ok = isEdit ? productDAO.updateProduct(p) : productDAO.addProduct(p);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Variety " + (isEdit ? "updated" : "added") + " successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving variety: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
