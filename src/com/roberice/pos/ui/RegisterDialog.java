package com.roberice.pos.ui;

import com.roberice.pos.db.UserDAO;
import com.roberice.pos.model.AccountRequest;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Registration Dialog for submitting an account request to the Administrator.
 */
public class RegisterDialog extends JDialog {
    private final JTextField txtFullName = new JTextField();
    private final JTextField txtUsername = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JComboBox<String> cmbRole = new JComboBox<>(new String[]{"Cashier", "Inventory Clerk", "Store Manager", "Auditor"});
    private final JLabel lblStatus = new JLabel(" ");

    public RegisterDialog(Dialog parent) {
        super(parent, "Request User Account - Roberice", true);
        setSize(400, 480);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Request Account Access");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel sub = new JLabel("Your request will be submitted to an Admin for approval.");
        sub.setFont(UIUtils.FONT_SMALL);
        sub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        panel.add(title);
        panel.add(sub);
        panel.add(Box.createVerticalStrut(16));

        addField(panel, "Full Name", txtFullName);
        addField(panel, "Username", txtUsername);
        addField(panel, "Email Address", txtEmail);
        addField(panel, "Password", txtPassword);

        JLabel lblRole = new JLabel("Requested Role");
        lblRole.setFont(UIUtils.FONT_BOLD);
        cmbRole.setMaximumSize(new Dimension(340, 34));
        panel.add(lblRole);
        panel.add(Box.createVerticalStrut(4));
        panel.add(cmbRole);
        panel.add(Box.createVerticalStrut(10));

        lblStatus.setFont(UIUtils.FONT_SMALL);
        lblStatus.setForeground(UIUtils.COLOR_DANGER);
        panel.add(lblStatus);
        panel.add(Box.createVerticalStrut(10));

        JButton btnSubmit = UIUtils.createPrimaryButton("Submit Registration Request");
        btnSubmit.setMaximumSize(new Dimension(340, 40));
        btnSubmit.addActionListener(e -> submit());
        panel.add(btnSubmit);

        setContentPane(panel);
    }

    private void addField(JPanel panel, String label, JComponent comp) {
        JLabel l = new JLabel(label);
        l.setFont(UIUtils.FONT_BOLD);
        comp.setMaximumSize(new Dimension(340, 34));
        panel.add(l);
        panel.add(Box.createVerticalStrut(4));
        panel.add(comp);
        panel.add(Box.createVerticalStrut(10));
    }

    private void submit() {
        String name = txtFullName.getText().trim();
        String user = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String pass = new String(txtPassword.getPassword());
        String role = (String) cmbRole.getSelectedItem();

        if (name.isEmpty() || user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            lblStatus.setText("All fields are required.");
            return;
        }

        try {
            AccountRequest req = new AccountRequest();
            req.setFullName(name);
            req.setUsername(user);
            req.setEmail(email);
            req.setPassword(pass);
            req.setRole(role);

            UserDAO dao = new UserDAO();
            boolean ok = dao.submitAccountRequest(req);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Your registration request has been submitted successfully!\nAn administrator will review and activate your account.",
                        "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                lblStatus.setText("Failed to submit request.");
            }
        } catch (Exception ex) {
            lblStatus.setText(ex.getMessage());
        }
    }
}
