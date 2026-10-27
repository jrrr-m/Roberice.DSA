package com.roberice.pos.ui.panels;

import com.roberice.pos.db.UserDAO;
import com.roberice.pos.model.AccountRequest;
import com.roberice.pos.model.User;
import com.roberice.pos.service.AuthSession;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User Management & Granular Permissions Matrix Panel (Section 2.8 in User Manual).
 */
public class UsersPanel extends JPanel {
    private final UserDAO userDAO = new UserDAO();

    private final JTabbedPane tabbedPane = new JTabbedPane();

    // Users Table
    private final DefaultTableModel userModel = new DefaultTableModel(
            new String[]{"ID", "Username", "Full Name", "Email", "Role", "Status", "Permissions"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblUsers = new JTable(userModel);

    // Account Requests Table
    private final DefaultTableModel reqModel = new DefaultTableModel(
            new String[]{"Req ID", "Full Name", "Username", "Email", "Requested Role", "Status", "Rejection Reason", "Date"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblRequests = new JTable(reqModel);

    public UsersPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // TAB 1: User Accounts
        JPanel usersTab = new JPanel(new BorderLayout(0, 14));
        usersTab.setBackground(Color.WHITE);
        usersTab.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel uHeader = new JPanel(new BorderLayout());
        uHeader.setBackground(Color.WHITE);

        JLabel lblUTitle = new JLabel("Staff User Accounts & Permission Access Control");
        lblUTitle.setFont(UIUtils.FONT_TITLE);
        lblUTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel uActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        uActions.setBackground(Color.WHITE);

        JButton btnAdd = UIUtils.createPrimaryButton("+ Add User");
        btnAdd.addActionListener(e -> showAddEditDialog(null));

        JButton btnEdit = UIUtils.createSecondaryButton("Edit & Permissions");
        btnEdit.addActionListener(e -> editSelectedUser());

        JButton btnResetPass = UIUtils.createSecondaryButton("Reset Password");
        btnResetPass.addActionListener(e -> resetUserPassword());

        JButton btnToggleStatus = UIUtils.createSecondaryButton("Toggle Status");
        btnToggleStatus.addActionListener(e -> toggleUserStatus());

        JButton btnDelete = UIUtils.createDangerButton("Delete User");
        btnDelete.addActionListener(e -> deleteSelectedUser());

        uActions.add(btnAdd);
        uActions.add(btnEdit);
        uActions.add(btnResetPass);
        uActions.add(btnToggleStatus);
        uActions.add(btnDelete);

        uHeader.add(lblUTitle, BorderLayout.WEST);
        uHeader.add(uActions, BorderLayout.EAST);
        usersTab.add(uHeader, BorderLayout.NORTH);

        UIUtils.styleTable(tblUsers);
        JScrollPane scrollUsers = new JScrollPane(tblUsers);
        scrollUsers.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        usersTab.add(scrollUsers, BorderLayout.CENTER);

        // TAB 2: Registration Requests
        JPanel reqTab = new JPanel(new BorderLayout(0, 14));
        reqTab.setBackground(Color.WHITE);
        reqTab.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel rHeader = new JPanel(new BorderLayout());
        rHeader.setBackground(Color.WHITE);

        JLabel lblRTitle = new JLabel("Pending & Historical Account Access Requests");
        lblRTitle.setFont(UIUtils.FONT_TITLE);
        lblRTitle.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JPanel rActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rActions.setBackground(Color.WHITE);

        JButton btnApprove = UIUtils.createPrimaryButton("Approve Request");
        btnApprove.addActionListener(e -> reviewSelectedRequest("Approved"));

        JButton btnReject = UIUtils.createDangerButton("Reject Request");
        btnReject.addActionListener(e -> reviewSelectedRequest("Rejected"));

        JButton btnRefreshReq = UIUtils.createSecondaryButton("Refresh");
        btnRefreshReq.addActionListener(e -> refreshRequests());

        rActions.add(btnApprove);
        rActions.add(btnReject);
        rActions.add(btnRefreshReq);

        rHeader.add(lblRTitle, BorderLayout.WEST);
        rHeader.add(rActions, BorderLayout.EAST);
        reqTab.add(rHeader, BorderLayout.NORTH);

        UIUtils.styleTable(tblRequests);
        JScrollPane scrollReq = new JScrollPane(tblRequests);
        scrollReq.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        reqTab.add(scrollReq, BorderLayout.CENTER);

        tabbedPane.addTab("Staff Accounts", usersTab);
        tabbedPane.addTab("Registration Requests", reqTab);
        tabbedPane.setFont(UIUtils.FONT_BOLD);

        add(tabbedPane, BorderLayout.CENTER);
        refreshData();
    }

    public void refreshData() {
        refreshUsers();
        refreshRequests();
    }

    private void refreshUsers() {
        try {
            List<User> list = userDAO.getAllUsers();
            userModel.setRowCount(0);
            for (User u : list) {
                userModel.addRow(new Object[]{
                        u.getUserId(),
                        u.getUsername(),
                        u.getFullName(),
                        u.getEmail() != null ? u.getEmail() : "-",
                        u.getRole(),
                        u.getAccountStatus(),
                        u.getPermissions().toString()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void refreshRequests() {
        try {
            List<AccountRequest> list = userDAO.getAccountRequests();
            reqModel.setRowCount(0);
            for (AccountRequest r : list) {
                reqModel.addRow(new Object[]{
                        r.getRequestId(),
                        r.getFullName(),
                        r.getUsername(),
                        r.getEmail(),
                        r.getRole(),
                        r.getStatus(),
                        r.getRejectionReason() != null ? r.getRejectionReason() : "-",
                        UIUtils.formatDate(r.getCreatedAt())
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private User getSelectedUser() {
        int row = tblUsers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user account row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tblUsers.getValueAt(row, 0);
        try {
            for (User u : userDAO.getAllUsers()) {
                if (u.getUserId() == id) return u;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void editSelectedUser() {
        User u = getSelectedUser();
        if (u != null) {
            showAddEditDialog(u);
        }
    }

    private void resetUserPassword() {
        User u = getSelectedUser();
        if (u == null) return;

        String newPass = JOptionPane.showInputDialog(this,
                "Enter new confidential password for " + u.getFullName() + " (" + u.getUsername() + "):",
                "Reset Password", JOptionPane.QUESTION_MESSAGE);

        if (newPass != null && !newPass.trim().isEmpty()) {
            try {
                userDAO.resetPassword(u.getUserId(), newPass.trim());
                JOptionPane.showMessageDialog(this, "Password has been successfully updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void toggleUserStatus() {
        User u = getSelectedUser();
        if (u == null) return;

        String newStatus = "Active".equalsIgnoreCase(u.getAccountStatus()) ? "Inactive" : "Active";
        try {
            userDAO.setUserStatus(u.getUserId(), newStatus);
            JOptionPane.showMessageDialog(this, u.getUsername() + " account status changed to " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshUsers();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedUser() {
        User u = getSelectedUser();
        if (u == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete account '" + u.getUsername() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userDAO.deleteUser(u.getUserId());
                JOptionPane.showMessageDialog(this, "User deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshUsers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void showAddEditDialog(User existing) {
        boolean isEdit = (existing != null);
        String title = isEdit ? "Edit User & Permissions - " + existing.getUsername() : "Add New User Account";

        JTextField txtUser = new JTextField(isEdit ? existing.getUsername() : "");
        txtUser.setEditable(!isEdit);
        JTextField txtName = new JTextField(isEdit ? existing.getFullName() : "");
        JTextField txtEmail = new JTextField(isEdit ? existing.getEmail() : "");
        JPasswordField txtPass = new JPasswordField("default123");
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"Admin", "Cashier", "Inventory Clerk", "Store Manager", "Auditor", "Custom"});
        if (isEdit) cmbRole.setSelectedItem(existing.getRole());

        // Granular 10-module permissions matrix check boxes
        JCheckBox chkDash = new JCheckBox("Dashboard");
        JCheckBox chkPos = new JCheckBox("Point of Sale");
        JCheckBox chkViewInv = new JCheckBox("View Inventory");
        JCheckBox chkInv = new JCheckBox("Manage Inventory");
        JCheckBox chkProd = new JCheckBox("Products & Pricing");
        JCheckBox chkTx = new JCheckBox("Transactions");
        JCheckBox chkReport = new SalesCheckBox("Sales Report");
        JCheckBox chkFiles = new JCheckBox("File Management");
        JCheckBox chkAudit = new JCheckBox("Audit History");
        JCheckBox chkUsers = new JCheckBox("User Management");

        List<JCheckBox> boxes = Arrays.asList(chkDash, chkPos, chkViewInv, chkInv, chkProd, chkTx, chkReport, chkFiles, chkAudit, chkUsers);
        String[] keys = {"dashboard", "pos", "view-inventory", "inventory", "products", "transactions", "sales-report", "file-manager", "audit", "users"};

        for (int i = 0; i < boxes.size(); i++) {
            if (isEdit && existing.hasPermission(keys[i])) {
                boxes.get(i).setSelected(true);
            }
        }

        // Role preset change handler
        cmbRole.addActionListener(e -> {
            String sel = (String) cmbRole.getSelectedItem();
            if (!"Custom".equals(sel)) {
                User dummy = new User();
                dummy.applyRoleDefaultPermissions(sel);
                for (int i = 0; i < boxes.size(); i++) {
                    boxes.get(i).setSelected(dummy.hasPermission(keys[i]));
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(460, 480));

        JPanel form = new JPanel(new GridLayout(isEdit ? 4 : 5, 2, 6, 6));
        form.add(new JLabel("Username:")); form.add(txtUser);
        if (!isEdit) {
            form.add(new JLabel("Initial Password:")); form.add(txtPass);
        }
        form.add(new JLabel("Full Name:")); form.add(txtName);
        form.add(new JLabel("Email Address:")); form.add(txtEmail);
        form.add(new JLabel("Role Preset:")); form.add(cmbRole);

        JPanel matrixPanel = new JPanel(new GridLayout(5, 2, 6, 4));
        matrixPanel.setBorder(BorderFactory.createTitledBorder("Granular 10-Module Permissions Matrix"));
        for (JCheckBox b : boxes) matrixPanel.add(b);

        panel.add(form);
        panel.add(Box.createVerticalStrut(10));
        panel.add(matrixPanel);

        int res = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String uName = txtUser.getText().trim();
            String fName = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String role = (String) cmbRole.getSelectedItem();

            if (uName.isEmpty() || fName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Full Name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<String> perms = new ArrayList<>();
            for (int i = 0; i < boxes.size(); i++) {
                if (boxes.get(i).isSelected()) {
                    perms.add(keys[i]);
                }
            }

            try {
                if (isEdit) {
                    existing.setFullName(fName);
                    existing.setEmail(email);
                    existing.setRole(role);
                    existing.setPermissions(perms);
                    userDAO.updateUser(existing);
                } else {
                    String pass = new String(txtPass.getPassword());
                    User nu = new User(0, uName, pass, fName, email, role, "Active");
                    nu.setPermissions(perms);
                    userDAO.addUser(nu, pass);
                }
                JOptionPane.showMessageDialog(this, "User saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshUsers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reviewSelectedRequest(String status) {
        int row = tblRequests.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a registration request from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reqId = (int) tblRequests.getValueAt(row, 0);
        String currentStatus = (String) tblRequests.getValueAt(row, 5);

        if (!"Pending".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This request has already been reviewed (" + currentStatus + ").", "Already Processed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String reason = "";
        if ("Rejected".equalsIgnoreCase(status)) {
            reason = JOptionPane.showInputDialog(this, "Enter reason for rejecting this registration request:", "Rejection Reason", JOptionPane.PLAIN_MESSAGE);
            if (reason == null) return;
        }

        try {
            int adminId = AuthSession.getCurrentUser() != null ? AuthSession.getCurrentUser().getUserId() : 1;
            boolean ok = userDAO.reviewAccountRequest(reqId, status, reason, adminId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Request marked as " + status + ".", "Processed", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class SalesCheckBox extends JCheckBox {
        public SalesCheckBox(String text) { super(text); }
    }
}
