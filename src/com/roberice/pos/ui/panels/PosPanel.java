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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Point of Sale (POS) Terminal Panel.
 * Matches the warm cream catalog layout, product cards with origin badges,
 * pricing breakdown, quick packaging trigger, and clean right order checkout panel.
 */
public class PosPanel extends JPanel {
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final SalesDAO salesDAO = new SalesDAO();

    private List<Product> allProducts = new ArrayList<>();
    private List<Inventory> allInventories = new ArrayList<>();

    // Filters
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cmbOrigin = new JComboBox<>(new String[]{"All Origins", "Local", "Imported"});
    private final JComboBox<String> cmbRiceType = new JComboBox<>(new String[]{"All Rice Types", "Sinandomeng", "Dinorado", "Jasmine", "Angelika", "Well-Milled", "Glutinous / Malagkit", "Aromatic", "Brown Rice"});

    // Product Grid
    private final JPanel productGrid = new JPanel(new GridLayout(0, 4, 12, 12));

    // Cart Order
    private final List<TransactionItem> cartItems = new ArrayList<>();
    private final JPanel cartItemsContainer = new JPanel();
    private final JLabel lblCartEmpty = new JLabel("<html><center>Cart is empty. Select a rice variety and packaging<br>option above to start sale.</center></html>");
    private final JLabel lblSubtotalVal = new JLabel("₱0.00");
    private final JLabel lblTotalAmountVal = new JLabel("₱0.00");
    private final JComboBox<String> cmbPaymentMethod = new JComboBox<>(new String[]{"Cash", "GCash", "Maya"});
    private final JTextField txtAmountReceived = new JTextField("0.00");
    private final JLabel lblChangeVal = new JLabel("₱0.00");
    private final JButton btnCompleteSale;

    // Design Colors
    private static final Color COLOR_APP_BG = new Color(246, 243, 233);
    private static final Color COLOR_CARD_BG = new Color(255, 253, 246);
    private static final Color COLOR_CARD_BORDER = new Color(234, 230, 218);
    private static final Color COLOR_COMPLETE_BTN = new Color(33, 175, 129);      // Teal green #21AF81
    private static final Color COLOR_COMPLETE_BTN_HOVER = new Color(27, 150, 110);
    private static final Color COLOR_OPEN_SACK_BTN = new Color(166, 86, 25);      // Terracotta brown #A65619
    private static final Color COLOR_OPEN_SACK_HOVER = new Color(145, 72, 18);

    public PosPanel() {
        setLayout(new BorderLayout(14, 0));
        setBackground(COLOR_APP_BG);
        setBorder(new EmptyBorder(10, 16, 16, 16));

        // ================= LEFT: Catalog & Filters =================
        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setOpaque(false);

        // Filter Bar
        JPanel filterBar = new JPanel(new BorderLayout(10, 0));
        filterBar.setOpaque(false);

        // Search Input (Rounded with search icon)
        JPanel searchBox = new JPanel(new BorderLayout(6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(COLOR_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(380, 36));
        searchBox.setBorder(new EmptyBorder(4, 10, 4, 10));

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lblSearchIcon.setForeground(new Color(150, 155, 150));

        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search rice variety, origin, or type...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterCatalog(); }
            public void removeUpdate(DocumentEvent e) { filterCatalog(); }
            public void changedUpdate(DocumentEvent e) { filterCatalog(); }
        });

        searchBox.add(lblSearchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        // Right Filter controls
        JPanel rightFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightFilters.setOpaque(false);

        styleDropdown(cmbOrigin);
        cmbOrigin.setPreferredSize(new Dimension(110, 36));
        cmbOrigin.addActionListener(e -> filterCatalog());

        styleDropdown(cmbRiceType);
        cmbRiceType.setPreferredSize(new Dimension(130, 36));
        cmbRiceType.addActionListener(e -> filterCatalog());

        JButton btnOpenSack = new JButton("Open Sack") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(COLOR_OPEN_SACK_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(COLOR_OPEN_SACK_HOVER);
                } else {
                    g2.setColor(COLOR_OPEN_SACK_BTN);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnOpenSack.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOpenSack.setForeground(Color.WHITE);
        btnOpenSack.setContentAreaFilled(false);
        btnOpenSack.setBorderPainted(false);
        btnOpenSack.setFocusPainted(false);
        btnOpenSack.setPreferredSize(new Dimension(100, 36));
        btnOpenSack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOpenSack.addActionListener(e -> openSackQuickDialog());

        rightFilters.add(cmbOrigin);
        rightFilters.add(cmbRiceType);
        rightFilters.add(btnOpenSack);

        filterBar.add(searchBox, BorderLayout.CENTER);
        filterBar.add(rightFilters, BorderLayout.EAST);
        leftPanel.add(filterBar, BorderLayout.NORTH);

        // Product Grid Scroll
        productGrid.setOpaque(false);
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(productGrid, BorderLayout.NORTH);

        JScrollPane gridScroll = new JScrollPane(gridWrapper);
        gridScroll.setBorder(null);
        gridScroll.setOpaque(false);
        gridScroll.getViewport().setOpaque(false);
        gridScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftPanel.add(gridScroll, BorderLayout.CENTER);

        // Responsive columns based on available width
        gridScroll.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateGridColumns(gridScroll.getViewport().getWidth());
            }
        });

        // ================= RIGHT: Cart Order Panel =================
        JPanel cartPanel = new JPanel() {
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
        cartPanel.setOpaque(false);
        cartPanel.setLayout(new BorderLayout(0, 8));
        cartPanel.setPreferredSize(new Dimension(320, 600));
        cartPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Cart Header
        JPanel cartHeader = new JPanel(new BorderLayout());
        cartHeader.setOpaque(false);

        JLabel lblCartTitle = new JLabel("Current Order");
        lblCartTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCartTitle.setForeground(new Color(20, 30, 25));

        JButton btnClear = new JButton("Clear Cart");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnClear.setForeground(new Color(100, 115, 105));
        btnClear.setBackground(Color.WHITE);
        btnClear.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
        btnClear.setFocusPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> clearCart());

        cartHeader.add(lblCartTitle, BorderLayout.WEST);
        cartHeader.add(btnClear, BorderLayout.EAST);
        cartPanel.add(cartHeader, BorderLayout.NORTH);

        // Cart Items List
        cartItemsContainer.setLayout(new BoxLayout(cartItemsContainer, BoxLayout.Y_AXIS));
        cartItemsContainer.setOpaque(false);

        lblCartEmpty.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCartEmpty.setForeground(new Color(130, 140, 135));
        lblCartEmpty.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel emptyCenterWrap = new JPanel(new GridBagLayout());
        emptyCenterWrap.setOpaque(false);
        emptyCenterWrap.add(lblCartEmpty);

        JScrollPane cartScroll = new JScrollPane(cartItemsContainer);
        cartScroll.setBorder(null);
        cartScroll.setOpaque(false);
        cartScroll.getViewport().setOpaque(false);

        // Container showing either empty label or item list
        JPanel cartCenter = new JPanel(new BorderLayout());
        cartCenter.setOpaque(false);
        cartCenter.add(emptyCenterWrap, BorderLayout.CENTER);
        cartPanel.add(cartCenter, BorderLayout.CENTER);

        // Cart Footer (Totals, Payment, Checkout)
        JPanel cartFooter = new JPanel();
        cartFooter.setLayout(new BoxLayout(cartFooter, BoxLayout.Y_AXIS));
        cartFooter.setOpaque(false);
        cartFooter.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Subtotal row
        JPanel subtotalRow = new JPanel(new BorderLayout());
        subtotalRow.setOpaque(false);
        JLabel lblSubTag = new JLabel("Subtotal");
        lblSubTag.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubTag.setForeground(new Color(120, 130, 125));
        lblSubtotalVal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtotalVal.setForeground(new Color(120, 130, 125));
        subtotalRow.add(lblSubTag, BorderLayout.WEST);
        subtotalRow.add(lblSubtotalVal, BorderLayout.EAST);

        // Total Amount row
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel lblTotalTag = new JLabel("Total Amount");
        lblTotalTag.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalTag.setForeground(new Color(20, 30, 25));
        lblTotalAmountVal.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTotalAmountVal.setForeground(new Color(20, 30, 25));
        totalRow.add(lblTotalTag, BorderLayout.WEST);
        totalRow.add(lblTotalAmountVal, BorderLayout.EAST);

        // Payment Method
        JLabel lblPayMethodTag = new JLabel("Payment Method");
        lblPayMethodTag.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblPayMethodTag.setForeground(new Color(110, 120, 115));
        lblPayMethodTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleDropdown(cmbPaymentMethod);
        cmbPaymentMethod.setMaximumSize(new Dimension(300, 32));
        cmbPaymentMethod.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Amount Received
        JLabel lblRecvTag = new JLabel("Amount Received (₱)");
        lblRecvTag.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblRecvTag.setForeground(new Color(110, 120, 115));
        lblRecvTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtAmountReceived.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtAmountReceived.setMaximumSize(new Dimension(300, 32));
        txtAmountReceived.setPreferredSize(new Dimension(300, 32));
        txtAmountReceived.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtAmountReceived.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        txtAmountReceived.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateChange(); }
            public void removeUpdate(DocumentEvent e) { updateChange(); }
            public void changedUpdate(DocumentEvent e) { updateChange(); }
        });

        // Change row
        JPanel changeRow = new JPanel(new BorderLayout());
        changeRow.setOpaque(false);
        JLabel lblChangeTag = new JLabel("Change");
        lblChangeTag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblChangeTag.setForeground(new Color(20, 30, 25));
        lblChangeVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblChangeVal.setForeground(new Color(20, 30, 25));
        changeRow.add(lblChangeTag, BorderLayout.WEST);
        changeRow.add(lblChangeVal, BorderLayout.EAST);

        // Complete Sale Button
        btnCompleteSale = new JButton("✔  Complete Sale") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(COLOR_COMPLETE_BTN_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(COLOR_COMPLETE_BTN_HOVER);
                } else {
                    g2.setColor(COLOR_COMPLETE_BTN);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCompleteSale.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCompleteSale.setForeground(Color.WHITE);
        btnCompleteSale.setContentAreaFilled(false);
        btnCompleteSale.setBorderPainted(false);
        btnCompleteSale.setFocusPainted(false);
        btnCompleteSale.setMaximumSize(new Dimension(300, 38));
        btnCompleteSale.setPreferredSize(new Dimension(300, 38));
        btnCompleteSale.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCompleteSale.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCompleteSale.addActionListener(e -> doCheckout());

        cartFooter.add(subtotalRow);
        cartFooter.add(Box.createVerticalStrut(4));
        cartFooter.add(totalRow);
        cartFooter.add(Box.createVerticalStrut(10));
        cartFooter.add(lblPayMethodTag);
        cartFooter.add(Box.createVerticalStrut(3));
        cartFooter.add(cmbPaymentMethod);
        cartFooter.add(Box.createVerticalStrut(10));
        cartFooter.add(lblRecvTag);
        cartFooter.add(Box.createVerticalStrut(3));
        cartFooter.add(txtAmountReceived);
        cartFooter.add(Box.createVerticalStrut(10));
        cartFooter.add(changeRow);
        cartFooter.add(Box.createVerticalStrut(14));
        cartFooter.add(btnCompleteSale);

        cartPanel.add(cartFooter, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.CENTER);
        add(cartPanel, BorderLayout.EAST);

        refreshCatalog();
    }

    private void updateGridColumns(int width) {
        if (width <= 0) return;
        // Optimal product card width is around 180-210px
        int cols = Math.max(2, Math.min(6, width / 200));
        GridLayout layout = (GridLayout) productGrid.getLayout();
        if (layout.getColumns() != cols) {
            layout.setColumns(cols);
            productGrid.revalidate();
            productGrid.repaint();
        }
    }

    private void styleDropdown(JComboBox<String> cmb) {
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cmb.setBackground(Color.WHITE);
        cmb.setFocusable(false);
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
            if (!"All Rice Types".equals(riceType) && !p.getRiceType().toLowerCase().contains(riceType.toLowerCase())) {
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
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(COLOR_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setLayout(new BorderLayout(0, 6));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.setPreferredSize(new Dimension(200, 155));
        card.setMaximumSize(new Dimension(320, 155));
        card.setMinimumSize(new Dimension(170, 155));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Top Row: Title + Available status
        JPanel topRow = new JPanel(new BorderLayout(4, 0));
        topRow.setOpaque(false);

        JLabel lblName = new JLabel("<html><b>" + p.getProductName() + "</b></html>");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(new Color(20, 30, 25));

        boolean isAvail = inv != null && (inv.getStockSacks() > 0 || inv.getStockKg() > 0);
        JLabel badgeAvail = UIUtils.createBadge(isAvail ? "• Available" : "• Low Stock",
                isAvail ? new Color(220, 252, 231) : new Color(254, 242, 242),
                isAvail ? new Color(22, 101, 52) : new Color(185, 28, 28));

        topRow.add(lblName, BorderLayout.CENTER);
        topRow.add(badgeAvail, BorderLayout.EAST);

        // Middle Row: Origin tag + Rice type
        JPanel midRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        midRow.setOpaque(false);

        boolean isLocal = "Local".equalsIgnoreCase(p.getOrigin());
        JLabel badgeOrigin = UIUtils.createBadge(p.getOrigin(),
                isLocal ? new Color(240, 253, 244) : new Color(224, 242, 254),
                isLocal ? new Color(22, 101, 52) : new Color(14, 116, 144));

        JLabel lblType = new JLabel(p.getRiceType() != null ? p.getRiceType() : "");
        lblType.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblType.setForeground(new Color(130, 140, 135));

        midRow.add(badgeOrigin);
        midRow.add(lblType);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(240, 235, 225));

        // Bottom Row: Prices (Left) and Choose Packaging link (Right)
        JPanel bottomRow = new JPanel(new BorderLayout(4, 0));
        bottomRow.setOpaque(false);

        JPanel priceBlock = new JPanel(new GridLayout(2, 1, 0, 1));
        priceBlock.setOpaque(false);
        JLabel lblKg = new JLabel(UIUtils.formatCurrency(p.getPriceKg()) + "/kg •");
        lblKg.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblKg.setForeground(new Color(40, 50, 45));

        JLabel lblSack = new JLabel(UIUtils.formatCurrency(p.getPriceSack()) + "/sack");
        lblSack.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSack.setForeground(new Color(110, 120, 115));

        priceBlock.add(lblKg);
        priceBlock.add(lblSack);

        JPanel packLinkBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 4));
        packLinkBlock.setOpaque(false);
        JLabel lblPackText = new JLabel("<html><font color='#26322b'>Choose<br>Packaging →</font></html>");
        lblPackText.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        packLinkBlock.add(lblPackText);

        bottomRow.add(priceBlock, BorderLayout.WEST);
        bottomRow.add(packLinkBlock, BorderLayout.EAST);

        // Center stack
        JPanel centerStack = new JPanel();
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.setOpaque(false);
        centerStack.add(midRow);
        centerStack.add(Box.createVerticalStrut(8));
        centerStack.add(sep);
        centerStack.add(Box.createVerticalStrut(6));
        centerStack.add(bottomRow);

        card.add(topRow, BorderLayout.NORTH);
        card.add(centerStack, BorderLayout.CENTER);

        // Click handler to open Packaging Modal
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(PosPanel.this);
                PackagingModal modal = new PackagingModal(topFrame, p, inv);
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
        rebuildCartUI();
    }

    private void clearCart() {
        cartItems.clear();
        rebuildCartUI();
    }

    private void rebuildCartUI() {
        cartItemsContainer.removeAll();
        double subtotal = 0;

        for (int i = 0; i < cartItems.size(); i++) {
            TransactionItem item = cartItems.get(i);
            subtotal += item.getSubtotal();

            final int itemIdx = i;
            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setBackground(new Color(250, 248, 242));
            row.setBorder(new EmptyBorder(6, 8, 6, 8));
            row.setMaximumSize(new Dimension(300, 48));

            JPanel left = new JPanel(new GridLayout(2, 1, 0, 1));
            left.setOpaque(false);

            JLabel lblName = new JLabel(item.getProductName());
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblName.setForeground(new Color(20, 30, 25));

            String unitDesc = item.getUnitDisplay() + " x " + (item.getQuantity() == (long) item.getQuantity() ? String.format("%d", (long) item.getQuantity()) : String.format("%.2f", item.getQuantity()));
            JLabel lblDesc = new JLabel(unitDesc);
            lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblDesc.setForeground(new Color(110, 120, 115));

            left.add(lblName);
            left.add(lblDesc);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
            right.setOpaque(false);

            JLabel lblSub = new JLabel(UIUtils.formatCurrency(item.getSubtotal()));
            lblSub.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblSub.setForeground(new Color(27, 77, 50));

            JButton btnDel = new JButton("✕");
            btnDel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            btnDel.setForeground(new Color(220, 38, 38));
            btnDel.setContentAreaFilled(false);
            btnDel.setBorderPainted(false);
            btnDel.setFocusPainted(false);
            btnDel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDel.setBorder(new EmptyBorder(0, 4, 0, 0));
            btnDel.addActionListener(e -> {
                cartItems.remove(itemIdx);
                rebuildCartUI();
            });

            right.add(lblSub);
            right.add(btnDel);

            row.add(left, BorderLayout.CENTER);
            row.add(right, BorderLayout.EAST);

            cartItemsContainer.add(row);
            cartItemsContainer.add(Box.createVerticalStrut(4));
        }

        lblCartEmpty.setVisible(cartItems.isEmpty());
        lblSubtotalVal.setText(UIUtils.formatCurrency(subtotal));
        lblTotalAmountVal.setText(UIUtils.formatCurrency(subtotal));

        updateChange();

        cartItemsContainer.revalidate();
        cartItemsContainer.repaint();
    }

    private void updateChange() {
        double total = calculateTotal();
        try {
            double received = Double.parseDouble(txtAmountReceived.getText().trim());
            double change = received - total;
            lblChangeVal.setText(UIUtils.formatCurrency(Math.max(0, change)));
        } catch (Exception ex) {
            lblChangeVal.setText("₱0.00");
        }
    }

    private double calculateTotal() {
        double total = 0;
        for (TransactionItem item : cartItems) {
            total += item.getSubtotal();
        }
        return total;
    }

    private void doCheckout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The cart is empty. Please add items to checkout.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = calculateTotal();
        double received;
        try {
            received = Double.parseDouble(txtAmountReceived.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount received.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (received < total) {
            JOptionPane.showMessageDialog(this, "Amount received is less than total amount due.", "Insufficient Payment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            SalesTransaction tx = new SalesTransaction();
            tx.setUserId(AuthSession.getCurrentUser() != null ? AuthSession.getCurrentUser().getUserId() : 1);
            tx.setCashierName(AuthSession.getFullName());
            tx.setPaymentMethod((String) cmbPaymentMethod.getSelectedItem());
            tx.setTotalAmount(total);
            tx.setPaymentAmount(received);
            tx.setChangeAmount(received - total);
            tx.setTransactionStatus("Completed");
            tx.setItems(new ArrayList<>(cartItems));

            String username = AuthSession.getCurrentUser() != null ? AuthSession.getCurrentUser().getUsername() : "admin";
            SalesTransaction completedTx = salesDAO.processSale(tx, username);
            if (completedTx != null) {
                // Show printable receipt dialog
                ReceiptDialog rDialog = new ReceiptDialog((Frame) SwingUtilities.getWindowAncestor(this), completedTx);
                rDialog.setVisible(true);

                clearCart();
                refreshCatalog();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error processing sale: " + ex.getMessage(), "Checkout Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSackQuickDialog() {
        if (allProducts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products available to open sacks.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<Product> cmbProd = new JComboBox<>(allProducts.toArray(new Product[0]));
        JTextField txtSacks = new JTextField("1", 4);

        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Select Variety:"));
        p.add(cmbProd);
        p.add(new JLabel("Sacks to Open (50kg each):"));
        p.add(txtSacks);

        int res = JOptionPane.showConfirmDialog(this, p, "Open Sack to Loose Rice (Tinatakal)", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            Product selected = (Product) cmbProd.getSelectedItem();
            try {
                int count = Integer.parseInt(txtSacks.getText().trim());
                if (selected != null && count > 0) {
                    boolean ok = inventoryDAO.openSack(selected.getProductId(), "AUTO", count, AuthSession.getFullName());
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Successfully opened " + count + " sack(s) of " + selected.getProductName() + " into " + (count * 50) + "kg loose rice.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshCatalog();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to open sack. Insufficient stock in inventory.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid number of sacks.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
