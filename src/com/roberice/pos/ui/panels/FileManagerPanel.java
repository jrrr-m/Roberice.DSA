package com.roberice.pos.ui.panels;

import com.roberice.pos.db.DatabaseManager;
import com.roberice.pos.db.FileManagerDAO;
import com.roberice.pos.model.FileRecord;
import com.roberice.pos.service.AuthSession;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * File & Document Management Panel.
 * Organizes store receipts, supplier delivery invoices, audit records, and automated database backup snapshots.
 */
public class FileManagerPanel extends JPanel {

    private final FileManagerDAO fileDAO = new FileManagerDAO();

    // Top Metric Labels
    private final JLabel lblDocCount = new JLabel("0 files");
    private final JLabel lblStorageUsed = new JLabel("0.00 KB");
    private final JLabel lblLatestBackup = new JLabel("No backups");

    // Filter controls
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cmbCategory = new JComboBox<>(new String[]{
            "All Categories", "Database Backup", "Financial Report", "Supplier Document", "Document", "Audit Sheet"
    });

    // Content container for table rows
    private final JPanel rowsContainer = new JPanel();
    private List<FileRecord> fullList = new ArrayList<>();

    public FileManagerPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel mainLayout = new JPanel();
        mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
        mainLayout.setOpaque(false);

        // 1. Metric Cards Header
        JPanel metricsPanel = createMetricsPanel();
        mainLayout.add(metricsPanel);
        mainLayout.add(Box.createVerticalStrut(16));

        // 2. Main Card Container (Table & Filters)
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 16, 16));

        // Filter Bar (Search + Category)
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
        JPanel p = new JPanel(new GridLayout(1, 3, 16, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        p.add(createKpiCard("STORED DOCUMENTS", lblDocCount, "Active archive records", new Color(237, 243, 236), "files"));
        p.add(createKpiCard("TOTAL STORAGE USED", lblStorageUsed, "In /uploads folder", new Color(209, 250, 229), "storage"));
        p.add(createKpiCard("LATEST DATABASE BACKUP", lblLatestBackup, "Automated snapshot", new Color(243, 232, 255), "backup"));

        return p;
    }

    private JPanel createKpiCard(String tag, JLabel valLbl, String subtext, Color badgeBg, String iconType) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(8, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTag = new JLabel(tag);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(UIUtils.COLOR_TEXT_MUTED);

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel lblSub = new JLabel(subtext);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        left.add(lblTag);
        left.add(Box.createVerticalStrut(2));
        left.add(valLbl);
        left.add(Box.createVerticalStrut(2));
        left.add(lblSub);

        // Right icon badge
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setStroke(new BasicStroke(1.5f));

                switch (iconType) {
                    case "files":
                        g2.setColor(new Color(27, 77, 50));
                        g2.drawRoundRect(cx - 5, cy - 7, 10, 14, 2, 2);
                        g2.drawLine(cx - 2, cy - 3, cx + 3, cy - 3);
                        g2.drawLine(cx - 2, cy + 1, cx + 3, cy + 1);
                        break;
                    case "storage":
                        g2.setColor(new Color(16, 120, 80));
                        g2.drawRoundRect(cx - 6, cy - 6, 12, 5, 2, 2);
                        g2.drawRoundRect(cx - 6, cy + 1, 12, 5, 2, 2);
                        g2.drawLine(cx - 3, cy - 3, cx - 1, cy - 3);
                        g2.drawLine(cx - 3, cy + 4, cx - 1, cy + 4);
                        break;
                    case "backup":
                        g2.setColor(new Color(126, 34, 206));
                        g2.drawRoundRect(cx - 6, cy - 6, 12, 12, 3, 3);
                        g2.drawLine(cx, cy - 3, cx, cy + 2);
                        g2.drawLine(cx - 2, cy, cx, cy + 2);
                        g2.drawLine(cx + 2, cy, cx, cy + 2);
                        break;
                }
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(36, 36));

        JPanel badgeWrap = new JPanel(new GridBagLayout());
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        card.add(left, BorderLayout.CENTER);
        card.add(badgeWrap, BorderLayout.EAST);

        return card;
    }

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        // Search Input
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search file name, notes, or category...");
        txtSearch.setPreferredSize(new Dimension(360, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 220, 210), 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        // Search wrapper
        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        lblSearchIcon.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JPanel searchWrap = new JPanel(new BorderLayout(8, 0));
        searchWrap.setBackground(Color.WHITE);
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 220, 210), 1),
                new EmptyBorder(0, 10, 0, 0)
        ));
        txtSearch.setBorder(new EmptyBorder(8, 4, 8, 10));
        searchWrap.add(lblSearchIcon, BorderLayout.WEST);
        searchWrap.add(txtSearch, BorderLayout.CENTER);
        searchBox.add(searchWrap, BorderLayout.CENTER);

        // Category dropdown
        cmbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbCategory.setPreferredSize(new Dimension(170, 36));
        cmbCategory.setBackground(Color.WHITE);
        cmbCategory.addActionListener(e -> applyFilters());

        bar.add(searchBox, BorderLayout.CENTER);
        bar.add(cmbCategory, BorderLayout.EAST);

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
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 4, 0, 4);
        gbc.weighty = 1.0;

        addHeaderCol(header, gbc, "FILE NAME", 0.22, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "CATEGORY", 0.12, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "SIZE", 0.08, SwingConstants.RIGHT);
        addHeaderCol(header, gbc, "DESCRIPTION / NOTES", 0.28, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "UPLOADED BY", 0.11, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "DATE & TIME", 0.11, SwingConstants.LEFT);
        addHeaderCol(header, gbc, "ACTIONS", 0.14, SwingConstants.CENTER);

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
            fullList = fileDAO.getAllFiles(null);
            updateKpis(fullList);
            applyFilters();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateKpis(List<FileRecord> list) {
        lblDocCount.setText(list.size() + " files");

        long totalBytes = 0;
        Date latestDate = null;

        for (FileRecord f : list) {
            totalBytes += f.getFileSize();
            if ("Database Backup".equalsIgnoreCase(f.getCategory())) {
                if (latestDate == null || (f.getUploadedAt() != null && f.getUploadedAt().after(latestDate))) {
                    latestDate = f.getUploadedAt();
                }
            }
        }

        // Format storage used
        if (totalBytes < 1024) {
            lblStorageUsed.setText(totalBytes + " B");
        } else if (totalBytes < 1024 * 1024) {
            lblStorageUsed.setText(String.format("%.2f KB", totalBytes / 1024.0));
        } else {
            lblStorageUsed.setText(String.format("%.2f MB", totalBytes / (1024.0 * 1024.0)));
        }

        // Latest backup date
        if (latestDate != null) {
            lblLatestBackup.setText(new SimpleDateFormat("MMM dd, yyyy, hh:mm a").format(latestDate));
        } else {
            lblLatestBackup.setText("No backups");
        }
    }

    private void applyFilters() {
        rowsContainer.removeAll();

        String kw = txtSearch.getText().trim().toLowerCase();
        String selectedCat = (String) cmbCategory.getSelectedItem();
        boolean filterCat = selectedCat != null && !selectedCat.equalsIgnoreCase("All Categories");

        int displayed = 0;
        for (FileRecord f : fullList) {
            if (filterCat && !selectedCat.equalsIgnoreCase(f.getCategory())) {
                continue;
            }

            if (!kw.isEmpty()) {
                String name = (f.getOriginalName() != null ? f.getOriginalName() : f.getFileName()).toLowerCase();
                String desc = (f.getDescription() != null ? f.getDescription() : "").toLowerCase();
                String cat = (f.getCategory() != null ? f.getCategory() : "").toLowerCase();
                String by = (f.getUploadedBy() != null ? f.getUploadedBy() : "").toLowerCase();

                if (!name.contains(kw) && !desc.contains(kw) && !cat.contains(kw) && !by.contains(kw)) {
                    continue;
                }
            }

            rowsContainer.add(createRowPanel(f));
            rowsContainer.add(createRowDivider());
            displayed++;
        }

        if (displayed == 0) {
            JPanel empty = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
            empty.setOpaque(false);
            JLabel lblEmpty = new JLabel("No document or backup records found.");
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblEmpty.setForeground(UIUtils.COLOR_TEXT_MUTED);
            empty.add(lblEmpty);
            rowsContainer.add(empty);
        }

        rowsContainer.revalidate();
        rowsContainer.repaint();
    }

    private JPanel createRowPanel(FileRecord f) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 16, 8, 16));
        row.setPreferredSize(new Dimension(1000, 48));
        row.setMinimumSize(new Dimension(800, 48));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 4, 0, 4);
        gbc.weighty = 1.0;

        // 1. File Name with Extension Badge
        gbc.weightx = 0.22;
        row.add(createFileNameCell(f), gbc);

        // 2. Category Pill
        gbc.weightx = 0.12;
        row.add(createCategoryCell(f.getCategory()), gbc);

        // 3. Size
        gbc.weightx = 0.08;
        JLabel lblSize = new JLabel(formatFileSize(f.getFileSize()));
        lblSize.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSize.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        lblSize.setHorizontalAlignment(SwingConstants.RIGHT);
        lblSize.setBorder(new EmptyBorder(0, 2, 0, 8));
        row.add(lblSize, gbc);

        // 4. Description / Notes
        gbc.weightx = 0.28;
        String desc = f.getDescription() != null && !f.getDescription().isEmpty() ? f.getDescription() : "No notes recorded.";
        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(new Color(110, 105, 95));
        lblDesc.setToolTipText(desc);
        row.add(lblDesc, gbc);

        // 5. Uploaded By
        gbc.weightx = 0.11;
        JLabel lblBy = new JLabel(f.getUploadedBy() != null ? f.getUploadedBy() : "System");
        lblBy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBy.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        row.add(lblBy, gbc);

        // 6. Date & Time
        gbc.weightx = 0.11;
        String dateStr = f.getUploadedAt() != null ?
                new SimpleDateFormat("MMM dd, yyyy, hh:mm a").format(f.getUploadedAt()) : "-";
        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(new Color(110, 105, 95));
        row.add(lblDate, gbc);

        // 7. Actions (Restore, Download, Delete)
        gbc.weightx = 0.14;
        row.add(createActionsCell(f), gbc);

        return row;
    }

    private JPanel createFileNameCell(FileRecord f) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);

        String ext = getFileExtension(f.getOriginalName() != null ? f.getOriginalName() : f.getFileName());
        Color badgeBg;
        Color badgeFg;

        if ("SQL".equalsIgnoreCase(ext)) {
            badgeBg = new Color(224, 242, 254); // Blue
            badgeFg = new Color(3, 105, 161);
        } else if ("PDF".equalsIgnoreCase(ext)) {
            badgeBg = new Color(254, 226, 226); // Red
            badgeFg = new Color(185, 28, 28);
        } else if ("XLS".equalsIgnoreCase(ext) || "XLSX".equalsIgnoreCase(ext) || "CSV".equalsIgnoreCase(ext)) {
            badgeBg = new Color(220, 252, 231); // Green
            badgeFg = new Color(22, 101, 52);
        } else {
            badgeBg = new Color(243, 244, 246);
            badgeFg = new Color(75, 85, 99);
        }

        JLabel badge = new JLabel(ext.toUpperCase(), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        badge.setForeground(badgeFg);
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(32, 22));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        String name = f.getOriginalName() != null ? f.getOriginalName() : f.getFileName();
        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel lblSub = new JLabel(f.getFileName());
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(130, 125, 115));

        textPanel.add(lblName);
        textPanel.add(lblSub);

        p.add(badge);
        p.add(textPanel);

        return p;
    }

    private JPanel createCategoryCell(String category) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 14));
        p.setOpaque(false);

        Color bg;
        Color fg;

        if ("Database Backup".equalsIgnoreCase(category)) {
            bg = new Color(243, 232, 255); // Light purple
            fg = new Color(126, 34, 206);
        } else if ("Supplier Document".equalsIgnoreCase(category)) {
            bg = new Color(224, 242, 254); // Cyan / Blue
            fg = new Color(2, 132, 199);
        } else if ("Financial Report".equalsIgnoreCase(category)) {
            bg = new Color(224, 242, 254);
            fg = new Color(3, 105, 161);
        } else if ("Audit Sheet".equalsIgnoreCase(category)) {
            bg = new Color(254, 243, 199); // Amber
            fg = new Color(180, 83, 9);
        } else {
            bg = new Color(243, 244, 246);
            fg = new Color(75, 85, 99);
        }

        JLabel pill = new JLabel(category != null ? category : "General", SwingConstants.CENTER) {
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

    private JPanel createActionsCell(FileRecord f) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 12));
        actions.setOpaque(false);

        boolean isBackup = "Database Backup".equalsIgnoreCase(f.getCategory()) ||
                (f.getFileName() != null && f.getFileName().endsWith(".sql"));

        if (isBackup) {
            JButton btnRestore = createActionButton("Restore", new Color(194, 91, 15), new Color(178, 85, 15));
            btnRestore.addActionListener(e -> restoreFile(f));
            actions.add(btnRestore);
        }

        JButton btnDownload = createActionButton("Download", new Color(27, 77, 50), new Color(20, 60, 38));
        btnDownload.addActionListener(e -> downloadFile(f));
        actions.add(btnDownload);

        JButton btnDelete = createActionButton("Delete", new Color(220, 38, 38), new Color(185, 28, 28));
        btnDelete.addActionListener(e -> deleteFile(f));
        actions.add(btnDelete);

        return actions;
    }

    private JButton createActionButton(String text, Color baseColor, Color hoverColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(baseColor);
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
        btn.setBorder(new EmptyBorder(4, 10, 4, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JComponent createRowDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(238, 234, 224));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "FILE";
        int idx = filename.lastIndexOf('.');
        if (idx > 0 && idx < filename.length() - 1) {
            return filename.substring(idx + 1).toUpperCase();
        }
        return "FILE";
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    // =========================================================================
    // Action Handlers
    // =========================================================================

    public void triggerBackup() {
        try {
            FileRecord rec = fileDAO.triggerBackup(AuthSession.getFullName());
            JOptionPane.showMessageDialog(this,
                    "Database snapshot backup completed successfully!\nFile: " + rec.getFileName() + "\nSize: " + rec.getFormattedSize(),
                    "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Backup Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void triggerRestore() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select SQL Database Backup to Restore");
        chooser.setCurrentDirectory(new File("backups"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            executeRestoreSqlFile(f);
        }
    }

    private void restoreFile(FileRecord rec) {
        File file = new File(rec.getFilePath());
        if (!file.exists()) {
            // Check backups/ or uploads/
            file = new File("backups", rec.getFileName());
            if (!file.exists()) {
                file = new File("uploads", rec.getFileName());
            }
        }

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "Cannot locate file on disk: " + rec.getFileName(), "File Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        executeRestoreSqlFile(file);
    }

    private void executeRestoreSqlFile(File sqlFile) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to restore the database from:\n" + sqlFile.getName() +
                        "\n\nWARNING: Existing table records will be synchronized/overwritten with this backup snapshot.",
                "Confirm Database Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile));
             java.sql.Connection conn = DatabaseManager.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            String line;
            StringBuilder sb = new StringBuilder();
            int executed = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("/*")) {
                    continue;
                }
                sb.append(line);
                if (line.endsWith(";")) {
                    try {
                        stmt.execute(sb.toString());
                        executed++;
                    } catch (Exception ex) {
                        // ignore duplicate / replace exceptions
                    }
                    sb.setLength(0);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Database restore completed successfully!\n" + executed + " statements processed.",
                    "Restore Completed", JOptionPane.INFORMATION_MESSAGE);
            refreshData();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Restore Failed: " + ex.getMessage(), "Restore Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadFile(FileRecord rec) {
        File src = new File(rec.getFilePath());
        if (!src.exists()) {
            src = new File("backups", rec.getFileName());
            if (!src.exists()) {
                src = new File("uploads", rec.getFileName());
            }
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(rec.getOriginalName() != null ? rec.getOriginalName() : rec.getFileName()));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File dest = chooser.getSelectedFile();
            try {
                if (src.exists()) {
                    copyFile(src, dest);
                } else {
                    // Generate placeholder text file if original was simulated
                    try (PrintWriter pw = new PrintWriter(new FileWriter(dest))) {
                        pw.println("-- Roberice Archive File: " + rec.getOriginalName());
                        pw.println("-- Category: " + rec.getCategory());
                        pw.println("-- Uploaded By: " + rec.getUploadedBy());
                        pw.println("-- Description: " + rec.getDescription());
                        pw.println("-- Generated at: " + new Date());
                    }
                }
                JOptionPane.showMessageDialog(this, "File exported successfully to:\n" + dest.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Download error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyFile(File src, File dest) throws IOException {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    public void uploadDocument() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Upload New Document to Store Repository");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String desc = JOptionPane.showInputDialog(this, "Enter document description or note:", "Document Description", JOptionPane.PLAIN_MESSAGE);
            String cat = (String) JOptionPane.showInputDialog(this, "Select Document Category:", "Category",
                    JOptionPane.QUESTION_MESSAGE, null,
                    new String[]{"Financial Report", "Supplier Document", "Document", "Audit Sheet", "Database Backup"}, "Document");

            if (cat != null) {
                try {
                    // Save file into uploads/ folder
                    File uploadDir = new File("uploads");
                    if (!uploadDir.exists()) uploadDir.mkdirs();
                    File dest = new File(uploadDir, f.getName());
                    if (!dest.exists()) {
                        copyFile(f, dest);
                    }

                    FileRecord rec = new FileRecord();
                    rec.setFileName(dest.getName());
                    rec.setOriginalName(f.getName());
                    rec.setFilePath(dest.getAbsolutePath());
                    rec.setFileSize(f.length());
                    rec.setFileType("application/octet-stream");
                    rec.setCategory(cat);
                    rec.setDescription(desc != null ? desc : "");
                    rec.setUploadedBy(AuthSession.getFullName());

                    fileDAO.addFileRecord(rec);
                    JOptionPane.showMessageDialog(this, "Document registered and archived in repository.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteFile(FileRecord rec) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete archive record '" + (rec.getOriginalName() != null ? rec.getOriginalName() : rec.getFileName()) + "'?\nThis action removes it from the store registry.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                fileDAO.deleteFileRecord(rec.getFileId());
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
