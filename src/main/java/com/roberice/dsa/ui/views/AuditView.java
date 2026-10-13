package com.roberice.dsa.ui.views;

import com.roberice.dsa.model.AdjustmentLog;
import com.roberice.dsa.service.AuditService;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AuditView extends JPanel {

    private final List<AdjustmentLog> auditList = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable auditTable;

    private JTextField searchField;
    private JComboBox<String> typeCombo;

    public AuditView() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_DARK);

        initComponents();
        loadAuditLogs();
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Filters Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        searchField = new JTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Search audit timeline...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadAuditLogs(); }
            @Override public void removeUpdate(DocumentEvent e) { loadAuditLogs(); }
            @Override public void changedUpdate(DocumentEvent e) { loadAuditLogs(); }
        });

        typeCombo = new JComboBox<>(new String[]{"All", "Inventory", "Sales"});
        typeCombo.addActionListener(e -> loadAuditLogs());

        leftFilters.add(searchField);
        leftFilters.add(typeCombo);

        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh Audit", e -> loadAuditLogs());

        topBar.add(leftFilters, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        content.add(topBar, BorderLayout.NORTH);

        // Audit Table
        String[] cols = {"Reference ID", "Timestamp", "Category", "Action Event", "Product / Order", "Impact", "User / Cashier", "Audit Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        auditTable = UIUtils.createStyledTable(tableModel);
        auditTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(auditTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    public void loadAuditLogs() {
        SwingUtilities.invokeLater(() -> {
            try {
                String type = (String) typeCombo.getSelectedItem();
                String search = searchField.getText().trim();

                auditList.clear();
                auditList.addAll(AuditService.getAuditLogs(type, search, 150));

                tableModel.setRowCount(0);
                for (AdjustmentLog l : auditList) {
                    tableModel.addRow(new Object[]{
                            l.getId(),
                            l.getFormattedDate(),
                            l.getCategory(),
                            l.getLabel(),
                            l.getProductName(),
                            l.getQtyImpact(),
                            l.getUser(),
                            l.getNote()
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
