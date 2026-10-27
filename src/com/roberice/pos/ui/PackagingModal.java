package com.roberice.pos.ui;

import com.roberice.pos.db.InventoryDAO;
import com.roberice.pos.model.InventoryBatch;
import com.roberice.pos.model.Product;
import com.roberice.pos.model.TransactionItem;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Packaging & Quantity Selection Modal (Section 1.3 in User Manual).
 * Allows selection of 50kg Whole Sack, 25kg Half, 20kg, 10kg, 5kg, or Tinatakal,
 * FIFO Lot Batch selection, and quantity stepper.
 */
public class PackagingModal extends JDialog {
    private final Product product;
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    private String selectedUnit = "sack"; // default
    private double unitPrice = 0;
    private double quantity = 1.0;

    private final JComboBox<InventoryBatch> cmbBatches = new JComboBox<>();
    private final JTextField txtQuantity = new JTextField("1", 4);
    private final JLabel lblSubtotalPreview = new JLabel("₱ 0.00");
    private final ButtonGroup packGroup = new ButtonGroup();

    private TransactionItem resultItem = null;

    public PackagingModal(Frame parent, Product product) {
        super(parent, "Select Packaging & Quantity - " + product.getProductName(), true);
        this.product = product;

        setSize(560, 520);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JLabel title = new JLabel(product.getProductName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel originBadge = UIUtils.createBadge(product.getOrigin() + " • " + product.getRiceType(),
                new Color(241, 245, 249), UIUtils.COLOR_TEXT_MUTED);

        JPanel topHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topHeader.setBackground(Color.WHITE);
        topHeader.add(title);
        topHeader.add(originBadge);

        // FIFO Batch Lot Selector
        JLabel lblBatchTag = new JLabel("Rice Batch / Lot (FIFO Rotation):");
        lblBatchTag.setFont(UIUtils.FONT_BOLD);
        loadBatches();
        cmbBatches.setMaximumSize(new Dimension(500, 32));

        // Packaging Options Grid
        JLabel lblPackTag = new JLabel("Select Packaging:");
        lblPackTag.setFont(UIUtils.FONT_BOLD);

        JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));
        grid.setBackground(Color.WHITE);
        grid.setMaximumSize(new Dimension(500, 140));

        grid.add(createPackOption("50kg Whole Sack", "sack", product.getPriceSack()));
        grid.add(createPackOption("25kg Half Sack", "25kg", product.getPrice25kg()));
        grid.add(createPackOption("20kg Bag", "20kg", product.getPrice20kg()));
        grid.add(createPackOption("10kg Bag", "10kg", product.getPrice10kg()));
        grid.add(createPackOption("5kg Bag", "5kg", product.getPrice5kg()));
        grid.add(createPackOption("Tinatakal (Per Kg)", "tinatakal", product.getPriceKg()));

        // Quantity Stepper
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        qtyPanel.setBackground(Color.WHITE);

        JButton btnMinus = UIUtils.createSecondaryButton("−");
        btnMinus.setPreferredSize(new Dimension(38, 34));
        btnMinus.addActionListener(e -> {
            if (quantity > 1.0) {
                quantity -= 1.0;
                txtQuantity.setText(formatQty(quantity));
                updateSubtotal();
            }
        });

        txtQuantity.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtQuantity.setHorizontalAlignment(SwingConstants.CENTER);
        txtQuantity.setPreferredSize(new Dimension(65, 34));
        txtQuantity.addActionListener(e -> onQuantityInput());

        JButton btnPlus = UIUtils.createSecondaryButton("+");
        btnPlus.setPreferredSize(new Dimension(38, 34));
        btnPlus.addActionListener(e -> {
            quantity += 1.0;
            txtQuantity.setText(formatQty(quantity));
            updateSubtotal();
        });

        qtyPanel.add(new JLabel("Quantity: "));
        qtyPanel.add(btnMinus);
        qtyPanel.add(txtQuantity);
        qtyPanel.add(btnPlus);

        // Subtotal Preview
        JPanel subtotalBar = new JPanel(new BorderLayout());
        subtotalBar.setBackground(new Color(248, 250, 252));
        subtotalBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));
        subtotalBar.setMaximumSize(new Dimension(500, 50));

        JLabel lblSubTag = new JLabel("Estimated Item Total:");
        lblSubTag.setFont(UIUtils.FONT_REGULAR);
        lblSubtotalPreview.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSubtotalPreview.setForeground(UIUtils.COLOR_PRIMARY);

        subtotalBar.add(lblSubTag, BorderLayout.WEST);
        subtotalBar.add(lblSubtotalPreview, BorderLayout.EAST);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Color.WHITE);
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnAdd = UIUtils.createPrimaryButton("Add to Order");
        btnAdd.addActionListener(e -> addToOrder());
        actions.add(btnCancel);
        actions.add(btnAdd);

        main.add(topHeader);
        main.add(Box.createVerticalStrut(14));
        main.add(lblBatchTag);
        main.add(Box.createVerticalStrut(4));
        main.add(cmbBatches);
        main.add(Box.createVerticalStrut(14));
        main.add(lblPackTag);
        main.add(Box.createVerticalStrut(6));
        main.add(grid);
        main.add(Box.createVerticalStrut(14));
        main.add(qtyPanel);
        main.add(Box.createVerticalStrut(14));
        main.add(subtotalBar);
        main.add(Box.createVerticalStrut(14));
        main.add(actions);

        setContentPane(main);
        updateSubtotal();
    }

    private void loadBatches() {
        try {
            List<InventoryBatch> batches = inventoryDAO.getBatchesForProduct(product.getProductId(), true);
            for (InventoryBatch b : batches) {
                cmbBatches.addItem(b);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JToggleButton createPackOption(String title, String unitKey, double price) {
        JToggleButton btn = new JToggleButton("<html><center><b>" + title + "</b><br><font color='#166534'><b>" + UIUtils.formatCurrency(price) + "</b></font></center></html>");
        btn.setFont(UIUtils.FONT_SMALL);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (unitKey.equals("sack")) {
            btn.setSelected(true);
            this.selectedUnit = "sack";
            this.unitPrice = price;
        }

        btn.addActionListener(e -> {
            this.selectedUnit = unitKey;
            this.unitPrice = price;
            updateSubtotal();
        });

        packGroup.add(btn);
        return btn;
    }

    private void onQuantityInput() {
        try {
            double q = Double.parseDouble(txtQuantity.getText().trim());
            if (q > 0) {
                this.quantity = q;
            } else {
                txtQuantity.setText(formatQty(quantity));
            }
        } catch (NumberFormatException ex) {
            txtQuantity.setText(formatQty(quantity));
        }
        updateSubtotal();
    }

    private void updateSubtotal() {
        double sub = unitPrice * quantity;
        lblSubtotalPreview.setText(UIUtils.formatCurrency(sub));
    }

    private void addToOrder() {
        onQuantityInput();
        double sub = unitPrice * quantity;
        resultItem = new TransactionItem(product.getProductId(), product.getProductName(), selectedUnit, quantity, unitPrice, sub);

        InventoryBatch b = (InventoryBatch) cmbBatches.getSelectedItem();
        if (b != null) {
            resultItem.setBatchCode(b.getBatchCode());
        }

        dispose();
    }

    public TransactionItem getResultItem() {
        return resultItem;
    }

    private String formatQty(double q) {
        return (q == (int) q) ? String.valueOf((int) q) : String.format("%.2f", q);
    }
}
