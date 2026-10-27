package com.roberice.pos.ui.panels;

import com.roberice.pos.db.InventoryDAO;
import com.roberice.pos.db.ProductDAO;
import com.roberice.pos.db.SalesDAO;
import com.roberice.pos.model.Inventory;
import com.roberice.pos.model.Product;
import com.roberice.pos.model.SalesTransaction;
import com.roberice.pos.model.TransactionItem;
import com.roberice.pos.service.AuthSession;
import com.roberice.pos.ui.PackagingModal;
import com.roberice.pos.ui.ReceiptDialog;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Point of Sale (POS) Terminal Panel (Section 1.2 & 1.3 in User Manual).
 */
public class PosPanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final SalesDAO salesDAO = new SalesDAO();

    private List<Product> allProducts = new ArrayList<>();
    private List<Inventory> allInventories = new ArrayList<>();

    // Filters
    private final JTextField txtSearch = new JTextField(12);
    private final JComboBox<String> cmbOrigin = new JComboBox<>(new String[]{"All Origins", "Local", "Imported"});
    private final JComboBox<String> cmbRiceType = new JComboBox<>(new String[]{"All Types", "Sinandomeng", "Dinorado", "Jasmine", "Angelika", "Well-Milled", "Glutinous / Malagkit", "Aromatic", "Brown Rice"});

    // Product Grid
    private final JPanel productGrid = new JPanel(new GridLayout(0, 3, 12, 12));

    // Cart
    private final List<TransactionItem> cartItems = new ArrayList<>();
    private final DefaultTableModel cartTableModel = new DefaultTableModel(
            new String[]{"Variety", "Package", "Qty", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblCart = new JTable(cartTableModel);
    private final JLabel lblCartTotal = new JLabel("₱ 0.00");
    private final JComboBox<String> cmbPaymentMethod = new JComboBox<>(new String[]{"Cash", "GCash", "Maya"});
    private final JTextField txtAmountReceived = new JTextField(8);
    private final JLabel lblChange = new JLabel("₱ 0.00");
    private final JButton btnCheckout = UIUtils.createPrimaryButton("Complete Checkout");

    public PosPanel() {
        setLayout(new BorderLayout(14, 0));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // LEFT: Catalog & Filters
        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setBackground(UIUtils.COLOR_BG);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setBackground(UIUtils.COLOR_BG);

        filterBar.add(new JLabel("Search: "));
        txtSearch.setPreferredSize(new Dimension(160, 32));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterCatalog(); }
            public void removeUpdate(DocumentEvent e) { filterCatalog(); }
            public void changedUpdate(DocumentEvent e) { filterCatalog(); }
        });
        filterBar.add(txtSearch);

        filterBar.add(cmbOrigin);
        cmbOrigin.addActionListener(e -> filterCatalog());

        filterBar.add(cmbRiceType);
        cmbRiceType.addActionListener(e -> filterCatalog());

        JButton btnOpenSack = UIUtils.createSecondaryButton("Open a Sack");
        btnOpenSack.setForeground(UIUtils.COLOR_PRIMARY);
        btnOpenSack.addActionListener(e -> openSackQuickDialog());
        filterBar.add(btnOpenSack);

        leftPanel.add(filterBar, BorderLayout.NORTH);

        // Product Cards Scroll Pane
        productGrid.setBackground(UIUtils.COLOR_BG);
        JScrollPane gridScroll = new JScrollPane(productGrid);
        gridScroll.setBorder(null);
        gridScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftPanel.add(gridScroll, BorderLayout.CENTER);

        // RIGHT: Cart Panel
        JPanel cartPanel = UIUtils.createCard();
        cartPanel.setLayout(new BorderLayout(0, 12));
        cartPanel.setPreferredSize(new Dimension(380, 600));

        // Cart Header
        JPanel cartHeader = new JPanel(new BorderLayout());
        cartHeader.setBackground(Color.WHITE);
        JLabel lblCartTitle = new JLabel("Current Order");
        lblCartTitle.setFont(UIUtils.FONT_TITLE);
        lblCartTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JButton btnClear = new JButton("Clear Cart");
        btnClear.setFont(UIUtils.FONT_SMALL);
        btnClear.setForeground(UIUtils.COLOR_DANGER);
        btnClear.setContentAreaFilled(false);
        btnClear.setBorderPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> clearCart());

        cartHeader.add(lblCartTitle, BorderLayout.WEST);
        cartHeader.add(btnClear, BorderLayout.EAST);
        cartPanel.add(cartHeader, BorderLayout.NORTH);

        // Cart Table
        UIUtils.styleTable(tblCart);
        JScrollPane cartTableScroll = new JScrollPane(tblCart);
        cartTableScroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        cartPanel.add(cartTableScroll, BorderLayout.CENTER);

        // Cart Footer (Totals & Checkout)
        JPanel cartFooter = new JPanel();
        cartFooter.setLayout(new BoxLayout(cartFooter, BoxLayout.Y_AXIS));
        cartFooter.setBackground(Color.WHITE);
        cartFooter.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Subtotal row
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(Color.WHITE);
        JLabel lblTotTag = new JLabel("Total Amount:");
        lblTotTag.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCartTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCartTotal.setForeground(UIUtils.COLOR_PRIMARY);
        totalRow.add(lblTotTag, BorderLayout.WEST);
        totalRow.add(lblCartTotal, BorderLayout.EAST);

        // Payment Method row
        JPanel payRow = new JPanel(new BorderLayout(8, 0));
        payRow.setBackground(Color.WHITE);
        JLabel lblPayTag = new JLabel("Payment:");
        lblPayTag.setFont(UIUtils.FONT_BOLD);
        cmbPaymentMethod.setPreferredSize(new Dimension(180, 32));
        payRow.add(lblPayTag, BorderLayout.WEST);
        payRow.add(cmbPaymentMethod, BorderLayout.EAST);

        // Amount Received row
        JPanel recvRow = new JPanel(new BorderLayout(8, 0));
        recvRow.setBackground(Color.WHITE);
        JLabel lblRecvTag = new JLabel("Amount Received:");
        lblRecvTag.setFont(UIUtils.FONT_BOLD);
        txtAmountReceived.setPreferredSize(new Dimension(180, 32));
        txtAmountReceived.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtAmountReceived.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateChange(); }
            public void removeUpdate(DocumentEvent e) { updateChange(); }
            public void changedUpdate(DocumentEvent e) { updateChange(); }
        });
        recvRow.add(lblRecvTag, BorderLayout.WEST);
        recvRow.add(txtAmountReceived, BorderLayout.EAST);

        // Change row
        JPanel changeRow = new JPanel(new BorderLayout());
        changeRow.setBackground(Color.WHITE);
        JLabel lblChgTag = new JLabel("Change Due:");
        lblChgTag.setFont(UIUtils.FONT_BOLD);
        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblChange.setForeground(UIUtils.COLOR_SUCCESS);
        changeRow.add(lblChgTag, BorderLayout.WEST);
        changeRow.add(lblChange, BorderLayout.EAST);

        // Remove item button
        JButton btnRemoveItem = UIUtils.createSecondaryButton("Remove Selected Item");
        btnRemoveItem.setMaximumSize(new Dimension(380, 32));
        btnRemoveItem.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRemoveItem.addActionListener(e -> removeSelectedItem());

        btnCheckout.setMaximumSize(new Dimension(380, 42));
        btnCheckout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCheckout.addActionListener(e -> doCheckout());

        cartFooter.add(btnRemoveItem);
        cartFooter.add(Box.createVerticalStrut(8));
        cartFooter.add(totalRow);
        cartFooter.add(Box.createVerticalStrut(8));
        cartFooter.add(payRow);
        cartFooter.add(Box.createVerticalStrut(8));
        cartFooter.add(recvRow);
        cartFooter.add(Box.createVerticalStrut(8));
        cartFooter.add(changeRow);
        cartFooter.add(Box.createVerticalStrut(12));
        cartFooter.add(btnCheckout);

        cartPanel.add(cartFooter, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.CENTER);
        add(cartPanel, BorderLayout.EAST);

        refreshCatalog();
    }

    public void refreshCatalog() {
        try {
            allProducts = productDAO.getAllProducts(true);
            allInventories = inventoryDAO.getInventorySummary();
            filterCatalog();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void filterCatalog() {
        productGrid.removeAll();

        String q = txtSearch.getText().trim().toLowerCase();
        String origin = (String) cmbOrigin.getSelectedItem();
        String riceType = (String) cmbRiceType.getSelectedItem();

        for (Product p : allProducts) {
            if (!q.isEmpty() && !p.getProductName().toLowerCase().contains(q) && !p.getRiceType().toLowerCase().contains(q)) {
                continue;
            }
            if (!"All Origins".equals(origin) && !p.getOrigin().equalsIgnoreCase(origin)) {
                continue;
            }
            if (!"All Types".equals(riceType) && !p.getRiceType().toLowerCase().contains(riceType.toLowerCase())) {
                continue;
            }

            Inventory inv = findInventoryForProduct(p.getProductId());
            productGrid.add(createProductCard(p, inv));
        }

        productGrid.revalidate();
        productGrid.repaint();
    }

    private Inventory findInventoryForProduct(int productId) {
        for (Inventory i : allInventories) {
            if (i.getProductId() == productId) return i;
        }
        return null;
    }

    private JPanel createProductCard(Product p, Inventory inv) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Top tags
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);

        JLabel lblName = new JLabel("<html><b>" + p.getProductName() + "</b></html>");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel badge = UIUtils.createBadge(p.getOrigin(),
                "Local".equalsIgnoreCase(p.getOrigin()) ? new Color(240, 253, 244) : new Color(239, 246, 255),
                "Local".equalsIgnoreCase(p.getOrigin()) ? UIUtils.COLOR_PRIMARY : UIUtils.COLOR_INFO);

        top.add(lblName, BorderLayout.WEST);
        top.add(badge, BorderLayout.EAST);

        // Prices & Stock info
        JPanel center = new JPanel(new GridLayout(3, 1, 2, 2));
        center.setBackground(Color.WHITE);

        JLabel lblSackPrice = new JLabel("50kg Sack: " + UIUtils.formatCurrency(p.getPriceSack()));
        lblSackPrice.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSackPrice.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel lblKgPrice = new JLabel("Tinatakal: " + UIUtils.formatCurrency(p.getPriceKg()) + " / kg");
        lblKgPrice.setFont(UIUtils.FONT_SMALL);
        lblKgPrice.setForeground(UIUtils.COLOR_TEXT_MUTED);

        int sacks = inv != null ? inv.getStockSacks() : 0;
        double looseKg = inv != null ? inv.getStockKg() : 0;
        JLabel lblStock = new JLabel(String.format("In Stock: %d sacks • %.1f kg loose", sacks, looseKg));
        lblStock.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStock.setForeground(sacks > 0 || looseKg > 20 ? UIUtils.COLOR_SUCCESS : UIUtils.COLOR_WARNING);

        center.add(lblSackPrice);
        center.add(lblKgPrice);
        center.add(lblStock);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);

        // Click card to open Packaging Modal
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                PackagingModal modal = new PackagingModal((Frame) SwingUtilities.getWindowAncestor(PosPanel.this), p);
                modal.setVisible(true);
                TransactionItem item = modal.getResultItem();
                if (item != null) {
                    addItemToCart(item);
                }
            }
        });

        return card;
    }

    private void addItemToCart(TransactionItem item) {
        cartItems.add(item);
        cartTableModel.addRow(new Object[]{
                item.getProductName(),
                item.getUnitDisplay(),
                formatQty(item.getQuantity()),
                UIUtils.formatCurrency(item.getSubtotal())
        });
        updateCartTotal();
    }

    private void removeSelectedItem() {
        int row = tblCart.getSelectedRow();
        if (row >= 0 && row < cartItems.size()) {
            cartItems.remove(row);
            cartTableModel.removeRow(row);
            updateCartTotal();
        }
    }

    private void clearCart() {
        cartItems.clear();
        cartTableModel.setRowCount(0);
        updateCartTotal();
    }

    private double calculateTotal() {
        double sum = 0;
        for (TransactionItem it : cartItems) sum += it.getSubtotal();
        return sum;
    }

    private void updateCartTotal() {
        double total = calculateTotal();
        lblCartTotal.setText(UIUtils.formatCurrency(total));
        updateChange();
    }

    private void updateChange() {
        double total = calculateTotal();
        try {
            double received = Double.parseDouble(txtAmountReceived.getText().trim());
            double change = received - total;
            if (change >= 0) {
                lblChange.setText(UIUtils.formatCurrency(change));
                lblChange.setForeground(UIUtils.COLOR_SUCCESS);
            } else {
                lblChange.setText("Short by " + UIUtils.formatCurrency(Math.abs(change)));
                lblChange.setForeground(UIUtils.COLOR_DANGER);
            }
        } catch (NumberFormatException ex) {
            lblChange.setText("₱ 0.00");
        }
    }

    private void doCheckout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot checkout: Current Order cart is empty.", "Cart Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = calculateTotal();
        double received = 0;
        try {
            received = Double.parseDouble(txtAmountReceived.getText().trim());
        } catch (NumberFormatException ex) {
            // If empty, assume exact cash
            received = total;
            txtAmountReceived.setText(String.format("%.2f", total));
        }

        if (received < total) {
            JOptionPane.showMessageDialog(this, "Amount received is less than total order amount.", "Insufficient Payment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double change = received - total;
        String payMethod = (String) cmbPaymentMethod.getSelectedItem();

        SalesTransaction tx = new SalesTransaction();
        tx.setTotalAmount(total);
        tx.setPaymentAmount(received);
        tx.setChangeAmount(change);
        tx.setPaymentMethod(payMethod);
        tx.setItems(new ArrayList<>(cartItems));

        try {
            SalesTransaction processed = salesDAO.processSale(tx, AuthSession.getUsername());
            JOptionPane.showMessageDialog(this, "Order completed successfully!\nReceipt No: REC-" + String.format("%06d", processed.getTransactionId()), "Checkout Complete", JOptionPane.INFORMATION_MESSAGE);

            // Open Receipt Modal
            ReceiptDialog rDialog = new ReceiptDialog((Frame) SwingUtilities.getWindowAncestor(this), processed);
            rDialog.setVisible(true);

            // Reset cart
            clearCart();
            txtAmountReceived.setText("");
            lblChange.setText("₱ 0.00");
            refreshCatalog();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Checkout Failed:\n" + ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSackQuickDialog() {
        if (allProducts.isEmpty()) return;
        JComboBox<Product> pCombo = new JComboBox<>(allProducts.toArray(new Product[0]));
        JTextField txtSacks = new JTextField("1", 4);

        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Rice Variety:"));
        p.add(pCombo);
        p.add(new JLabel("Whole Sacks to Open (50kg):"));
        p.add(txtSacks);

        int res = JOptionPane.showConfirmDialog(this, p, "Quick Sack Opening into Loose Bin", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            Product prod = (Product) pCombo.getSelectedItem();
            try {
                int count = Integer.parseInt(txtSacks.getText().trim());
                if (prod != null && count > 0) {
                    inventoryDAO.openSack(prod.getProductId(), "AUTO", count, AuthSession.getFullName());
                    JOptionPane.showMessageDialog(this, "Opened " + count + "x 50kg sack(s) into loose inventory.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshCatalog();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String formatQty(double q) {
        return (q == (int) q) ? String.valueOf((int) q) : String.format("%.2f", q);
    }
}
