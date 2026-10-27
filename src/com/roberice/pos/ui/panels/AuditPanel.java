package com.roberice.pos.ui.panels;

import com.roberice.pos.db.AuditDAO;
import com.roberice.pos.model.InventoryAdjustment;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * System Audit Trail & Compliance Log Panel (Section 2.7 in User Manual).
 */
public class AuditPanel extends JPanel {
    private final AuditDAO auditDAO = new AuditDAO();

    private final JComboBox<String> cmbType = new JComboBox<>(new String[]{"All Events", "restock", "open_sack", "repack", "correction", "spoilage", "batch_depleted"});

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Date & Time", "Event Type", "Rice Variety", "Batch Code", "Sacks", "Volume (Kg)", "Auditor Note / Description", "Performed By"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblAudit = new JTable(tableModel);

    public AuditPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Warehouse Adjustments & Compliance Audit Trail");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Color.WHITE);

        actions.add(new JLabel("Event Type:"));
        actions.add(cmbType);
        cmbType.addActionListener(e -> refreshData());

        JButton btnExport = UIUtils.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> exportCsv());

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh Log");
        btnRefresh.addActionListener(e -> refreshData());

        actions.add(btnExport);
        actions.add(btnRefresh);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        UIUtils.styleTable(tblAudit);
        JScrollPane scroll = new JScrollPane(tblAudit);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
        refreshData();
    }

    public void refreshData() {
        try {
            String filter = (String) cmbType.getSelectedItem();
            if ("All Events".equals(filter)) filter = "all";

            List<InventoryAdjustment> list = auditDAO.getAuditLogs(filter, 300);
            tableModel.setRowCount(0);

            for (InventoryAdjustment a : list) {
                tableModel.addRow(new Object[]{
                        a.getAdjustmentId(),
                        UIUtils.formatDate(a.getAdjustmentDate()),
                        formatType(a.getAdjustmentType()),
                        a.getProductName(),
                        a.getBatchCode() != null ? a.getBatchCode() : "-",
                        a.getSacks(),
                        String.format("%.2f", a.getKg()),
                        a.getNote(),
                        a.getPerformedBy()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String formatType(String t) {
        if (t == null) return "-";
        switch (t.toLowerCase()) {
            case "restock": return "Restock";
            case "open_sack": return "Sack Opening";
            case "repack": return "Repack Bags";
            case "correction": return "Physical Count";
            case "spoilage": return "Damage / Spoilage";
            case "batch_depleted": return "Batch Depleted";
            default: return t;
        }
    }

    private void exportCsv() {
        try {
            File dir = new File("reports");
            if (!dir.exists()) dir.mkdirs();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            File f = new File(dir, "audit_log_" + timeStamp + ".csv");

            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                pw.println("Audit ID,Date & Time,Event Type,Rice Variety,Batch Code,Sacks,Volume (Kg),Description,Performed By");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    pw.printf("%s,\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,\"%s\",\"%s\"\n",
                            tableModel.getValueAt(i, 0),
                            tableModel.getValueAt(i, 1),
                            tableModel.getValueAt(i, 2),
                            tableModel.getValueAt(i, 3),
                            tableModel.getValueAt(i, 4),
                            tableModel.getValueAt(i, 5),
                            tableModel.getValueAt(i, 6),
                            tableModel.getValueAt(i, 7),
                            tableModel.getValueAt(i, 8)
                    );
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Audit log exported successfully!\nFile: " + f.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
