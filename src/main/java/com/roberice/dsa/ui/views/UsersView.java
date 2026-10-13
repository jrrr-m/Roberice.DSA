package com.roberice.dsa.ui.views;

import com.roberice.dsa.model.User;
import com.roberice.dsa.service.UserService;
import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsersView extends JPanel {

    private final List<User> userList = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable userTable;

    public UsersView() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_DARK);

        initComponents();
        loadUsers();
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Header / Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel titleLbl = new JLabel("Staff & Cashier User Accounts");
        titleLbl.setFont(UIUtils.FONT_SUBHEADER);
        titleLbl.setForeground(UIUtils.TEXT_PRIMARY);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton addBtn = UIUtils.createPrimaryButton("+ Create New User", e -> showUserDialog(null));
        JButton editBtn = UIUtils.createSecondaryButton("✏️ Edit Selected", e -> editSelectedUser());
        JButton deleteBtn = UIUtils.createDangerButton("🗑️ Deactivate", e -> deactivateSelectedUser());
        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh", e -> loadUsers());

        rightActions.add(addBtn);
        rightActions.add(editBtn);
        rightActions.add(deleteBtn);
        rightActions.add(refreshBtn);

        topBar.add(titleLbl, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        content.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"User ID", "Username", "Full Name", "Role", "Status", "Sales Count", "Granted Permissions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        userTable = UIUtils.createStyledTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(userTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    public void loadUsers() {
        SwingUtilities.invokeLater(() -> {
            try {
                userList.clear();
                userList.addAll(UserService.getAllUsers());

                tableModel.setRowCount(0);
                for (User u : userList) {
                    tableModel.addRow(new Object[]{
                            u.getUserId(),
                            u.getUsername(),
                            u.getFullName(),
                            u.getRole().toUpperCase(),
                            u.getAccountStatus(),
                            u.getTransactionCount(),
                            String.join(", ", u.getPermissions())
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private User getSelectedUser() {
        int r = userTable.getSelectedRow();
        if (r < 0) return null;
        int uId = (int) tableModel.getValueAt(r, 0);
        for (User u : userList) {
            if (u.getUserId() == uId) return u;
        }
        return null;
    }

    private void editSelectedUser() {
        User u = getSelectedUser();
        if (u == null) {
            UIUtils.showError(this, "Please select a user from the table first.");
            return;
        }
        showUserDialog(u);
    }

    private void deactivateSelectedUser() {
        User u = getSelectedUser();
        if (u == null) {
            UIUtils.showError(this, "Please select a user from the table first.");
            return;
        }

        if (UIUtils.showConfirm(this, "Deactivate user account '" + u.getUsername() + "'?", "Confirm Deactivation")) {
            try {
                UserService.deleteUser(u.getUserId());
                UIUtils.showSuccess(this, "User processed successfully.");
                loadUsers();
            } catch (Exception ex) {
                UIUtils.showError(this, ex.getMessage());
                loadUsers();
            }
        }
    }

    private void showUserDialog(User existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit User Account" : "Create New User Account", true);
        dlg.setSize(520, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 12));
        form.setBackground(UIUtils.BG_CARD);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));

        JTextField userField = new JTextField(isEdit ? existing.getUsername() : "");
        JPasswordField passField = new JPasswordField();
        JTextField nameField = new JTextField(isEdit ? existing.getFullName() : "");
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Admin", "Cashier"});
        if (isEdit && existing.getRole() != null) roleCombo.setSelectedItem(existing.getRole());

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        if (isEdit && existing.getAccountStatus() != null) statusCombo.setSelectedItem(existing.getAccountStatus());

        form.add(new JLabel("Username:")); form.add(userField);
        form.add(new JLabel(isEdit ? "New Password (blank to keep):" : "Password:")); form.add(passField);
        form.add(new JLabel("Full Name:")); form.add(nameField);
        form.add(new JLabel("Role:")); form.add(roleCombo);
        form.add(new JLabel("Status:")); form.add(statusCombo);

        // Permissions Checkboxes
        JPanel permsPanel = new JPanel(new GridLayout(5, 2, 4, 4));
        permsPanel.setBackground(UIUtils.BG_CARD);
        permsPanel.setBorder(BorderFactory.createTitledBorder("Module Permissions"));

        String[] allPerms = {"dashboard", "pos", "view-inventory", "inventory", "products", "transactions", "sales-report", "file-manager", "audit", "users"};
        List<JCheckBox> boxes = new ArrayList<>();
        for (String perm : allPerms) {
            JCheckBox box = new JCheckBox(perm);
            box.setOpaque(false);
            if (isEdit && existing.hasPermission(perm)) {
                box.setSelected(true);
            } else if (!isEdit) {
                box.setSelected(true);
            }
            boxes.add(box);
            permsPanel.add(box);
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(form, BorderLayout.NORTH);
        centerPanel.add(permsPanel, BorderLayout.CENTER);

        JButton saveBtn = UIUtils.createPrimaryButton(isEdit ? "Update User" : "Create Account", e -> {
            try {
                String un = userField.getText().trim();
                String pw = new String(passField.getPassword()).trim();
                String fn = nameField.getText().trim();
                String role = (String) roleCombo.getSelectedItem();
                String status = (String) statusCombo.getSelectedItem();

                List<String> chosenPerms = new ArrayList<>();
                for (JCheckBox cb : boxes) {
                    if (cb.isSelected()) chosenPerms.add(cb.getText());
                }

                if (isEdit) {
                    UserService.updateUser(existing.getUserId(), un, pw, fn, role, status, chosenPerms);
                    UIUtils.showSuccess(dlg, "User updated successfully.");
                } else {
                    UserService.createUser(un, pw, fn, role, status, chosenPerms);
                    UIUtils.showSuccess(dlg, "User created successfully.");
                }
                dlg.dispose();
                loadUsers();
            } catch (Exception ex) {
                UIUtils.showError(dlg, "Error saving user: " + ex.getMessage());
            }
        });

        dlg.add(centerPanel, BorderLayout.CENTER);
        dlg.add(saveBtn, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
