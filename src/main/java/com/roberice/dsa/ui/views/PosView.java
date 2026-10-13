package com.roberice.dsa.ui.views;

import com.roberice.dsa.model.Product;
import com.roberice.dsa.model.SalesTransaction;
import com.roberice.dsa.model.TransactionItem;
import com.roberice.dsa.model.User;
import com.roberice.dsa.service.PosService;
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

public class PosView extends JPanel {

    private final List<Product> allProducts = new ArrayList<>();
    private final List<TransactionItem> cartItems = new ArrayList<>();

    private JPanel productsGridPanel;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;

    private DefaultTableModel cartTableModel;
    private JLabel totalAmountLabel;
    private JComboBox<String> paymentMethodCombo;
    private JTextField paymentAmountField;
    private JLabel changeAmountLabel;
    private JButton checkoutButton;

    public PosView() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_DARK);

        initComponents();
        loadProducts();
    }

    private void initComponents() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);
        splitPane.setDividerSize(4);
        splitPane.setBackground(UIUtils.BORDER_COLOR);

        // LEFT: Product Catalog
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(UIUtils.BG_DARK);
        leftPanel.setBorder(new EmptyBorder(16, 20, 16, 12));

        // Filters Header
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(0, 0, 14, 0));

        searchField = new JTextField(16);
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Search rice products...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterProducts(); }
            @Override public void removeUpdate(DocumentEvent e) { filterProducts(); }
            @Override public void changedUpdate(DocumentEvent e) { filterProducts(); }
        });

        categoryCombo = new JComboBox<>(new String[]{"All Varieties", "Premium", "Well-Milled", "Regular-Milled", "Special", "Brown", "Glutinous"});
        categoryCombo.addActionListener(e -> filterProducts());

        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh Stock", e -> loadProducts());

        filterBar.add(searchField);
        filterBar.add(categoryCombo);
        filterBar.add(refreshBtn);
        leftPanel.add(filterBar, BorderLayout.NORTH);

        // Products Grid
        productsGridPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        productsGridPanel.setBackground(UIUtils.BG_DARK);

        JScrollPane gridScroll = new JScrollPane(productsGridPanel);
        gridScroll.setBorder(null);
        gridScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftPanel.add(gridScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);

        // RIGHT: POS Cart & Checkout Panel
        JPanel rightPanel = UIUtils.createCard();
        rightPanel.setLayout(new BorderLayout(0, 12));
        rightPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Cart Header
        JPanel cartHeader = new JPanel(new BorderLayout());
        cartHeader.setOpaque(false);

        JLabel cartTitle = new JLabel("🛒 Current Order Cart");
        cartTitle.setFont(UIUtils.FONT_SUBHEADER);
        cartTitle.setForeground(UIUtils.TEXT_PRIMARY);

        JButton clearBtn = UIUtils.createSecondaryButton("Clear", e -> clearCart());
        clearBtn.setFont(UIUtils.FONT_SMALL);

        cartHeader.add(cartTitle, BorderLayout.WEST);
        cartHeader.add(clearBtn, BorderLayout.EAST);
        rightPanel.add(cartHeader, BorderLayout.NORTH);

        // Cart Table
        String[] cartCols = {"Item", "Unit", "Qty", "Price", "Subtotal"};
        cartTableModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable cartTable = UIUtils.createStyledTable(cartTableModel);
        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        rightPanel.add(cartScroll, BorderLayout.CENTER);

        // Cart Footer / Tender controls
        JPanel tenderPanel = new JPanel();
        tenderPanel.setLayout(new BoxLayout(tenderPanel, BoxLayout.Y_AXIS));
        tenderPanel.setOpaque(false);

        // Total Amount
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalLbl = new JLabel("TOTAL AMOUNT");
        totalLbl.setFont(UIUtils.FONT_BOLD);
        totalLbl.setForeground(UIUtils.TEXT_MUTED);

        totalAmountLabel = new JLabel("₱0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalAmountLabel.setForeground(UIUtils.ACCENT_SUCCESS);

        totalRow.add(totalLbl, BorderLayout.WEST);
        totalRow.add(totalAmountLabel, BorderLayout.EAST);
        tenderPanel.add(totalRow);
        tenderPanel.add(Box.createVerticalStrut(12));

        // Payment Method
        JPanel methodRow = new JPanel(new BorderLayout(8, 0));
        methodRow.setOpaque(false);
        JLabel methodLbl = new JLabel("Payment Method:");
        methodLbl.setForeground(UIUtils.TEXT_SECONDARY);
        paymentMethodCombo = new JComboBox<>(new String[]{"Cash", "GCash", "Maya", "Bank Transfer"});
        methodRow.add(methodLbl, BorderLayout.WEST);
        methodRow.add(paymentMethodCombo, BorderLayout.CENTER);
        tenderPanel.add(methodRow);
        tenderPanel.add(Box.createVerticalStrut(8));

        // Payment Amount Input
        JPanel paymentRow = new JPanel(new BorderLayout(8, 0));
        paymentRow.setOpaque(false);
        JLabel paymentLbl = new JLabel("Amount Tendered:");
        paymentLbl.setForeground(UIUtils.TEXT_SECONDARY);
        paymentAmountField = new JTextField();
        paymentAmountField.setFont(UIUtils.FONT_BOLD);
        paymentAmountField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { calculateChange(); }
            @Override public void removeUpdate(DocumentEvent e) { calculateChange(); }
            @Override public void changedUpdate(DocumentEvent e) { calculateChange(); }
        });
        paymentRow.add(paymentLbl, BorderLayout.WEST);
        paymentRow.add(paymentAmountField, BorderLayout.CENTER);
        tenderPanel.add(paymentRow);
        tenderPanel.add(Box.createVerticalStrut(8));

        // Quick Cash Presets
        JPanel quickCashRow = new JPanel(new GridLayout(1, 4, 6, 0));
        quickCashRow.setOpaque(false);
        quickCashRow.add(createQuickCashBtn(500));
        quickCashRow.add(createQuickCashBtn(1000));
        quickCashRow.add(createQuickCashBtn(2000));
        JButton exactBtn = UIUtils.createSecondaryButton("Exact", e -> {
            double total = getCartTotal();
            paymentAmountField.setText(String.format("%.2f", total));
        });
        exactBtn.setFont(UIUtils.FONT_SMALL);
        quickCashRow.add(exactBtn);
        tenderPanel.add(quickCashRow);
        tenderPanel.add(Box.createVerticalStrut(10));

        // Change Amount
        JPanel changeRow = new JPanel(new BorderLayout());
        changeRow.setOpaque(false);
        JLabel changeLbl = new JLabel("Change Due:");
        changeLbl.setFont(UIUtils.FONT_BOLD);
        changeLbl.setForeground(UIUtils.TEXT_MUTED);

        changeAmountLabel = new JLabel("₱0.00");
        changeAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        changeAmountLabel.setForeground(UIUtils.ACCENT_INFO);

        changeRow.add(changeLbl, BorderLayout.WEST);
        changeRow.add(changeAmountLabel, BorderLayout.EAST);
        tenderPanel.add(changeRow);
        tenderPanel.add(Box.createVerticalStrut(14));

        // Checkout Button
        checkoutButton = UIUtils.createPrimaryButton("✓ COMPLETE CHECKOUT", e -> processCheckout());
        checkoutButton.setPreferredSize(new Dimension(0, 44));
        checkoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        tenderPanel.add(checkoutButton);

        rightPanel.add(tenderPanel, BorderLayout.SOUTH);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JButton createQuickCashBtn(int amount) {
        JButton btn = UIUtils.createSecondaryButton("₱" + amount, e -> paymentAmountField.setText(String.valueOf(amount)));
        btn.setFont(UIUtils.FONT_SMALL);
        return btn;
    }

    public void loadProducts() {
        SwingUtilities.invokeLater(() -> {
            try {
                allProducts.clear();
                allProducts.addAll(ProductService.getProducts());
                filterProducts();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void filterProducts() {
        String query = searchField.getText().trim().toLowerCase();
        String selectedCat = (String) categoryCombo.getSelectedItem();

        productsGridPanel.removeAll();

        for (Product p : allProducts) {
            boolean matchesCat = selectedCat == null || "All Varieties".equals(selectedCat) ||
                    (p.getCategory() != null && p.getCategory().equalsIgnoreCase(selectedCat)) ||
                    (p.getRiceType() != null && p.getRiceType().equalsIgnoreCase(selectedCat));

            boolean matchesSearch = query.isEmpty() ||
                    p.getProductName().toLowerCase().contains(query) ||
                    (p.getCategory() != null && p.getCategory().toLowerCase().contains(query)) ||
                    (p.getRiceType() != null && p.getRiceType().toLowerCase().contains(query));

            if (matchesCat && matchesSearch) {
                productsGridPanel.add(createProductCard(p));
            }
        }

        productsGridPanel.revalidate();
        productsGridPanel.repaint();
    }

    private JPanel createProductCard(Product p) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(0, 8));

        // Header: Name & Origin
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel nameLbl = new JLabel(p.getProductName());
        nameLbl.setFont(UIUtils.FONT_BOLD);
        nameLbl.setForeground(UIUtils.TEXT_PRIMARY);

        JLabel originBadge = UIUtils.createBadge(p.getOrigin(),
                "Imported".equalsIgnoreCase(p.getOrigin()) ? UIUtils.WARNING_BG : UIUtils.SUCCESS_BG,
                "Imported".equalsIgnoreCase(p.getOrigin()) ? UIUtils.WARNING_TEXT : UIUtils.SUCCESS_TEXT);

        head.add(nameLbl, BorderLayout.WEST);
        head.add(originBadge, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        // Center: Available Stocks
        JPanel stockPanel = new JPanel(new GridLayout(2, 3, 4, 4));
        stockPanel.setOpaque(false);
        stockPanel.add(createStockPill("Sacks", p.getStockSacks() + ""));
        stockPanel.add(createStockPill("25kg", p.getPackaged25kg() + ""));
        stockPanel.add(createStockPill("20kg", p.getPackaged20kg() + ""));
        stockPanel.add(createStockPill("10kg", p.getPackaged10kg() + ""));
        stockPanel.add(createStockPill("5kg", p.getPackaged5kg() + ""));
        stockPanel.add(createStockPill("Loose", String.format("%.1f kg", p.getStockKg())));
        card.add(stockPanel, BorderLayout.CENTER);

        // Bottom: Unit Select & Add to Cart
        JPanel actionPanel = new JPanel(new BorderLayout(6, 0));
        actionPanel.setOpaque(false);

        List<UnitOption> units = new ArrayList<>();
        if (p.getPriceSack() > 0) units.add(new UnitOption("Sack (50kg)", "sack", p.getPriceSack()));
        if (p.getPrice25kg() > 0) units.add(new UnitOption("25kg Pack", "25kg", p.getPrice25kg()));
        if (p.getPrice20kg() > 0) units.add(new UnitOption("20kg Pack", "20kg", p.getPrice20kg()));
        if (p.getPrice10kg() > 0) units.add(new UnitOption("10kg Pack", "10kg", p.getPrice10kg()));
        if (p.getPrice5kg() > 0) units.add(new UnitOption("5kg Pack", "5kg", p.getPrice5kg()));
        if (p.getPriceKg() > 0) units.add(new UnitOption("1kg Loose", "kg", p.getPriceKg()));

        JComboBox<UnitOption> unitCombo = new JComboBox<>(units.toArray(new UnitOption[0]));

        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        qtySpinner.setPreferredSize(new Dimension(55, 32));

        JButton addBtn = UIUtils.createPrimaryButton("+ Add", e -> {
            UnitOption opt = (UnitOption) unitCombo.getSelectedItem();
            if (opt != null) {
                int qty = (int) qtySpinner.getValue();
                addToCart(p, opt.unitKey, opt.price, qty);
            }
        });

        JPanel selPanel = new JPanel(new BorderLayout(4, 0));
        selPanel.setOpaque(false);
        selPanel.add(unitCombo, BorderLayout.CENTER);
        selPanel.add(qtySpinner, BorderLayout.EAST);

        actionPanel.add(selPanel, BorderLayout.CENTER);
        actionPanel.add(addBtn, BorderLayout.EAST);
        card.add(actionPanel, BorderLayout.SOUTH);

        return card;
    }

    private JLabel createStockPill(String title, String val) {
        JLabel l = new JLabel(title + ": " + val, SwingConstants.CENTER);
        l.setFont(UIUtils.FONT_SMALL);
        l.setForeground(UIUtils.TEXT_MUTED);
        l.setOpaque(true);
        l.setBackground(UIUtils.BG_CARD_ALT);
        l.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true));
        return l;
    }

    private static class UnitOption {
        String label;
        String unitKey;
        double price;

        public UnitOption(String label, String unitKey, double price) {
            this.label = label + " - ₱" + String.format("%.2f", price);
            this.unitKey = unitKey;
            this.price = price;
        }
        @Override public String toString() { return label; }
    }

    private void addToCart(Product p, String unitKey, double unitPrice, int qty) {
        for (TransactionItem item : cartItems) {
            if (item.getProductId() == p.getProductId() && item.getUnit().equalsIgnoreCase(unitKey)) {
                item.setQuantity(item.getQuantity() + qty);
                item.setSubtotal(item.getQuantity() * item.getUnitPrice());
                updateCartDisplay();
                return;
            }
        }

        TransactionItem item = new TransactionItem(p.getProductId(), p.getProductName(), unitKey, qty, unitPrice, qty * unitPrice);
        cartItems.add(item);
        updateCartDisplay();
    }

    private void clearCart() {
        cartItems.clear();
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartTableModel.setRowCount(0);
        double total = 0.0;
        for (TransactionItem it : cartItems) {
            total += it.getSubtotal();
            cartTableModel.addRow(new Object[]{
                    it.getProductName(),
                    it.getUnit(),
                    it.getQuantity(),
                    UIUtils.formatCurrency(it.getUnitPrice()),
                    UIUtils.formatCurrency(it.getSubtotal())
            });
        }
        totalAmountLabel.setText(UIUtils.formatCurrency(total));
        calculateChange();
    }

    private double getCartTotal() {
        double total = 0.0;
        for (TransactionItem it : cartItems) total += it.getSubtotal();
        return total;
    }

    private void calculateChange() {
        double total = getCartTotal();
        try {
            String text = paymentAmountField.getText().trim();
            if (text.isEmpty()) {
                changeAmountLabel.setText("₱0.00");
                return;
            }
            double paid = Double.parseDouble(text);
            double change = paid - total;
            if (change >= 0) {
                changeAmountLabel.setText(UIUtils.formatCurrency(change));
                changeAmountLabel.setForeground(UIUtils.ACCENT_SUCCESS);
            } else {
                changeAmountLabel.setText("-" + UIUtils.formatCurrency(Math.abs(change)));
                changeAmountLabel.setForeground(UIUtils.ACCENT_DANGER);
            }
        } catch (NumberFormatException e) {
            changeAmountLabel.setText("₱0.00");
        }
    }

    private void processCheckout() {
        if (cartItems.isEmpty()) {
            UIUtils.showError(this, "Order cart is empty. Add rice items first.");
            return;
        }

        double total = getCartTotal();
        double paid;
        try {
            paid = Double.parseDouble(paymentAmountField.getText().trim());
        } catch (Exception e) {
            UIUtils.showError(this, "Please enter a valid payment amount.");
            return;
        }

        if (paid < total) {
            UIUtils.showError(this, String.format("Insufficient payment. Cart total is ₱%.2f, but received ₱%.2f.", total, paid));
            return;
        }

        User user = Session.getCurrentUser();
        int userId = user != null ? user.getUserId() : 1;
        String cashierName = user != null ? user.getFullName() : "Cashier";
        String method = (String) paymentMethodCombo.getSelectedItem();

        try {
            SalesTransaction tx = PosService.checkout(userId, cashierName, method, paid, cartItems);

            // Show Receipt Dialog
            showReceiptModal(tx);

            // Reset cart
            clearCart();
            paymentAmountField.setText("");
            changeAmountLabel.setText("₱0.00");

            // Refresh products
            loadProducts();

        } catch (Exception e) {
            UIUtils.showError(this, "Checkout Failed: " + e.getMessage());
        }
    }

    private void showReceiptModal(SalesTransaction tx) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Transaction Receipt #" + tx.getTransactionId(), true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(420, 520);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UIUtils.BG_DARK);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIUtils.BG_CARD);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel logoLbl = new JLabel("🌾 ROBERICE TRADING", SwingConstants.CENTER);
        logoLbl.setFont(UIUtils.FONT_HEADER);
        logoLbl.setForeground(UIUtils.TEXT_PRIMARY);
        logoLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Official Sales Receipt", SwingConstants.CENTER);
        subLbl.setFont(UIUtils.FONT_SMALL);
        subLbl.setForeground(UIUtils.TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel txNum = new JLabel("Invoice #: " + tx.getTransactionId() + "  •  Cashier: " + tx.getCashierName());
        txNum.setFont(UIUtils.FONT_SMALL);
        txNum.setForeground(UIUtils.TEXT_SECONDARY);
        txNum.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(logoLbl);
        content.add(subLbl);
        content.add(Box.createVerticalStrut(10));
        content.add(txNum);
        content.add(Box.createVerticalStrut(16));

        // Line Items Area
        JTextArea itemsArea = new JTextArea();
        itemsArea.setEditable(false);
        itemsArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        itemsArea.setBackground(UIUtils.BG_CARD_ALT);
        itemsArea.setForeground(UIUtils.TEXT_PRIMARY);
        itemsArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-18s %-5s %6s %8s\n", "Item", "Unit", "Qty", "Total"));
        sb.append("----------------------------------------\n");
        for (TransactionItem item : tx.getItems()) {
            String name = item.getProductName();
            if (name.length() > 18) name = name.substring(0, 17) + ".";
            sb.append(String.format("%-18s %-5s %6.1f %8.2f\n", name, item.getUnit(), item.getQuantity(), item.getSubtotal()));
        }
        sb.append("========================================\n");
        sb.append(String.format("%-30s ₱%8.2f\n", "TOTAL AMOUNT:", tx.getTotalAmount()));
        sb.append(String.format("%-30s ₱%8.2f\n", "AMOUNT PAID (" + tx.getPaymentMethod() + "):", tx.getPaymentAmount()));
        sb.append(String.format("%-30s ₱%8.2f\n", "CHANGE RETURNED:", tx.getChangeAmount()));
        itemsArea.setText(sb.toString());

        content.add(new JScrollPane(itemsArea));
        content.add(Box.createVerticalStrut(16));

        JButton closeBtn = UIUtils.createPrimaryButton("Done / Print Receipt", e -> dlg.dispose());
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(closeBtn);

        dlg.add(content, BorderLayout.CENTER);
        dlg.setVisible(true);
    }
}
