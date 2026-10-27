package com.roberice.pos.service;

import com.roberice.pos.model.SalesTransaction;
import com.roberice.pos.model.TransactionItem;

import java.text.SimpleDateFormat;

/**
 * Generates official formatted text receipts for customer sales orders.
 */
public class ReceiptPrinter {

    public static String buildReceiptText(SalesTransaction tx) {
        StringBuilder sb = new StringBuilder();
        String dateStr = tx.getTransactionDate() != null
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tx.getTransactionDate())
                : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        sb.append("================================================\n");
        sb.append("                   ROBERICE                     \n");
        sb.append("      Wholesale & Retail Rice Merchandising     \n");
        sb.append("   73 Farmers St. Corner Ampalaya, Marikina     \n");
        sb.append("     Tel: (02) 8942-0192 | Cell: 0917-823-4567  \n");
        sb.append("================================================\n");
        sb.append(String.format("Receipt No : REC-%06d\n", tx.getTransactionId()));
        sb.append(String.format("Date & Time: %s\n", dateStr));
        sb.append(String.format("Cashier    : %s\n", tx.getCashierName()));
        sb.append(String.format("Payment    : %s\n", tx.getPaymentMethod()));
        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-28s %-6s %12s\n", "ITEM", "QTY", "SUBTOTAL"));
        sb.append("------------------------------------------------\n");

        for (TransactionItem it : tx.getItems()) {
            String name = it.getProductName();
            if (name.length() > 27) {
                name = name.substring(0, 24) + "...";
            }
            String qtyStr = String.format("%.2f", it.getQuantity());
            if (it.getQuantity() == (int) it.getQuantity()) {
                qtyStr = String.valueOf((int) it.getQuantity());
            }

            sb.append(String.format("%-28s %-6s %12s\n", name, qtyStr, String.format("P %,.2f", it.getSubtotal())));
            sb.append(String.format("   [%s @ P %,.2f]\n", it.getUnitDisplay(), it.getUnitPrice()));
        }

        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-30s %16s\n", "TOTAL AMOUNT:", String.format("PHP %,.2f", tx.getTotalAmount())));
        sb.append(String.format("%-30s %16s\n", "PAYMENT RECEIVED (" + tx.getPaymentMethod() + "):", String.format("PHP %,.2f", tx.getPaymentAmount())));
        sb.append(String.format("%-30s %16s\n", "CHANGE:", String.format("PHP %,.2f", tx.getChangeAmount())));
        sb.append("================================================\n");
        sb.append("       Thank you for choosing Roberice!        \n");
        sb.append("   Please present this receipt for returns or   \n");
        sb.append("             quality replacement.               \n");
        sb.append("================================================\n");

        return sb.toString();
    }
}
