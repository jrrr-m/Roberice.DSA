package com.roberice.pos.ui.panels;

import com.roberice.pos.db.FileManagerDAO;
import com.roberice.pos.model.FileRecord;
import com.roberice.pos.service.AuthSession;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * File Management & One-Click Database Backup Repository (Section 2.6 in User Manual).
 */
public class FileManagerPanel extends JPanel {
    private final FileManagerDAO fileDAO = new FileManagerDAO();

    private final JComboBox<String> cmbCategory = new JComboBox<>(new String[]{"All Categories", "Database Backup", "Financial Report", "Supplier Document", "Document"});

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "File Name", "Category", "File Size", "Description", "Uploaded By", "Date"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblFiles = new JTable(tableModel);

    public FileManagerPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Store Document Repository & Database Backups");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Color.WHITE);

        actions.add(new JLabel("Category:"));
        actions.add(cmbCategory);
        cmbCategory.addActionListener(e -> refreshData());

        JButton btnBackup = UIUtils.createPrimaryButton("One-Click Database Backup");
        btnBackup.addActionListener(e -> triggerBackup());

        JButton btnUpload = UIUtils.createSecondaryButton("Upload Document");
        btnUpload.addActionListener(e -> uploadDocument());

        JButton btnDelete = UIUtils.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteSelected());

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        actions.add(btnBackup);
        actions.add(btnUpload);
        actions.add(btnDelete);
        actions.add(btnRefresh);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        UIUtils.styleTable(tblFiles);
        JScrollPane scroll = new JScrollPane(tblFiles);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
        refreshData();
    }

    public void refreshData() {
        try {
            String cat = (String) cmbCategory.getSelectedItem();
            List<FileRecord> list = fileDAO.getAllFiles(cat);
            tableModel.setRowCount(0);

            for (FileRecord f : list) {
                tableModel.addRow(new Object[]{
                        f.getFileId(),
                        f.getOriginalName(),
                        f.getCategory(),
                        f.getFormattedSize(),
                        f.getDescription() != null ? f.getDescription() : "-",
                        f.getUploadedBy(),
                        UIUtils.formatDate(f.getUploadedAt())
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void triggerBackup() {
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

    private void uploadDocument() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String desc = JOptionPane.showInputDialog(this, "Enter document description or note:", "Document Description", JOptionPane.PLAIN_MESSAGE);
            String cat = (String) JOptionPane.showInputDialog(this, "Select Document Category:", "Category",
                    JOptionPane.QUESTION_MESSAGE, null, new String[]{"Financial Report", "Supplier Document", "Document", "Audit Sheet"}, "Document");

            if (cat != null) {
                try {
                    FileRecord rec = new FileRecord();
                    rec.setFileName(f.getName());
                    rec.setOriginalName(f.getName());
                    rec.setFilePath(f.getAbsolutePath());
                    rec.setFileSize(f.length());
                    rec.setFileType("application/octet-stream");
                    rec.setCategory(cat);
                    rec.setDescription(desc != null ? desc : "");
                    rec.setUploadedBy(AuthSession.getFullName());

                    fileDAO.addFileRecord(rec);
                    JOptionPane.showMessageDialog(this, "Document registered in repository.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteSelected() {
        int row = tblFiles.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a file record from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tblFiles.getValueAt(row, 0);
        String name = (String) tblFiles.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete file record '" + name + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                fileDAO.deleteFileRecord(id);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
