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
import java.util.*;
import java.util.List;

/**
 * Modern Available Inventory Panel.
 * Matches Screenshot 1: Warm ivory card, filter bar, expandable inventory variety rows,
 * pre-packed pill badges, and FIFO delivery batch lot sub-tables with Open Sack triggers.
 */
public class ViewInventoryPanel extends JPanel {
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private List<Product> allProducts = new ArrayList<>();
    private List<Inventory> allInventories = new ArrayList<>();
    private final Map<Integer, List<InventoryBatch>> batchCache = new HashMap<>();
    private final Set<Integer> expandedProductIds = new HashSet<>();

    // Filters
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cmbOrigin = new JComboBox<>(new String[]{"All Origins", "Local", "Imported"});
    private final JComboBox<String> cmbRiceType = new JComboBox<>(new String[]{"All Rice Types", "Sinandomeng", "Dinorado", "Jasmine", "Angelika", "Well-Milled", "Glutinous / Malagkit", "Aromatic", "Brown Rice"});
    private final JComboBox<String> cmbAge = new JComboBox<>(new String[]{"All Stock Ages", "Fresh (<=30d)", "Normal (31-60d)", "Aging (>60d)"});
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"All Statuses", "In Stock", "Low Stock", "Out of Stock"});

    // Content container for inventory table rows
    private final JPanel rowsContainer = new ScrollableRowsPanel();

    private static class ScrollableRowsPanel extends JPanel implements Scrollable {
        public ScrollableRowsPanel() {
            super();
        }
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // Design Colors
    private static final Color COLOR_APP_BG = new Color(246, 243, 233);
    private static final Color COLOR_CARD_BG = new Color(255, 253, 246);
    private static final Color COLOR_CARD_BORDER = new Color(234, 230, 218);
    private static final Color COLOR_TH_BG = new Color(242, 234, 213);
    private static final Color COLOR_SUBTH_BG = new Color(243, 236, 218);
    private static final Color COLOR_SUB_ROW_BG = Color.WHITE;
    private static final Color COLOR_OPEN_SACK = new Color(178, 85, 15);
    private static final Color COLOR_OPEN_SACK_HOVER = new Color(150, 70, 10);

    public ViewInventoryPanel() {
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

        // Filter Bar (Search + Dropdowns)
        JPanel filterBar = createFilterBar();
        card.add(filterBar, BorderLayout.NORTH);

        // Table Panel (Header + Scrollable Dynamic Rows)
        JPanel tablePanel = new JPanel(new BorderLayout());
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Search variety, origin, or type...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { rebuildRows(); }
            public void removeUpdate(DocumentEvent e) { rebuildRows(); }
            public void changedUpdate(DocumentEvent e) { rebuildRows(); }
        });

        searchBox.add(lblSearchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        // Right dropdowns
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
        th.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 218, 198)),
                new EmptyBorder(8, 16, 8, 16)
        ));

        // Column weights matching design
        double[] weights = {0.16, 0.08, 0.08, 0.09, 0.07, 0.17, 0.08, 0.08, 0.08, 0.06, 0.05};
        String[] titles = {
            "RICE VARIETY", "ORIGIN & GRAIN", "SACK PRICE", "PER KG (TINATAKAL)",
            "SACKS LEFT", "PRE-PACKED (25KG / 20KG / 10KG / 5KG)",
            "LOOSE / TINATAKAL", "TOTAL VOLUME", "AVAILABLE BATCHES", "STATUS", "ACTIONS"
        };

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 4, 0, 4);
        c.weighty = 1.0;

        for (int i = 0; i < titles.length; i++) {
            c.gridx = i;
            c.weightx = weights[i];

            JLabel lbl = new JLabel(titles[i], SwingConstants.LEFT);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lbl.setForeground(new Color(70, 75, 70));
            th.add(lbl, c);
        }
        UIUtils.alignTableContentLeft(th);
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

            // Auto-expand first product by default (e.g., Angelika White Rice)
            if (expandedProductIds.isEmpty() && !allProducts.isEmpty()) {
                expandedProductIds.add(allProducts.get(0).getProductId());
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
            String status = inv != null ? inv.getStockStatus() : "Out of Stock";
            if (!"All Statuses".equals(statusFilter) && !status.equalsIgnoreCase(statusFilter)) {
                continue;
            }

            List<InventoryBatch> batches = batchCache.getOrDefault(p.getProductId(), Collections.emptyList());

            // Age filtering check
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

            // Add Variety Main Row
            JPanel varietyRow = createVarietyRow(p, inv, batches);
            rowsContainer.add(varietyRow);

            // If expanded, add FIFO Sub-table
            if (expandedProductIds.contains(p.getProductId())) {
                JPanel subTable = createBatchSubtable(p, batches);
                rowsContainer.add(subTable);
            }

            // Divider line between items
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(240, 235, 222));
            sep.setAlignmentX(Component.CENTER_ALIGNMENT);
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

    private JPanel createVarietyRow(Product p, Inventory inv, List<InventoryBatch> batches) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 16, 6, 16));
        row.setPreferredSize(new Dimension(1000, 58));
        row.setMinimumSize(new Dimension(800, 58));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        double[] weights = {0.16, 0.08, 0.08, 0.09, 0.07, 0.17, 0.08, 0.08, 0.08, 0.06, 0.05};

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 4, 0, 4);
        c.weighty = 1.0;

        // 1. Variety Name + Subtitle (Wrapped with maximum width, excess text goes to second line)
        JPanel cell1 = new JPanel();
        cell1.setLayout(new BoxLayout(cell1, BoxLayout.Y_AXIS));
        cell1.setOpaque(false);
        cell1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblName = new JLabel("<html><body style='margin:0;padding:0;'><div style='width:125px;line-height:1.15;'>"
                + p.getProductName() + "</div></body></html>");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(new Color(20, 30, 25));
        lblName.setToolTipText(p.getProductName());
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(p.getRiceType() != null ? p.getRiceType() : "Rice Variety");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(130, 140, 135));
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        cell1.add(lblName);
        cell1.add(Box.createVerticalStrut(2));
        cell1.add(lblSub);

        // 2. Origin & Grain
        JPanel cell2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cell2.setOpaque(false);
        boolean isLocal = "Local".equalsIgnoreCase(p.getOrigin());
        JLabel badgeOrigin = createBadgePill(p.getOrigin(),
                isLocal ? new Color(240, 253, 244) : new Color(224, 242, 254),
                isLocal ? new Color(22, 101, 52) : new Color(14, 116, 144));
        cell2.add(badgeOrigin);

        // 3. Sack Price
        JLabel cell3 = new JLabel(UIUtils.formatCurrency(p.getPriceSack()), SwingConstants.LEFT);
        cell3.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cell3.setForeground(new Color(20, 30, 25));

        // 4. Per Kg Price
        JLabel cell4 = new JLabel(UIUtils.formatCurrency(p.getPriceKg()), SwingConstants.LEFT);
        cell4.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cell4.setForeground(new Color(20, 30, 25));

        // 5. Sacks Left
        int sacks = inv != null ? inv.getStockSacks() : 0;
        JPanel cell5 = new JPanel();
        cell5.setLayout(new BoxLayout(cell5, BoxLayout.Y_AXIS));
        cell5.setOpaque(false);
        cell5.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSackCount = new JLabel(sacks + " sacks", SwingConstants.LEFT);
        lblSackCount.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSackCount.setForeground(new Color(20, 30, 25));
        lblSackCount.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSackSize = new JLabel("(50Kg)", SwingConstants.LEFT);
        lblSackSize.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblSackSize.setForeground(new Color(130, 140, 135));
        lblSackSize.setAlignmentX(Component.LEFT_ALIGNMENT);

        cell5.add(lblSackCount);
        cell5.add(lblSackSize);

        // 6. Pre-packed badges
        JPanel cell6 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        cell6.setOpaque(false);
        int p25 = inv != null ? inv.getPackaged25kg() : 0;
        int p20 = inv != null ? inv.getPackaged20kg() : 0;
        int p10 = inv != null ? inv.getPackaged10kg() : 0;
        int p5 = inv != null ? inv.getPackaged5kg() : 0;
        cell6.add(createMiniPill("25kg: " + p25, new Color(238, 242, 255), new Color(79, 70, 229)));
        cell6.add(createMiniPill("20kg: " + p20, new Color(248, 250, 252), new Color(100, 116, 139)));
        cell6.add(createMiniPill("10kg: " + p10, new Color(254, 243, 199), new Color(180, 83, 9)));
        cell6.add(createMiniPill("5kg: " + p5, new Color(243, 232, 255), new Color(147, 51, 234)));

        // 7. Loose / Tinatakal
        double looseKg = inv != null ? inv.getStockKg() : 0;
        JLabel cell7 = new JLabel(String.format("%.2f kg", looseKg), SwingConstants.LEFT);
        cell7.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cell7.setForeground(new Color(40, 50, 45));

        // 8. Total Volume
        double totalKg = inv != null ? ((sacks * 50.0) + (p25 * 25.0) + (p20 * 20.0) + (p10 * 10.0) + (p5 * 5.0) + looseKg) : 0;
        JLabel cell8 = new JLabel(String.format("%,.2f kg", totalKg), SwingConstants.LEFT);
        cell8.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cell8.setForeground(new Color(20, 30, 25));

        // 9. Available Batches button (toggles dropdown)
        boolean expanded = expandedProductIds.contains(p.getProductId());
        JButton btnBatches = new JButton(batches.size() + " Batches " + (expanded ? "▲" : "▼"));
        btnBatches.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnBatches.setForeground(new Color(60, 75, 65));
        btnBatches.setBackground(Color.WHITE);
        btnBatches.setFocusPainted(false);
        btnBatches.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1),
                new EmptyBorder(3, 6, 3, 6)
        ));
        btnBatches.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBatches.addActionListener(e -> {
            if (expandedProductIds.contains(p.getProductId())) {
                expandedProductIds.remove(p.getProductId());
            } else {
                expandedProductIds.add(p.getProductId());
            }
            rebuildRows();
        });

        JPanel cell9 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cell9.setOpaque(false);
        cell9.add(btnBatches);

        // 10. Status pill
        JPanel cell10 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cell10.setOpaque(false);
        boolean inStock = sacks > 0 || looseKg > 0;
        JLabel badgeStatus = createBadgePill(inStock ? "In Stock" : "Low Stock",
                inStock ? new Color(220, 252, 231) : new Color(254, 242, 242),
                inStock ? new Color(22, 101, 52) : new Color(185, 28, 28));
        cell10.add(badgeStatus);

        // 11. Actions: Open Sack button
        JPanel cell11 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cell11.setOpaque(false);
        JButton btnOpenSack = createOpenSackButton();
        btnOpenSack.addActionListener(e -> triggerOpenSack(p));
        cell11.add(btnOpenSack);

        // Add to row with grid weights
        Component[] cells = {cell1, cell2, cell3, cell4, cell5, cell6, cell7, cell8, cell9, cell10, cell11};
        for (int i = 0; i < cells.length; i++) {
            c.gridx = i;
            c.weightx = weights[i];
            row.add(cells[i], c);
        }

        UIUtils.alignTableContentLeft(row);
        return row;
    }

    private JPanel createBatchSubtable(Product p, List<InventoryBatch> batches) {
        JPanel subWrap = new JPanel(new BorderLayout(0, 4));
        subWrap.setBackground(new Color(252, 250, 244));
        subWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 225, 212)),
                new EmptyBorder(8, 16, 10, 16)
        ));
        subWrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtable header bar
        JPanel subTop = new JPanel(new BorderLayout());
        subTop.setOpaque(false);
        subTop.setBorder(new EmptyBorder(0, 4, 4, 4));

        JLabel lblTitle = new JLabel("● Available Batches for " + p.getProductName() + " (" + batches.size() + " lot(s) currently in warehouse)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(178, 85, 15));

        JLabel lblFifo = new JLabel("FIFO: Oldest batches listed first for quality rotation");
        lblFifo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFifo.setForeground(new Color(180, 83, 9));
        lblFifo.setBackground(new Color(254, 243, 199));
        lblFifo.setOpaque(true);
        lblFifo.setBorder(new EmptyBorder(2, 6, 2, 6));

        subTop.add(lblTitle, BorderLayout.WEST);
        subTop.add(lblFifo, BorderLayout.EAST);
        subWrap.add(subTop, BorderLayout.NORTH);

        double[] subWeights = {0.14, 0.10, 0.10, 0.08, 0.07, 0.07, 0.07, 0.07, 0.09, 0.11, 0.10};
        String[] subCols = {"BATCH CODE", "DATE RECEIVED", "STOCK AGE", "50KG SACKS", "25KG HALF", "20KG SACKS", "10KG PACKS", "5KG PACKS", "LOOSE KG", "BATCH VOLUME", "ACTIONS"};

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 4, 0, 4);
        c.weighty = 1.0;

        // Header Row
        JPanel subTh = new JPanel(new GridBagLayout());
        subTh.setBackground(COLOR_SUBTH_BG);
        subTh.setPreferredSize(new Dimension(900, 28));
        subTh.setMinimumSize(new Dimension(800, 28));
        subTh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        subTh.setBorder(new EmptyBorder(2, 4, 2, 4));

        for (int i = 0; i < subCols.length; i++) {
            c.gridx = i;
            c.weightx = subWeights[i];
            JLabel lbl = new JLabel(subCols[i], SwingConstants.LEFT);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 8));
            lbl.setForeground(new Color(90, 95, 90));
            subTh.add(lbl, c);
        }
        UIUtils.alignTableContentLeft(subTh);

        JPanel subRowsContainer = new JPanel();
        subRowsContainer.setLayout(new BoxLayout(subRowsContainer, BoxLayout.Y_AXIS));
        subRowsContainer.setOpaque(false);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        for (InventoryBatch b : batches) {
            JPanel bRow = new JPanel(new GridBagLayout());
            bRow.setBackground(COLOR_SUB_ROW_BG);
            bRow.setPreferredSize(new Dimension(900, 32));
            bRow.setMinimumSize(new Dimension(800, 32));
            bRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            bRow.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 235, 225)),
                    new EmptyBorder(2, 4, 2, 4)
            ));

            long ageDays = b.getAgeInDays();
            String ageText = ageDays + "d ";
            Color ageBg, ageFg;
            if (ageDays <= 30) {
                ageText += "(Fresh)";
                ageBg = new Color(220, 252, 231);
                ageFg = new Color(22, 101, 52);
            } else if (ageDays <= 60) {
                ageText += "(Normal)";
                ageBg = new Color(224, 242, 254);
                ageFg = new Color(14, 116, 144);
            } else {
                ageText += "(Aging)";
                ageBg = new Color(254, 243, 199);
                ageFg = new Color(180, 83, 9);
            }

            JPanel pCode = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pCode.setOpaque(false);
            pCode.add(createBadgePill(b.getBatchCode(), new Color(238, 242, 255), new Color(79, 70, 229)));

            JLabel lblDate = new JLabel(b.getReceivedDate() != null ? df.format(b.getReceivedDate()) : "-", SwingConstants.LEFT);
            lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            JPanel pAge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pAge.setOpaque(false);
            pAge.add(createBadgePill(ageText, ageBg, ageFg));

            JLabel lbl50k = new JLabel(b.getStockSacks() + " whole", SwingConstants.LEFT);
            lbl50k.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            JLabel lbl25 = new JLabel(String.valueOf(b.getPackaged25kg()), SwingConstants.LEFT);
            lbl25.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            JLabel lbl20 = new JLabel(String.valueOf(b.getPackaged20kg()), SwingConstants.LEFT);
            lbl20.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            JLabel lbl10 = new JLabel(String.valueOf(b.getPackaged10kg()), SwingConstants.LEFT);
            lbl10.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            JLabel lbl5 = new JLabel(String.valueOf(b.getPackaged5kg()), SwingConstants.LEFT);
            lbl5.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            JLabel lblLoose = new JLabel(String.format("%.2f kg", b.getStockKg()), SwingConstants.LEFT);
            lblLoose.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            JLabel lblVol = new JLabel(String.format("%,.2f kg", b.getTotalKg()), SwingConstants.LEFT);
            lblVol.setFont(new Font("Segoe UI", Font.BOLD, 10));

            JButton btnBatchOpen = createOpenSackButton();
            btnBatchOpen.addActionListener(e -> triggerOpenSackBatch(p, b));
            JPanel pAct = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pAct.setOpaque(false);
            pAct.add(btnBatchOpen);

            Component[] bCells = {pCode, lblDate, pAge, lbl50k, lbl25, lbl20, lbl10, lbl5, lblLoose, lblVol, pAct};
            for (int i = 0; i < bCells.length; i++) {
                c.gridx = i;
                c.weightx = subWeights[i];
                bRow.add(bCells[i], c);
            }
            UIUtils.alignTableContentLeft(bRow);
            subRowsContainer.add(bRow);
        }

        JPanel subTableContent = new JPanel(new BorderLayout());
        subTableContent.add(subTh, BorderLayout.NORTH);
        subTableContent.add(subRowsContainer, BorderLayout.CENTER);
        subWrap.add(subTableContent, BorderLayout.CENTER);

        return subWrap;
    }

    private JButton createOpenSackButton() {
        JButton btn = new JButton("Open Sack") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(COLOR_OPEN_SACK_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(COLOR_OPEN_SACK_HOVER);
                } else {
                    g2.setColor(COLOR_OPEN_SACK);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(72, 24));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createBadgePill(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(1, 6, 1, 6));
        return lbl;
    }

    private JLabel createMiniPill(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 9));
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
                        JOptionPane.showMessageDialog(this, "Insufficient whole sacks in stock to open.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void triggerOpenSackBatch(Product p, InventoryBatch b) {
        String input = JOptionPane.showInputDialog(this,
                "Enter number of 50kg sacks to open from Batch " + b.getBatchCode() + ":",
                "Open Sack (Batch Lot " + b.getBatchCode() + ")", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int sacks = Integer.parseInt(input.trim());
                if (sacks > 0) {
                    boolean ok = inventoryDAO.openSack(p.getProductId(), b.getBatchCode(), sacks, AuthSession.getFullName());
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Successfully opened " + sacks + " sack(s) from " + b.getBatchCode() + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Insufficient stock in this batch lot.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
