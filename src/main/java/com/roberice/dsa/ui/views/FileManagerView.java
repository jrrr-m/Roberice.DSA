package com.roberice.dsa.ui.views;

import com.roberice.dsa.model.FileRecord;
import com.roberice.dsa.service.FileManagerService;
import com.roberice.dsa.util.Session;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManagerView extends JPanel {

    private final List<FileRecord> files = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable fileTable;

    private JTextField searchField;
    private JComboBox<String> categoryCombo;

    public FileManagerView() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_DARK);

        initComponents();
        loadFiles();
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Top Filters & Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        searchField = new JTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Search stored documents...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadFiles(); }
            @Override public void removeUpdate(DocumentEvent e) { loadFiles(); }
            @Override public void changedUpdate(DocumentEvent e) { loadFiles(); }
        });

        categoryCombo = new JComboBox<>(new String[]{"All", "Document", "Receipt", "Report", "Backup"});
        categoryCombo.addActionListener(e -> loadFiles());

        leftFilters.add(searchField);
        leftFilters.add(categoryCombo);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton uploadBtn = UIUtils.createPrimaryButton("📁 Upload Document", e -> uploadFileDialog());
        JButton backupBtn = UIUtils.createSuccessButton("💾 Backup Database", e -> backupDatabase());
        JButton deleteBtn = UIUtils.createDangerButton("🗑️ Delete", e -> deleteSelectedFile());
        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh", e -> loadFiles());

        rightActions.add(uploadBtn);
        rightActions.add(backupBtn);
        rightActions.add(deleteBtn);
        rightActions.add(refreshBtn);

        topBar.add(leftFilters, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        content.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"File ID", "Document Name", "Category", "Type", "Size", "Uploaded At", "Uploaded By", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        fileTable = UIUtils.createStyledTable(tableModel);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        fileTable.getColumnModel().getColumn(2).setCellRenderer((t, val, isSel, hasFocus, row, col) -> {
            String text = val != null ? String.valueOf(val) : "";
            Color bg, fg;
            if ("Backup".equalsIgnoreCase(text)) {
                bg = UIUtils.WARNING_BG; fg = UIUtils.WARNING_TEXT;
            } else if ("Report".equalsIgnoreCase(text)) {
                bg = UIUtils.INFO_BG; fg = UIUtils.INFO_TEXT;
            } else if ("Receipt".equalsIgnoreCase(text)) {
                bg = UIUtils.SUCCESS_BG; fg = UIUtils.SUCCESS_TEXT;
            } else {
                bg = UIUtils.PRIMARY_LIGHT; fg = UIUtils.PRIMARY;
            }
            return UIUtils.createBadge(text, bg, fg);
        });

        fileTable.getColumnModel().getColumn(3).setCellRenderer((t, val, isSel, hasFocus, row, col) -> {
            String text = val != null ? String.valueOf(val) : "";
            Color bg, fg;
            if ("SQL".equalsIgnoreCase(text)) {
                bg = UIUtils.WARNING_BG; fg = UIUtils.WARNING_TEXT;
            } else if ("CSV".equalsIgnoreCase(text)) {
                bg = UIUtils.SUCCESS_BG; fg = UIUtils.SUCCESS_TEXT;
            } else if ("PDF".equalsIgnoreCase(text)) {
                bg = UIUtils.DANGER_BG; fg = UIUtils.DANGER_TEXT;
            } else {
                bg = UIUtils.BG_CARD_ALT; fg = UIUtils.TEXT_MUTED;
            }
            return UIUtils.createBadge(text, bg, fg);
        });

        JScrollPane scroll = new JScrollPane(fileTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    public void loadFiles() {
        SwingUtilities.invokeLater(() -> {
            try {
                String cat = (String) categoryCombo.getSelectedItem();
                String search = searchField.getText().trim();

                files.clear();
                files.addAll(FileManagerService.getFiles(cat, search));

                tableModel.setRowCount(0);
                for (FileRecord f : files) {
                    tableModel.addRow(new Object[]{
                            f.getFileId(),
                            f.getOriginalName(),
                            f.getCategory(),
                            f.getFileType(),
                            f.getFormattedSize(),
                            f.getUploadedAt(),
                            f.getUploadedBy(),
                            f.getDescription()
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void uploadFileDialog() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();

            JPanel pnl = new JPanel(new GridLayout(2, 2, 8, 8));
            JComboBox<String> catBox = new JComboBox<>(new String[]{"Document", "Receipt", "Report", "Inventory"});
            JTextField descField = new JTextField("Uploaded via Roberice desktop application.");
            pnl.add(new JLabel("Category:")); pnl.add(catBox);
            pnl.add(new JLabel("Description:")); pnl.add(descField);

            int opt = JOptionPane.showConfirmDialog(this, pnl, "Document Details", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                try {
                    String user = Session.getCurrentUser() != null ? Session.getCurrentUser().getFullName() : "Admin";
                    FileManagerService.saveFile(selected, (String) catBox.getSelectedItem(), descField.getText().trim(), user);
                    UIUtils.showSuccess(this, "Document uploaded successfully.");
                    loadFiles();
                } catch (Exception ex) {
                    UIUtils.showError(this, "Failed to upload file: " + ex.getMessage());
                }
            }
        }
    }

    private void backupDatabase() {
        if (!UIUtils.showConfirm(this, "Generate a fresh MySQL database snapshot for roberice_db?", "Confirm Backup")) {
            return;
        }

        try {
            String user = Session.getCurrentUser() != null ? Session.getCurrentUser().getFullName() : "Admin";
            File backup = FileManagerService.createDatabaseBackup(user);
            UIUtils.showSuccess(this, "Database backup completed successfully!\nSaved to: " + backup.getAbsolutePath());
            loadFiles();
        } catch (Exception ex) {
            UIUtils.showError(this, "Database backup failed: " + ex.getMessage());
        }
    }

    private void deleteSelectedFile() {
        int r = fileTable.getSelectedRow();
        if (r < 0) {
            UIUtils.showError(this, "Please select a file to delete.");
            return;
        }

        int fileId = (int) tableModel.getValueAt(r, 0);
        String name = (String) tableModel.getValueAt(r, 1);

        if (UIUtils.showConfirm(this, "Permanently delete file '" + name + "'?", "Confirm Delete")) {
            try {
                FileManagerService.deleteFile(fileId);
                UIUtils.showSuccess(this, "File deleted successfully.");
                loadFiles();
            } catch (Exception ex) {
                UIUtils.showError(this, "Error deleting file: " + ex.getMessage());
            }
        }
    }
}
