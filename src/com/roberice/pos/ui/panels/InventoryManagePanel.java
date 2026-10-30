package com.roberice.pos.ui.panels;

import com.roberice.pos.db.InventoryDAO;
import com.roberice.pos.db.ProductDAO;
import com.roberice.pos.model.Inventory;
import com.roberice.pos.model.InventoryBatch;
import com.roberice.pos.model.Product;
import com.roberice.pos.service.AuthSession;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/**
 * Modern Manage Inventory Panel.
 * Matches Screenshot 2: Admin control panel for warehouse restocks, sack openings,
 * physical count corrections, damage reporting, pre-packed breakdown pills,
 * stock age & FIFO lot indicator, low-stock threshold alert, and multi-action controls.
 */
public class InventoryManagePanel extends JPanel {
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private List<Product> allProducts = new ArrayList<>();
    private List<Inventory> allInventories = new ArrayList<>();
    private final Map<Integer, List<InventoryBatch>> batchCache = new HashMap<>();

    // Filters
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cmbOrigin = new JComboBox<>(new String[]{"All Origins", "Local", "Imported"});
    private final JComboBox<String> cmbRiceType = new JComboBox<>(new String[]{"All Rice Types", "Sinandomeng", "Dinorado", "Jasmine", "Angelika", "Well-Milled", "Glutinous / Malagkit", "Aromatic", "Brown Rice"});
    private final JComboBox<String> cmbAge = new JComboBox<>(new String[]{"All Stock Ages", "Fresh (<=30d)", "Normal (31-60d)", "Aging (>60d)"});
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"All Statuses", "Sufficient", "Low Stock", "Out of Stock"});

    // Rows Container
    private final JPanel rowsContainer = new JPanel();

    // Design Colors
    private static final Color COLOR_APP_BG = new Color(246, 243, 233);
    private static final Color COLOR_CARD_BG = new Color(255, 253, 246);
    private static final Color COLOR_CARD_BORDER = new Color(234, 230, 218);
    private static final Color COLOR_TH_BG = new Color(242, 234, 213);
    private static final Color COLOR_OPEN_SACK = new Color(178, 85, 15);
    private static final Color COLOR_OPEN_SACK_HOVER = new Color(150, 70, 10);
    private static final Color COLOR_RESTOCK = new Color(27, 77, 50);
    private static final Color COLOR_RESTOCK_HOVER = new Color(20, 60, 38);

    public InventoryManagePanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(COLOR_APP_BG);
        setBorder(new EmptyBorder(10, 16, 16, 16));

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

        // Filter Bar
        JPanel filterBar = createFilterBar();
        card.add(filterBar, BorderLayout.NORTH);

        // Table Panel (Section Header + Column Header + Scrollable Dynamic Rows)
        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setOpaque(false);

        // Section title
        JLabel lblSectionTitle = new JLabel("Current Rice Stock Levels & Aging");
        lblSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSectionTitle.setForeground(new Color(20, 30, 25));

        JPanel tableHeader = createTableHeader();

        JPanel topGroup = new JPanel(new BorderLayout(0, 8));
        topGroup.setOpaque(false);
        topGroup.add(lblSectionTitle, BorderLayout.NORTH);
        topGroup.add(tableHeader, BorderLayout.CENTER);
        tablePanel.add(topGroup, BorderLayout.NORTH);

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

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);

        // Search Input
        JPanel searchBox = new JPanel(new BorderLayout(6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(COLOR_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(380, 34));
        searchBox.setBorder(new EmptyBorder(4, 10, 4, 10));

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lblSearchIcon.setForeground(new Color(150, 155, 150));

        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search variety, origin, or rice type...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { rebuildRows(); }
            public void removeUpdate(DocumentEvent e) { rebuildRows(); }
            public void changedUpdate(DocumentEvent e) { rebuildRows(); }
        });

        searchBox.add(lblSearchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        // Dropdowns
        JPanel rightFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightFilters.setOpaque(false);

        styleDropdown(cmbOrigin);
        styleDropdown(cmbRiceType);
        styleDropdown(cmbAge);
        styleDropdown(cmbStatus);

        cmbOrigin.addActionListener(e -> rebuildRows());
        cmbRiceType.addActionListener(e -> rebuildRows());
        cmbAge.addActionListener(e -> rebuildRows());
        cmbStatus.addActionListener(e -> rebuildRows());

        rightFilters.add(cmbOrigin);
        rightFilters.add(cmbRiceType);
        rightFilters.add(cmbAge);
        rightFilters.add(cmbStatus);

        bar.add(searchBox, BorderLayout.CENTER);
        bar.add(rightFilters, BorderLayout.EAST);
        return bar;
    }

    private void styleDropdown(JComboBox<String> cmb) {
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cmb.setBackground(Color.WHITE);
        cmb.setFocusable(false);
    }

    private JPanel createTableHeader() {
        JPanel th = new JPanel(new GridBagLayout());
        th.setBackground(COLOR_TH_BG);
        th.setPreferredSize(new Dimension(1000, 36));
        th.setMinimumSize(new Dimension(800, 36));
        th.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 218, 198)));

        double[] weights = {0.15, 0.09, 0.08, 0.16, 0.08, 0.09, 0.12, 0.08, 0.06, 0.09};
        String[] titles = {
            "RICE VARIETY", "ORIGIN & TYPE", "SACKS STOCK",
            "PRE-PACKED (25KG / 20KG / 10KG / 5KG)", "LOOSE / TINATAKAL", "TOTAL EQUIVALENT",
            "STOCK AGE & BATCH", "LOW-STOCK ALERT", "STATUS", "ACTIONS"
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

            if (i == 2) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (i == 3) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (i == 4 || i == 5) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (i == 6) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (i == 7) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (i >= 8) {
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
            allProducts = productDAO.getAllProducts(true);
            allInventories = inventoryDAO.getInventorySummary();

            batchCache.clear();
            for (Product p : allProducts) {
                List<InventoryBatch> batches = inventoryDAO.getBatchesForProduct(p.getProductId(), true);
                batchCache.put(p.getProductId(), batches);
            }

            rebuildRows();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void rebuildRows() {
        rowsContainer.removeAll();

        String q = txtSearch.getText().trim().toLowerCase();
        String origin = (String) cmbOrigin.getSelectedItem();
        String riceType = (String) cmbRiceType.getSelectedItem();
        String ageFilter = (String) cmbAge.getSelectedItem();
        String statusFilter = (String) cmbStatus.getSelectedItem();

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

            Inventory inv = findInventory(p.getProductId());
            boolean isSufficient = inv != null && inv.getStockSacks() > 5;
            String status = isSufficient ? "Sufficient" : (inv != null && inv.getStockSacks() > 0 ? "Low Stock" : "Out of Stock");

            if (!"All Statuses".equals(statusFilter) && !status.equalsIgnoreCase(statusFilter)) {
                continue;
            }

            List<InventoryBatch> batches = batchCache.getOrDefault(p.getProductId(), Collections.emptyList());

            if (!"All Stock Ages".equals(ageFilter)) {
                boolean matchAge = false;
                for (InventoryBatch b : batches) {
                    long age = b.getAgeInDays();
                    if (ageFilter.startsWith("Fresh") && age <= 30) matchAge = true;
                    else if (ageFilter.startsWith("Normal") && age > 30 && age <= 60) matchAge = true;
                    else if (ageFilter.startsWith("Aging") && age > 60) matchAge = true;
                }
                if (!matchAge && !batches.isEmpty()) continue;
            }

            JPanel manageRow = createManageRow(p, inv, batches, status);
            rowsContainer.add(manageRow);

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

    private JPanel createManageRow(Product p, Inventory inv, List<InventoryBatch> batches, String status) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(1000, 68));
        row.setMinimumSize(new Dimension(800, 68));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        double[] weights = {0.15, 0.09, 0.08, 0.16, 0.08, 0.09, 0.12, 0.08, 0.06, 0.09};

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;

        // 1. Variety Name + Subtitle
        JPanel cell1 = new JPanel(new GridLayout(2, 1, 0, 1));
        cell1.setOpaque(false);
        JLabel lblName = new JLabel(p.getProductName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(new Color(20, 30, 25));
        JLabel lblSub = new JLabel(p.getRiceType() != null ? p.getRiceType() : "Rice Variety");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(130, 140, 135));
        cell1.add(lblName);
        cell1.add(lblSub);
        cell1.setBorder(new EmptyBorder(0, 6, 0, 4));

        // 2. Origin & Type
        JPanel cell2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        cell2.setOpaque(false);
        boolean isLocal = "Local".equalsIgnoreCase(p.getOrigin());
        JLabel badgeOrigin = createBadgePill(p.getOrigin(),
                isLocal ? new Color(240, 253, 244) : new Color(224, 242, 254),
                isLocal ? new Color(22, 101, 52) : new Color(14, 116, 144));
        cell2.add(badgeOrigin);

        // 3. Sacks Stock
        int sacks = inv != null ? inv.getStockSacks() : 0;
        JPanel cell3 = new JPanel(new GridLayout(2, 1, 0, 0));
        cell3.setOpaque(false);
        JLabel lblSackCount = new JLabel(sacks + " sacks");
        lblSackCount.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSackCount.setForeground(new Color(20, 30, 25));
        lblSackCount.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel lblSackSize = new JLabel("(50Kg)");
        lblSackSize.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblSackSize.setForeground(new Color(130, 140, 135));
        lblSackSize.setHorizontalAlignment(SwingConstants.RIGHT);
        cell3.add(lblSackCount);
        cell3.add(lblSackSize);
        cell3.setBorder(new EmptyBorder(0, 2, 0, 8));

        // 4. Pre-packed breakdown
        JPanel cell4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 4));
        cell4.setOpaque(false);
        int p25 = inv != null ? inv.getPackaged25kg() : 0;
        int p20 = inv != null ? inv.getPackaged20kg() : 0;
        int p10 = inv != null ? inv.getPackaged10kg() : 0;
        int p5 = inv != null ? inv.getPackaged5kg() : 0;
        cell4.add(createMiniPill("25kg Half: " + p25, new Color(238, 242, 255), new Color(79, 70, 229)));
        cell4.add(createMiniPill("20kg: " + p20, new Color(248, 250, 252), new Color(100, 116, 139)));
        cell4.add(createMiniPill("10kg: " + p10, new Color(254, 243, 199), new Color(180, 83, 9)));
        cell4.add(createMiniPill("5kg: " + p5, new Color(243, 232, 255), new Color(147, 51, 234)));

        // 5. Loose / Tinatakal
        double looseKg = inv != null ? inv.getStockKg() : 0;
        JLabel cell5 = new JLabel(String.format("%.1f kg", looseKg));
        cell5.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cell5.setForeground(new Color(40, 50, 45));
        cell5.setHorizontalAlignment(SwingConstants.RIGHT);
        cell5.setBorder(new EmptyBorder(0, 2, 0, 8));

        // 6. Total Equivalent
        double totalKg = inv != null ? ((sacks * 50.0) + (p25 * 25.0) + (p20 * 20.0) + (p10 * 10.0) + (p5 * 5.0) + looseKg) : 0;
        JLabel cell6 = new JLabel(String.format("%,.2f kg", totalKg));
        cell6.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cell6.setForeground(new Color(20, 30, 25));
        cell6.setHorizontalAlignment(SwingConstants.RIGHT);
        cell6.setBorder(new EmptyBorder(0, 2, 0, 8));

        // 7. Stock Age & Batch
        JPanel cell7 = new JPanel();
        cell7.setLayout(new BoxLayout(cell7, BoxLayout.Y_AXIS));
        cell7.setOpaque(false);

        InventoryBatch oldestBatch = !batches.isEmpty() ? batches.get(0) : null;
        long age = oldestBatch != null ? oldestBatch.getAgeInDays() : 0;
        String bCode = oldestBatch != null ? oldestBatch.getBatchCode() : "N/A";
        String bDate = (oldestBatch != null && oldestBatch.getReceivedDate() != null)
                ? new SimpleDateFormat("MMM d, yyyy").format(oldestBatch.getReceivedDate()) : "";

        String agePillText = age + "d " + (age > 60 ? "(Aging)" : (age > 30 ? "(Normal)" : "(Fresh)"));
        Color ageBg = age > 60 ? new Color(254, 243, 199) : (age > 30 ? new Color(224, 242, 254) : new Color(220, 252, 231));
        Color ageFg = age > 60 ? new Color(180, 83, 9) : (age > 30 ? new Color(14, 116, 144) : new Color(22, 101, 52));

        JLabel lblAgePill = createBadgePill(agePillText, ageBg, ageFg);
        lblAgePill.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblBatchMeta = new JLabel(bCode + " (" + bDate + ")");
        lblBatchMeta.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        lblBatchMeta.setForeground(new Color(130, 140, 135));
        lblBatchMeta.setAlignmentX(Component.CENTER_ALIGNMENT);

        cell7.add(lblAgePill);
        cell7.add(Box.createVerticalStrut(2));
        cell7.add(lblBatchMeta);

        // 8. Low-Stock Alert
        double reorder = inv != null ? inv.getReorderLevelKg() : 250;
        JLabel cell8 = new JLabel((int) reorder + " kg alert");
        cell8.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        cell8.setForeground(new Color(70, 80, 75));
        cell8.setHorizontalAlignment(SwingConstants.RIGHT);
        cell8.setBorder(new EmptyBorder(0, 2, 0, 8));

        // 9. Status Pill
        JPanel cell9 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        cell9.setOpaque(false);
        Color statBg = "Sufficient".equalsIgnoreCase(status) ? new Color(220, 252, 231) : ("Low Stock".equalsIgnoreCase(status) ? new Color(254, 243, 199) : new Color(254, 242, 242));
        Color statFg = "Sufficient".equalsIgnoreCase(status) ? new Color(22, 101, 52) : ("Low Stock".equalsIgnoreCase(status) ? new Color(180, 83, 9) : new Color(185, 28, 28));
        JLabel badgeStatus = createBadgePill(status, statBg, statFg);
        cell9.add(badgeStatus);

        // 10. Actions Stack: Open Sack + Restock + Adjust/Repack
        JPanel cell10 = new JPanel();
        cell10.setLayout(new BoxLayout(cell10, BoxLayout.Y_AXIS));
        cell10.setOpaque(false);
        cell10.setBorder(new EmptyBorder(2, 0, 2, 0));

        JButton btnOpen = createSmallButton("Open Sack", COLOR_OPEN_SACK, COLOR_OPEN_SACK_HOVER);
        btnOpen.addActionListener(e -> triggerOpenSack(p));

        JButton btnRestock = createSmallButton("Restock", COLOR_RESTOCK, COLOR_RESTOCK_HOVER);
        btnRestock.addActionListener(e -> triggerRestock(p));

        JButton btnAdjust = new JButton("Adjust / Repack") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(230, 225, 215));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(245, 240, 230));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                g2.setColor(new Color(205, 195, 180));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAdjust.setFont(new Font("Segoe UI", Font.BOLD, 9));
        btnAdjust.setForeground(new Color(40, 50, 45));
        btnAdjust.setContentAreaFilled(false);
        btnAdjust.setBorderPainted(false);
        btnAdjust.setFocusPainted(false);
        btnAdjust.setPreferredSize(new Dimension(84, 21));
        btnAdjust.setMaximumSize(new Dimension(84, 21));
        btnAdjust.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdjust.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAdjust.addActionListener(e -> showAdjustRepackMenu(p, inv, btnAdjust));

        cell10.add(btnOpen);
        cell10.add(Box.createVerticalStrut(2));
        cell10.add(btnRestock);
        cell10.add(Box.createVerticalStrut(2));
        cell10.add(btnAdjust);

        Component[] cells = {cell1, cell2, cell3, cell4, cell5, cell6, cell7, cell8, cell9, cell10};
        for (int i = 0; i < cells.length; i++) {
            c.gridx = i;
            c.weightx = weights[i];
            row.add(cells[i], c);
        }

        return row;
    }

    private JButton createSmallButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(hover.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hover);
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 9));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(84, 21));
        btn.setMaximumSize(new Dimension(84, 21));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createBadgePill(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(1, 5, 1, 5));
        return lbl;
    }

    private JLabel createMiniPill(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(1, 4, 1, 4));
        return lbl;
    }

    private void triggerOpenSack(Product p) {
        String input = JOptionPane.showInputDialog(this,
                "Enter number of whole 50kg sacks to open for " + p.getProductName() + ":",
                "Open Sack to Loose Rice", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int sacks = Integer.parseInt(input.trim());
                if (sacks > 0) {
                    boolean ok = inventoryDAO.openSack(p.getProductId(), "AUTO", sacks, AuthSession.getFullName());
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Opened " + sacks + " sack(s) (" + (sacks * 50) + "kg loose rice).", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Insufficient sacks in stock.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void triggerRestock(Product p) {
        JTextField txtBatch = new JTextField("BATCH-" + System.currentTimeMillis() % 10000);
        JTextField txtSacks = new JTextField("20");
        JTextField txtSupplier = new JTextField("Central Luzon Milling Corp");

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Batch / Lot Code:"));
        form.add(txtBatch);
        form.add(new JLabel("50kg Sacks Received:"));
        form.add(txtSacks);
        form.add(new JLabel("Supplier / Origin:"));
        form.add(txtSupplier);

        int res = JOptionPane.showConfirmDialog(this, form, "Restock Shipment - " + p.getProductName(), JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int sacks = Integer.parseInt(txtSacks.getText().trim());
                String code = txtBatch.getText().trim();
                if (sacks > 0 && !code.isEmpty()) {
                    String todayStr = LocalDate.now().toString();
                    boolean ok = inventoryDAO.restock(p.getProductId(), code, todayStr, sacks, 0, 0, 0, 0, 0.0, txtSupplier.getText().trim(), AuthSession.getFullName());
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Successfully restocked " + sacks + " sacks of " + p.getProductName() + "!", "Restock Successful", JOptionPane.INFORMATION_MESSAGE);
                        refreshData();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid restock input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAdjustRepackMenu(Product p, Inventory inv, JComponent anchor) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem itmRepack = new JMenuItem("Repack from Loose Rice");
        itmRepack.addActionListener(e -> showRepackDialog(p, inv));

        JMenuItem itmCorrect = new JMenuItem("Physical Count Correction");
        itmCorrect.addActionListener(e -> showCorrectionDialog(p, inv));

        JMenuItem itmDamage = new JMenuItem("Log Spoilage / Damage");
        itmDamage.addActionListener(e -> showDamageDialog(p, inv));

        menu.add(itmRepack);
        menu.add(itmCorrect);
        menu.addSeparator();
        menu.add(itmDamage);

        menu.show(anchor, 0, anchor.getHeight());
    }

    private void showRepackDialog(Product p, Inventory inv) {
        String[] options = {"25kg Bag", "20kg Bag", "10kg Bag", "5kg Bag"};
        JComboBox<String> cmbSize = new JComboBox<>(options);
        JTextField txtBags = new JTextField("1");

        double loose = inv != null ? inv.getStockKg() : 0.0;
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Target Packaging Size:"));
        form.add(cmbSize);
        form.add(new JLabel("Number of Bags:"));
        form.add(txtBags);
        form.add(new JLabel("Available Loose Rice:"));
        form.add(new JLabel(String.format("%.1f kg", loose)));

        int res = JOptionPane.showConfirmDialog(this, form, "Repack Loose Rice into Bags - " + p.getProductName(), JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int count = Integer.parseInt(txtBags.getText().trim());
                String sz = (String) cmbSize.getSelectedItem();
                int kgPerBag = sz.startsWith("25") ? 25 : (sz.startsWith("20") ? 20 : (sz.startsWith("10") ? 10 : 5));
                double reqKg = count * kgPerBag;

                if (loose < reqKg) {
                    JOptionPane.showMessageDialog(this, "Insufficient loose rice. Required: " + reqKg + "kg, available: " + loose + "kg.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int p25 = sz.startsWith("25") ? count : 0;
                int p20 = sz.startsWith("20") ? count : 0;
                int p10 = sz.startsWith("10") ? count : 0;
                int p5 = sz.startsWith("5") ? count : 0;

                boolean ok = inventoryDAO.repack(p.getProductId(), p25, p20, p10, p5, "Repacked into " + sz, AuthSession.getFullName());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Successfully repacked " + count + " x " + sz + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid repack input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCorrectionDialog(Product p, Inventory inv) {
        int curSacks = inv != null ? inv.getStockSacks() : 0;
        double curLoose = inv != null ? inv.getStockKg() : 0.0;

        JTextField txtNewSacks = new JTextField(String.valueOf(curSacks));
        JTextField txtNewLoose = new JTextField(String.valueOf(curLoose));
        JTextField txtReason = new JTextField("Physical Count Audit");

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Actual 50kg Sacks:"));
        form.add(txtNewSacks);
        form.add(new JLabel("Actual Loose Rice (kg):"));
        form.add(txtNewLoose);
        form.add(new JLabel("Audit Reason:"));
        form.add(txtReason);

        int res = JOptionPane.showConfirmDialog(this, form, "Physical Stock Count - " + p.getProductName(), JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int s = Integer.parseInt(txtNewSacks.getText().trim());
                double l = Double.parseDouble(txtNewLoose.getText().trim());
                int diffSacks = s - curSacks;
                double diffLoose = l - curLoose;
                boolean ok = inventoryDAO.recordAdjustment(p.getProductId(), "Physical Count", diffSacks, 0, 0, 0, 0, diffLoose, txtReason.getText().trim(), AuthSession.getFullName());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Physical count updated successfully.", "Audited", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid count inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDamageDialog(Product p, Inventory inv) {
        JTextField txtSacks = new JTextField("0");
        JTextField txtLoose = new JTextField("0.0");
        JTextField txtReason = new JTextField("Water Damage / Pests");

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Damaged 50kg Sacks:"));
        form.add(txtSacks);
        form.add(new JLabel("Damaged Loose (kg):"));
        form.add(txtLoose);
        form.add(new JLabel("Incident Description:"));
        form.add(txtReason);

        int res = JOptionPane.showConfirmDialog(this, form, "Log Spoilage / Damage - " + p.getProductName(), JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int s = Integer.parseInt(txtSacks.getText().trim());
                double l = Double.parseDouble(txtLoose.getText().trim());
                if (s > 0 || l > 0) {
                    boolean ok = inventoryDAO.recordAdjustment(p.getProductId(), "Damage/Spoilage", -s, 0, 0, 0, 0, -l, txtReason.getText().trim(), AuthSession.getFullName());
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Spoilage recorded and deducted from inventory.", "Damage Logged", JOptionPane.INFORMATION_MESSAGE);
                        refreshData();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid damage input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
