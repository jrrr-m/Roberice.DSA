package com.roberice.pos.ui;

import com.roberice.pos.db.InventoryDAO;
import com.roberice.pos.model.Inventory;
import com.roberice.pos.model.InventoryBatch;
import com.roberice.pos.model.Product;
import com.roberice.pos.model.TransactionItem;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Packaging & Quantity Selection Modal matching the design in the second screenshot.
 * Allows choosing rice packaging sizes (Whole Sack, Half Sack, 20kg, 10kg, 5kg, Specified Kilos),
 * FIFO lot selection, interactive selection borders, quantity stepper, and estimated total preview.
 */
public class PackagingModal extends JDialog {
    private final Product product;
    private final Inventory inventory;
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    private String selectedUnit = "sack"; // "sack", "25kg", "20kg", "10kg", "5kg", "tinatakal"
    private double unitPrice = 0;
    private double quantity = 1.0;

    private final JComboBox<InventoryBatch> cmbBatches = new JComboBox<>();
    private final JTextField txtQuantity = new JTextField("1", 4);
    private final JLabel lblSubtotalPreview = new JLabel("₱ 0.00");
    private final JLabel lblSelectedDesc = new JLabel("Whole Sack (50kg) x 1");
    private final JLabel lblAvailableStockInfo = new JLabel("Available in inventory: 0");

    private final List<PackOptionCard> optionCards = new ArrayList<>();
    private TransactionItem resultItem = null;

    // Design Colors
    private static final Color COLOR_MODAL_BG = Color.WHITE;
    private static final Color COLOR_SELECTED_BORDER = new Color(27, 77, 50);
    private static final Color COLOR_SELECTED_BG = new Color(250, 252, 250);
    private static final Color COLOR_CARD_BORDER = new Color(225, 230, 225);
    private static final Color COLOR_GREEN_BTN = new Color(27, 77, 50);
    private static final Color COLOR_GREEN_BTN_HOVER = new Color(21, 62, 40);

    public PackagingModal(Frame parent, Product product, Inventory inventory) {
        super(parent, "Select Packaging", true);
        this.product = product;
        this.inventory = inventory;

        setSize(580, 560);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(COLOR_MODAL_BG);
        main.setBorder(new EmptyBorder(18, 24, 18, 24));

        // 1. Header (Title + Badges + Close)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel lblTitle = new JLabel(product.getProductName());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(new Color(15, 30, 24));

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        badgeRow.setOpaque(false);

        JLabel badgeOrigin = createBadge(product.getOrigin(), new Color(224, 242, 254), new Color(14, 116, 144));
        JLabel lblType = new JLabel(product.getRiceType() != null ? product.getRiceType() : "General");
        lblType.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblType.setForeground(new Color(110, 125, 115));

        boolean inStock = inventory != null && (inventory.getStockSacks() > 0 || inventory.getStockKg() > 0);
        JLabel badgeAvail = createBadge(inStock ? "• Available" : "• Low Stock",
                inStock ? new Color(220, 252, 231) : new Color(254, 242, 242),
                inStock ? new Color(22, 101, 52) : new Color(185, 28, 28));

        badgeRow.add(badgeOrigin);
        badgeRow.add(lblType);
        badgeRow.add(badgeAvail);

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(badgeRow);

        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.setForeground(new Color(150, 160, 155));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(btnClose, BorderLayout.EAST);

        // 2. FIFO Batch Lot Selector
        JLabel lblBatchTag = new JLabel("RICE BATCH / LOT (FIFO SELECTION):");
        lblBatchTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBatchTag.setForeground(new Color(120, 130, 125));
        lblBatchTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        loadBatches();
        cmbBatches.setMaximumSize(new Dimension(530, 32));
        cmbBatches.setPreferredSize(new Dimension(530, 32));
        cmbBatches.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbBatches.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        // 3. Packaging Grid Section
        JLabel lblPackTag = new JLabel("CHOOSE SACK PACKAGING OR SPECIFIED LOOSE KILOS:");
        lblPackTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPackTag.setForeground(new Color(120, 130, 125));
        lblPackTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(3, 2, 10, 10));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(530, 190));
        grid.setPreferredSize(new Dimension(530, 190));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        int sacks = inventory != null ? inventory.getStockSacks() : 0;
        int p25 = inventory != null ? inventory.getPackaged25kg() : 0;
        int p20 = inventory != null ? inventory.getPackaged20kg() : 0;
        int p10 = inventory != null ? inventory.getPackaged10kg() : 0;
        int p5 = inventory != null ? inventory.getPackaged5kg() : 0;
        double loose = inventory != null ? inventory.getStockKg() : 0.0;

        PackOptionCard cSack = new PackOptionCard("Whole Sack (50 Kilos)", "sack", product.getPriceSack(), "Stock: " + sacks + " sacks");
        PackOptionCard c25 = new PackOptionCard("Half Sack (25 Kilos)", "25kg", product.getPrice25kg(), "Stock: " + p25 + " sacks");
        PackOptionCard c20 = new PackOptionCard("20 Kilos Sack", "20kg", product.getPrice20kg(), "Stock: " + p20 + " sacks");
        PackOptionCard c10 = new PackOptionCard("10 Kilos Sack", "10kg", product.getPrice10kg(), "Stock: " + p10 + " sacks");
        PackOptionCard c5 = new PackOptionCard("5 Kilos Sack", "5kg", product.getPrice5kg(), "Stock: " + p5 + " sacks");
        PackOptionCard cLoose = new PackOptionCard("Specified Kilos (Loose Rice)", "tinatakal", product.getPriceKg(), String.format("Max available: %.1f kg (Loose: %.1f kg)", (sacks * 50.0 + loose), loose));

        optionCards.add(cSack);
        optionCards.add(c25);
        optionCards.add(c20);
        optionCards.add(c10);
        optionCards.add(c5);
        optionCards.add(cLoose);

        for (PackOptionCard card : optionCards) {
            grid.add(card);
        }

        // Select first by default
        selectOption(cSack);

        // 4. Quantity Stepper
        JPanel qtyRow = new JPanel(new BorderLayout(8, 0));
        qtyRow.setOpaque(false);
        qtyRow.setMaximumSize(new Dimension(530, 40));
        qtyRow.setPreferredSize(new Dimension(530, 40));
        qtyRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel qtyLeft = new JPanel(new GridLayout(2, 1, 0, 1));
        qtyLeft.setOpaque(false);
        JLabel lblQtyTitle = new JLabel("Quantity");
        lblQtyTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblQtyTitle.setForeground(new Color(20, 30, 25));

        lblAvailableStockInfo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblAvailableStockInfo.setForeground(new Color(120, 130, 125));

        qtyLeft.add(lblQtyTitle);
        qtyLeft.add(lblAvailableStockInfo);

        JPanel stepper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        stepper.setOpaque(false);

        JButton btnMinus = createStepperButton("−");
        btnMinus.addActionListener(e -> {
            if (quantity > 1.0) {
                quantity -= 1.0;
                txtQuantity.setText(formatQty(quantity));
                updateSubtotal();
            }
        });

        txtQuantity.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtQuantity.setHorizontalAlignment(SwingConstants.CENTER);
        txtQuantity.setPreferredSize(new Dimension(48, 28));
        txtQuantity.addActionListener(e -> onQuantityInput());

        JButton btnPlus = createStepperButton("+");
        btnPlus.addActionListener(e -> {
            quantity += 1.0;
            txtQuantity.setText(formatQty(quantity));
            updateSubtotal();
        });

        stepper.add(btnMinus);
        stepper.add(txtQuantity);
        stepper.add(btnPlus);

        qtyRow.add(qtyLeft, BorderLayout.WEST);
        qtyRow.add(stepper, BorderLayout.EAST);

        // 5. Total Bar
        JPanel totalBar = new JPanel(new BorderLayout());
        totalBar.setBackground(new Color(254, 252, 244)); // Warm ivory bar #FEFCF4
        totalBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(240, 235, 222), 1),
                new EmptyBorder(8, 14, 8, 14)
        ));
        totalBar.setMaximumSize(new Dimension(530, 48));
        totalBar.setPreferredSize(new Dimension(530, 48));
        totalBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel totLeft = new JPanel(new GridLayout(2, 1, 0, 1));
        totLeft.setOpaque(false);
        JLabel lblTotTag = new JLabel("ITEM TOTAL");
        lblTotTag.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblTotTag.setForeground(new Color(130, 140, 135));

        lblSelectedDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSelectedDesc.setForeground(new Color(40, 50, 45));

        totLeft.add(lblTotTag);
        totLeft.add(lblSelectedDesc);

        lblSubtotalPreview.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblSubtotalPreview.setForeground(COLOR_GREEN_BTN);

        totalBar.add(totLeft, BorderLayout.WEST);
        totalBar.add(lblSubtotalPreview, BorderLayout.EAST);

        // 6. Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setMaximumSize(new Dimension(530, 40));
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setForeground(new Color(80, 95, 85));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 220), 1),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());

        JButton btnAdd = new JButton("🛒 Add to Order") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(COLOR_GREEN_BTN_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(COLOR_GREEN_BTN_HOVER);
                } else {
                    g2.setColor(COLOR_GREEN_BTN);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setFocusPainted(false);
        btnAdd.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> addToOrder());

        actionRow.add(btnCancel);
        actionRow.add(btnAdd);

        // Assemble
        main.add(headerPanel);
        main.add(Box.createVerticalStrut(10));
        main.add(lblBatchTag);
        main.add(Box.createVerticalStrut(4));
        main.add(cmbBatches);
        main.add(Box.createVerticalStrut(12));
        main.add(lblPackTag);
        main.add(Box.createVerticalStrut(6));
        main.add(grid);
        main.add(Box.createVerticalStrut(10));
        main.add(qtyRow);
        main.add(Box.createVerticalStrut(10));
        main.add(totalBar);
        main.add(Box.createVerticalStrut(12));
        main.add(actionRow);

        setContentPane(main);
        updateSubtotal();
    }

    private void selectOption(PackOptionCard selectedCard) {
        for (PackOptionCard card : optionCards) {
            card.setSelected(card == selectedCard);
        }
        this.selectedUnit = selectedCard.unitKey;
        this.unitPrice = selectedCard.price;

        int avail = 0;
        if (inventory != null) {
            switch (selectedUnit) {
                case "sack": avail = inventory.getStockSacks(); break;
                case "25kg": avail = inventory.getPackaged25kg(); break;
                case "20kg": avail = inventory.getPackaged20kg(); break;
                case "10kg": avail = inventory.getPackaged10kg(); break;
                case "5kg": avail = inventory.getPackaged5kg(); break;
                case "tinatakal": avail = (int) inventory.getStockKg(); break;
            }
        }
        lblAvailableStockInfo.setText("Available in inventory: " + avail + (selectedUnit.equals("tinatakal") ? " kg" : " sacks"));
        updateSubtotal();
    }

    private class PackOptionCard extends JPanel {
        private final String title;
        private final String unitKey;
        private final double price;
        private final String stockText;
        private boolean selected = false;

        public PackOptionCard(String title, String unitKey, double price, String stockText) {
            this.title = title;
            this.unitKey = unitKey;
            this.price = price;
            this.stockText = stockText;

            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectOption(PackOptionCard.this);
                }
            });
        }

        public void setSelected(boolean sel) {
            this.selected = sel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background
            g2.setColor(selected ? COLOR_SELECTED_BG : Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);

            // Border
            if (selected) {
                g2.setColor(COLOR_SELECTED_BORDER);
                g2.setStroke(new BasicStroke(2.0f));
            } else {
                g2.setColor(COLOR_CARD_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.drawRoundRect(1, 1, w - 3, h - 3, 8, 8);

            // Draw text directly for perfect pixel placement
            g2.setColor(new Color(20, 30, 25));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(title, 10, 18);

            g2.setColor(COLOR_GREEN_BTN);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(UIUtils.formatCurrency(price) + (unitKey.equals("tinatakal") ? " / kg" : ""), 10, 34);

            g2.setColor(new Color(130, 140, 135));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.drawString(stockText, 10, 48);

            g2.dispose();
        }
    }

    private JButton createStepperButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(40, 50, 45));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 220), 1),
                new EmptyBorder(2, 4, 2, 4)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(" " + text + " ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(1, 6, 1, 6));
        return badge;
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

    private void onQuantityInput() {
        try {
            double q = Double.parseDouble(txtQuantity.getText().trim());
            if (q > 0) {
                this.quantity = q;
            } else {
                this.quantity = 1.0;
                txtQuantity.setText("1");
            }
        } catch (NumberFormatException e) {
            this.quantity = 1.0;
            txtQuantity.setText("1");
        }
        updateSubtotal();
    }

    private void updateSubtotal() {
        double subtotal = unitPrice * quantity;
        lblSubtotalPreview.setText(UIUtils.formatCurrency(subtotal));

        String unitName = switch (selectedUnit) {
            case "sack" -> "Whole Sack (50kg)";
            case "25kg" -> "Half Sack (25kg)";
            case "20kg" -> "20kg Bag";
            case "10kg" -> "10kg Bag";
            case "5kg" -> "5kg Bag";
            case "tinatakal" -> "Loose Rice (kg)";
            default -> selectedUnit;
        };
        lblSelectedDesc.setText(unitName + " x " + formatQty(quantity));
    }

    private String formatQty(double q) {
        return (q == (long) q) ? String.format("%d", (long) q) : String.format("%.2f", q);
    }

    private void addToOrder() {
        onQuantityInput();
        InventoryBatch selectedBatch = (InventoryBatch) cmbBatches.getSelectedItem();
        String batchCode = selectedBatch != null ? selectedBatch.getBatchCode() : "FIFO";

        TransactionItem item = new TransactionItem();
        item.setProductId(product.getProductId());
        item.setProductName(product.getProductName());
        item.setBatchCode(batchCode);
        item.setUnit(selectedUnit);
        item.setUnitPrice(unitPrice);
        item.setQuantity(quantity);
        item.setSubtotal(unitPrice * quantity);

        this.resultItem = item;
        dispose();
    }

    public TransactionItem getResultItem() {
        return resultItem;
    }
}
