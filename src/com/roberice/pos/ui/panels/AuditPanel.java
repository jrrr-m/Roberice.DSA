package com.roberice.pos.ui.panels;

import com.roberice.pos.db.AuditDAO;
import com.roberice.pos.db.AuditDAO.AuditSummary;
import com.roberice.pos.db.AuditDAO.UnifiedAuditEntry;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Audit History & System Trail Panel.
 * Chronological audit log tracking warehouse adjustments, sack openings, and batch depletions.
 */
public class AuditPanel extends JPanel {

    private final AuditDAO auditDAO = new AuditDAO();

    // KPI metric labels
    private final JLabel lblTotalEvents = new JLabel("4,320");
    private final JLabel lblSacksOpened = new JLabel("28");
    private final JLabel lblBatchesDepleted = new JLabel("132");
    private final JLabel lblRestocks = new JLabel("166");

    // Filter controls
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cmbActionType = new JComboBox<>(new String[]{
            "All Action Types", "POS Sale", "Sack Opened", "Restock", "Repackaging", "Batch Depleted", "Count Correction", "Damage / Spoilage"
    });
    private final JComboBox<String> cmbTimeFilter = new JComboBox<>(new String[]{
            "All Time", "Today", "Last 7 Days", "Last 30 Days"
    });

    // Content container for table rows
    private final JPanel rowsContainer = new JPanel();
    private List<UnifiedAuditEntry> fullList = new ArrayList<>();

    public AuditPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel mainLayout = new JPanel();
        mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
        mainLayout.setOpaque(false);

        // 1. KPI Cards Header
        JPanel metricsPanel = createMetricsPanel();
        mainLayout.add(metricsPanel);
        mainLayout.add(Box.createVerticalStrut(16));

        // 2. Main Card Container (Table & Filters)
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 16, 16));

        // Filter Bar (Search + Action Types + Time Range)
        JPanel filterBar = createFilterBar();
        card.add(filterBar, BorderLayout.NORTH);

        // Table Panel
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
        mainLayout.add(card);

        add(mainLayout, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createMetricsPanel() {
        JPanel p = new JPanel(new GridLayout(1, 4, 14, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        p.add(createKpiCard(lblTotalEvents, "Total Audit Events", new Color(224, 242, 254), "doc"));
        p.add(createKpiCard(lblSacksOpened, "Sacks Opened to Loose", new Color(254, 243, 199), "box"));
        p.add(createKpiCard(lblBatchesDepleted, "Batches Depleted & Archived", new Color(254, 226, 226), "trash"));
        p.add(createKpiCard(lblRestocks, "Restocks & Deliveries", new Color(209, 250, 229), "arrow_up"));

        return p;
    }

    private JPanel createKpiCard(JLabel valLbl, String subtitle, Color badgeBg, String iconType) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(12, 0));

        // Left icon badge
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setStroke(new BasicStroke(1.6f));

                switch (iconType) {
                    case "doc":
                        g2.setColor(new Color(2, 132, 199)); // Blue
                        g2.drawRoundRect(cx - 5, cy - 7, 10, 14, 2, 2);
                        g2.drawLine(cx - 2, cy - 3, cx + 2, cy - 3);
                        g2.drawLine(cx - 2, cy + 1, cx + 2, cy + 1);
                        break;
                    case "box":
                        g2.setColor(new Color(180, 83, 9)); // Amber
                        g2.drawRect(cx - 6, cy - 5, 12, 11);
                        g2.drawLine(cx - 6, cy - 1, cx + 6, cy - 1);
                        g2.drawLine(cx, cy - 1, cx, cy + 6);
                        break;
                    case "trash":
                        g2.setColor(new Color(220, 38, 38)); // Red
                        g2.drawRoundRect(cx - 5, cy - 4, 10, 11, 2, 2);
                        g2.drawLine(cx - 7, cy - 5, cx + 7, cy - 5);
                        g2.drawLine(cx - 2, cy - 7, cx + 2, cy - 7);
                        g2.drawLine(cx - 2, cy - 1, cx - 2, cy + 4);
                        g2.drawLine(cx + 2, cy - 1, cx + 2, cy + 4);
                        break;
                    case "arrow_up":
                        g2.setColor(new Color(16, 120, 80)); // Green
                        g2.drawLine(cx, cy + 6, cx, cy - 5);
                        g2.drawLine(cx, cy - 5, cx - 4, cy - 1);
                        g2.drawLine(cx, cy - 5, cx + 4, cy - 1);
                        break;
                }
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(38, 38));

        JPanel badgeWrap = new JPanel(new GridBagLayout());
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        // Right values
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(valLbl);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblSub);
        textPanel.add(Box.createVerticalGlue());

        card.add(badgeWrap, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        // Search Input
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by variety, batch code, staff, note, or ID...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        JPanel searchWrap = new JPanel(new BorderLayout(8, 0));
        searchWrap.setBackground(Color.WHITE);
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 220, 210), 1),
                new EmptyBorder(0, 10, 0, 0)
        ));
        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        lblSearchIcon.setForeground(UIUtils.COLOR_TEXT_MUTED);
        txtSearch.setBorder(new EmptyBorder(8, 4, 8, 10));
        searchWrap.add(lblSearchIcon, BorderLayout.WEST);
        searchWrap.add(txtSearch, BorderLayout.CENTER);

        // Right selectors
        JPanel rightSelectors = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightSelectors.setOpaque(false);

        cmbActionType.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbActionType.setPreferredSize(new Dimension(150, 36));
        cmbActionType.setBackground(Color.WHITE);
        cmbActionType.addActionListener(e -> applyFilters());

        cmbTimeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbTimeFilter.setPreferredSize(new Dimension(110, 36));
        cmbTimeFilter.setBackground(Color.WHITE);
        cmbTimeFilter.addActionListener(e -> applyFilters());

        rightSelectors.add(cmbActionType);
        rightSelectors.add(cmbTimeFilter);

        bar.add(searchWrap, BorderLayout.CENTER);
        bar.add(rightSelectors, BorderLayout.EAST);

        return bar;
    }

    private JPanel createTableHeader() {
        JPanel header = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(242, 234, 213)); // #F2EAD5
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(8, 16, 8, 16));
        header.setPreferredSize(new Dimension(1000, 36));
        header.setMinimumSize(new Dimension(800, 36));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 4, 0, 4);
        gbc.weighty = 1.0;

        addHeaderCol(header, gbc, "TIMESTAMP", 0.16, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "ACTION TYPE", 0.12, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "ITEM / VARIETY", 0.28, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "BATCH / REF", 0.10, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "QUANTITY SHIFT", 0.12, SwingConstants.RIGHT);
        addHeaderCol(header, gbc, "REASON & DETAILS", 0.18, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "PERFORMED BY", 0.10, SwingConstants.LEFT);

        UIUtils.alignTableContentLeft(header);
        return header;
    }

    private void addHeaderCol(JPanel p, GridBagConstraints gbc, String title, double weight, int align) {
        gbc.weightx = weight;
        JLabel lbl = new JLabel(title, align);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(90, 80, 70));
        p.add(lbl, gbc);
    }

    public void refreshData() {
        try {
            AuditSummary sum = auditDAO.getAuditSummary();
            lblTotalEvents.setText(String.format("%,d", sum.totalAuditEvents));
            lblSacksOpened.setText(String.format("%,d", sum.sacksOpened));
            lblBatchesDepleted.setText(String.format("%,d", sum.batchesDepleted));
            lblRestocks.setText(String.format("%,d", sum.restocks));

            fullList = auditDAO.getUnifiedLogs(null, 500);
            applyFilters();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void applyFilters() {
        rowsContainer.removeAll();

        String kw = txtSearch.getText().trim().toLowerCase();
        String selectedType = (String) cmbActionType.getSelectedItem();
        boolean filterType = selectedType != null && !selectedType.equalsIgnoreCase("All Action Types");

        String timeSelection = (String) cmbTimeFilter.getSelectedItem();

        Calendar cal = Calendar.getInstance();
        Date now = new Date();

        int displayed = 0;
        for (UnifiedAuditEntry e : fullList) {
            // Action type filter
            if (filterType) {
                if ("POS Sale".equalsIgnoreCase(selectedType) && !"POS Sale".equalsIgnoreCase(e.getActionType())) {
                    continue;
                } else if ("Sack Opened".equalsIgnoreCase(selectedType) && !"Sack Opened".equalsIgnoreCase(e.getActionType())) {
                    continue;
                } else if ("Restock".equalsIgnoreCase(selectedType) && !"Restock".equalsIgnoreCase(e.getActionType())) {
                    continue;
                } else if ("Repackaging".equalsIgnoreCase(selectedType) && !"Repackaging".equalsIgnoreCase(e.getActionType())) {
                    continue;
                } else if ("Batch Depleted".equalsIgnoreCase(selectedType) && !"Batch Depleted".equalsIgnoreCase(e.getActionType())) {
                    continue;
                } else if ("Count Correction".equalsIgnoreCase(selectedType) && !"Count Correction".equalsIgnoreCase(e.getActionType())) {
                    continue;
                } else if ("Damage / Spoilage".equalsIgnoreCase(selectedType) && !"Damage / Spoilage".equalsIgnoreCase(e.getActionType())) {
                    continue;
                }
            }

            // Time filter
            if (e.getDate() != null && timeSelection != null && !timeSelection.equalsIgnoreCase("All Time")) {
                long diffDays = (now.getTime() - e.getDate().getTime()) / (1000 * 60 * 60 * 24);
                if ("Today".equalsIgnoreCase(timeSelection) && diffDays > 0) {
                    continue;
                } else if ("Last 7 Days".equalsIgnoreCase(timeSelection) && diffDays > 7) {
                    continue;
                } else if ("Last 30 Days".equalsIgnoreCase(timeSelection) && diffDays > 30) {
                    continue;
                }
            }

            // Keyword filter
            if (!kw.isEmpty()) {
                String id = (e.getId() != null ? e.getId() : "").toLowerCase();
                String item = (e.getItemVariety() != null ? e.getItemVariety() : "").toLowerCase();
                String batch = (e.getBatchRef() != null ? e.getBatchRef() : "").toLowerCase();
                String user = (e.getPerformedBy() != null ? e.getPerformedBy() : "").toLowerCase();
                String note = (e.getReasonDetails() != null ? e.getReasonDetails() : "").toLowerCase();
                String act = (e.getActionType() != null ? e.getActionType() : "").toLowerCase();

                if (!id.contains(kw) && !item.contains(kw) && !batch.contains(kw) && !user.contains(kw) && !note.contains(kw) && !act.contains(kw)) {
                    continue;
                }
            }

            rowsContainer.add(createRowPanel(e));
            rowsContainer.add(createRowDivider());
            displayed++;
        }

        if (displayed == 0) {
            JPanel empty = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
            empty.setOpaque(false);
            JLabel lblEmpty = new JLabel("No audit log events match current search or filters.");
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblEmpty.setForeground(UIUtils.COLOR_TEXT_MUTED);
            empty.add(lblEmpty);
            rowsContainer.add(empty);
        }

        rowsContainer.revalidate();
        rowsContainer.repaint();
    }

    private JPanel createRowPanel(UnifiedAuditEntry e) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 16, 8, 16));
        row.setPreferredSize(new Dimension(1000, 48));
        row.setMinimumSize(new Dimension(800, 48));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 4, 0, 4);
        gbc.weighty = 1.0;

        // 1. TIMESTAMP (Date + ID subtitle)
        gbc.weightx = 0.16;
        row.add(createTimestampCell(e), gbc);

        // 2. ACTION TYPE (Pill badge)
        gbc.weightx = 0.12;
        row.add(createActionTypeCell(e.getActionType()), gbc);

        // 3. ITEM / VARIETY
        gbc.weightx = 0.28;
        JLabel lblItem = new JLabel(e.getItemVariety() != null ? e.getItemVariety() : "General Stock");
        lblItem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblItem.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        lblItem.setToolTipText(e.getItemVariety());
        row.add(lblItem, gbc);

        // 4. BATCH / REF (Subtle pill or box)
        gbc.weightx = 0.10;
        row.add(createBatchRefCell(e.getBatchRef()), gbc);

        // 5. QUANTITY SHIFT
        gbc.weightx = 0.12;
        JLabel lblQty = new JLabel(e.getQuantityShift() != null ? e.getQuantityShift() : "-");
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblQty.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        lblQty.setHorizontalAlignment(SwingConstants.RIGHT);
        lblQty.setBorder(new EmptyBorder(0, 2, 0, 8));
        row.add(lblQty, gbc);

        // 6. REASON & DETAILS
        gbc.weightx = 0.18;
        String note = e.getReasonDetails() != null ? e.getReasonDetails() : "-";
        JLabel lblNote = new JLabel(note);
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblNote.setForeground(new Color(110, 105, 95));
        lblNote.setToolTipText(note);
        row.add(lblNote, gbc);

        // 7. PERFORMED BY (User silhouette icon + name)
        gbc.weightx = 0.10;
        row.add(createUserCell(e.getPerformedBy()), gbc);

        UIUtils.alignTableContentLeft(row);
        return row;
    }

    private JPanel createTimestampCell(UnifiedAuditEntry e) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 1));
        p.setOpaque(false);

        String dateStr = e.getDate() != null ?
                new SimpleDateFormat("MMM dd, yyyy hh:mm a").format(e.getDate()) : "-";
        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(new Color(70, 65, 55));

        JLabel lblId = new JLabel(e.getId() != null ? e.getId() : "-");
        lblId.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblId.setForeground(new Color(150, 145, 135));

        p.add(lblDate);
        p.add(lblId);

        return p;
    }

    private JPanel createActionTypeCell(String actionType) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);

        Color bg;
        Color fg;

        if ("POS Sale".equalsIgnoreCase(actionType)) {
            bg = new Color(209, 250, 229); // Pastel Mint Green
            fg = new Color(16, 120, 80);
        } else if ("Sack Opened".equalsIgnoreCase(actionType)) {
            bg = new Color(254, 243, 199); // Light Amber
            fg = new Color(180, 83, 9);
        } else if ("Restock".equalsIgnoreCase(actionType)) {
            bg = new Color(220, 252, 231); // Green
            fg = new Color(22, 101, 52);
        } else if ("Repackaging".equalsIgnoreCase(actionType)) {
            bg = new Color(224, 242, 254); // Cyan
            fg = new Color(2, 132, 199);
        } else if ("Batch Depleted".equalsIgnoreCase(actionType) || "Damage / Spoilage".equalsIgnoreCase(actionType)) {
            bg = new Color(254, 226, 226); // Light Red
            fg = new Color(185, 28, 28);
        } else {
            bg = new Color(243, 244, 246);
            fg = new Color(75, 85, 99);
        }

        JLabel pill = new JLabel(actionType != null ? actionType : "Event", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pill.setForeground(fg);
        pill.setBorder(new EmptyBorder(3, 10, 3, 10));
        pill.setOpaque(false);

        p.add(pill);
        return p;
    }

    private JPanel createBatchRefCell(String batchRef) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);

        JLabel pill = new JLabel(batchRef != null ? batchRef : "-", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(238, 242, 255)); // Soft blue-purple
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pill.setForeground(new Color(79, 70, 229)); // Indigo text
        pill.setBorder(new EmptyBorder(3, 8, 3, 8));
        pill.setOpaque(false);

        p.add(pill);
        return p;
    }

    private JPanel createUserCell(String performedBy) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JLabel icon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(120, 115, 105));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(3, 1, 6, 6);
                g2.drawArc(1, 8, 10, 7, 0, 180);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(14, 16));

        JLabel lblName = new JLabel(performedBy != null ? performedBy : "System");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        p.add(icon);
        p.add(lblName);

        return p;
    }

    private JComponent createRowDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(238, 234, 224));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // =========================================================================
    // Header Actions: Export CSV & Refresh Log
    // =========================================================================

    public void exportCsv() {
        try {
            File dir = new File("reports");
            if (!dir.exists()) dir.mkdirs();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File f = new File(dir, "audit_trail_" + timeStamp + ".csv");

            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                pw.println("Audit ID,Timestamp,Action Type,Item / Variety,Batch / Ref,Quantity Shift,Reason & Details,Performed By");
                for (UnifiedAuditEntry e : fullList) {
                    pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            e.getId(),
                            e.getDate() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(e.getDate()) : "",
                            e.getActionType(),
                            e.getItemVariety() != null ? e.getItemVariety().replace("\"", "\"\"") : "",
                            e.getBatchRef(),
                            e.getQuantityShift(),
                            e.getReasonDetails() != null ? e.getReasonDetails().replace("\"", "\"\"") : "",
                            e.getPerformedBy()
                    );
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Audit trail exported successfully!\nFile: " + f.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
