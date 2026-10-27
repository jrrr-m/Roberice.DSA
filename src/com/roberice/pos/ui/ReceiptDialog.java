package com.roberice.pos.ui;

import com.roberice.pos.model.SalesTransaction;
import com.roberice.pos.service.ReceiptPrinter;
import com.roberice.pos.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;

/**
 * Official Sales Receipt Preview Dialog (Section 1.5 in User Manual).
 */
public class ReceiptDialog extends JDialog {

    public ReceiptDialog(Frame parent, SalesTransaction tx) {
        super(parent, "Customer Sales Receipt - " + tx.getTransactionId(), true);
        setSize(460, 620);
        setLocationRelativeTo(parent);

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(16, 20, 16, 20));

        JTextArea txtReceipt = new JTextArea();
        txtReceipt.setEditable(false);
        txtReceipt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReceipt.setBackground(new Color(250, 250, 250));
        txtReceipt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1),
                new EmptyBorder(12, 14, 12, 14)
        ));

        String receiptText = ReceiptPrinter.buildReceiptText(tx);
        txtReceipt.setText(receiptText);
        txtReceipt.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(txtReceipt);
        scroll.setBorder(null);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Color.WHITE);

        JButton btnPrint = UIUtils.createPrimaryButton("Print Receipt");
        btnPrint.addActionListener(e -> {
            try {
                txtReceipt.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage(), "Printer Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnClose = UIUtils.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dispose());

        actions.add(btnClose);
        actions.add(btnPrint);

        main.add(scroll, BorderLayout.CENTER);
        main.add(actions, BorderLayout.SOUTH);

        setContentPane(main);
    }
}
