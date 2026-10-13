package com.roberice.dsa.service;

import com.roberice.dsa.db.Database;
import com.roberice.dsa.model.SalesTransaction;
import com.roberice.dsa.model.TransactionItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class TransactionService {

    public static List<SalesTransaction> getTransactions(String search, String status, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
                "SELECT t.transaction_id, t.transaction_date, t.total_amount, t.payment_amount, t.change_amount, " +
                "t.payment_method, t.transaction_status, t.user_id, u.username, u.full_name " +
                "FROM sales_transaction t " +
                "LEFT JOIN users u ON t.user_id = u.user_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (t.transaction_id LIKE ? OR u.username LIKE ? OR u.full_name LIKE ? OR t.payment_method LIKE ?) ");
            String term = "%" + search.trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            sql.append("AND t.transaction_status = ? ");
            params.add(status.trim());
        }

        sql.append("ORDER BY t.transaction_date DESC ");
        if (limit > 0) {
            sql.append("LIMIT ? OFFSET ?");
            params.add(limit);
            params.add(Math.max(offset, 0));
        }

        List<Map<String, Object>> rows = Database.queryForList(sql.toString(), params.toArray());
        List<SalesTransaction> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            SalesTransaction tx = new SalesTransaction();
            tx.setTransactionId(((Number) r.get("transaction_id")).intValue());
            tx.setTransactionDate(String.valueOf(r.get("transaction_date")));
            tx.setTotalAmount(((Number) r.get("total_amount")).doubleValue());
            tx.setPaymentAmount(((Number) r.get("payment_amount")).doubleValue());
            tx.setChangeAmount(((Number) r.get("change_amount")).doubleValue());
            tx.setPaymentMethod(String.valueOf(r.get("payment_method")));
            tx.setTransactionStatus(String.valueOf(r.get("transaction_status")));
            tx.setUserId(r.get("user_id") != null ? ((Number) r.get("user_id")).intValue() : 0);
            tx.setUsername(r.get("username") != null ? String.valueOf(r.get("username")) : "Unknown");
            tx.setCashierName(r.get("full_name") != null ? String.valueOf(r.get("full_name")) : tx.getUsername());
            list.add(tx);
        }
        return list;
    }

    public static SalesTransaction getTransactionDetails(int transactionId) {
        String txSql = "SELECT t.transaction_id, t.transaction_date, t.total_amount, t.payment_amount, t.change_amount, " +
                "t.payment_method, t.transaction_status, t.user_id, u.username, u.full_name " +
                "FROM sales_transaction t " +
                "LEFT JOIN users u ON t.user_id = u.user_id " +
                "WHERE t.transaction_id = ?";

        Map<String, Object> row = Database.queryForMap(txSql, transactionId);
        if (row == null) return null;

        SalesTransaction tx = new SalesTransaction();
        tx.setTransactionId(((Number) row.get("transaction_id")).intValue());
        tx.setTransactionDate(String.valueOf(row.get("transaction_date")));
        tx.setTotalAmount(((Number) row.get("total_amount")).doubleValue());
        tx.setPaymentAmount(((Number) row.get("payment_amount")).doubleValue());
        tx.setChangeAmount(((Number) row.get("change_amount")).doubleValue());
        tx.setPaymentMethod(String.valueOf(row.get("payment_method")));
        tx.setTransactionStatus(String.valueOf(row.get("transaction_status")));
        tx.setUserId(row.get("user_id") != null ? ((Number) row.get("user_id")).intValue() : 0);
        tx.setUsername(row.get("username") != null ? String.valueOf(row.get("username")) : "Unknown");
        tx.setCashierName(row.get("full_name") != null ? String.valueOf(row.get("full_name")) : tx.getUsername());

        String itemSql = "SELECT ti.transaction_item_id, ti.transaction_id, ti.product_id, ti.unit, ti.quantity, ti.unit_price, ti.subtotal, " +
                "p.product_name " +
                "FROM transaction_item ti " +
                "LEFT JOIN product p ON ti.product_id = p.product_id " +
                "WHERE ti.transaction_id = ?";

        List<Map<String, Object>> itemRows = Database.queryForList(itemSql, transactionId);
        List<TransactionItem> items = new ArrayList<>();
        for (Map<String, Object> ir : itemRows) {
            TransactionItem item = new TransactionItem();
            item.setTransactionItemId(((Number) ir.get("transaction_item_id")).intValue());
            item.setTransactionId(((Number) ir.get("transaction_id")).intValue());
            item.setProductId(((Number) ir.get("product_id")).intValue());
            item.setProductName(ir.get("product_name") != null ? String.valueOf(ir.get("product_name")) : "Product #" + item.getProductId());
            item.setUnit(String.valueOf(ir.get("unit")));
            item.setQuantity(((Number) ir.get("quantity")).doubleValue());
            item.setUnitPrice(((Number) ir.get("unit_price")).doubleValue());
            item.setSubtotal(((Number) ir.get("subtotal")).doubleValue());
            items.add(item);
        }
        tx.setItems(items);
        return tx;
    }

    public static void cancelTransaction(int transactionId, String reason, String username) throws Exception {
        SalesTransaction tx = getTransactionDetails(transactionId);
        if (tx == null) throw new Exception("Transaction not found.");
        if ("Cancelled".equalsIgnoreCase(tx.getTransactionStatus())) {
            throw new Exception("Transaction is already cancelled.");
        }

        Database.inTransaction(conn -> {
            // Revert stock for each line item
            for (TransactionItem item : tx.getItems()) {
                int pId = item.getProductId();
                String unit = item.getUnit().toLowerCase();
                double qty = item.getQuantity();

                String col;
                switch (unit) {
                    case "sack": col = "stock_sacks"; break;
                    case "25kg": col = "packaged_25kg"; break;
                    case "20kg": col = "packaged_20kg"; break;
                    case "10kg": col = "packaged_10kg"; break;
                    case "5kg": col = "packaged_5kg"; break;
                    case "kg": col = "stock_kg"; break;
                    default: col = "stock_kg"; break;
                }

                String updInv = "UPDATE inventory SET " + col + " = " + col + " + ?, last_updated = NOW() WHERE product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updInv)) {
                    ps.setDouble(1, qty);
                    ps.setInt(2, pId);
                    ps.executeUpdate();
                }

                // Log adjustment for cancellation
                String logSql = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, 'CANCEL-TX', NOW(), 'restock', ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                    ps.setInt(1, pId);
                    ps.setInt(2, "sack".equals(unit) ? (int) qty : 0);
                    ps.setDouble(3, "sack".equals(unit) ? qty * 50.0 : qty);
                    ps.setString(4, "Reverted from cancelled TX #" + transactionId + ". Reason: " + (reason != null ? reason : "N/A"));
                    ps.setString(5, username != null ? username : "System");
                    ps.executeUpdate();
                }
            }

            // Mark transaction cancelled
            String cancelSql = "UPDATE sales_transaction SET transaction_status = 'Cancelled' WHERE transaction_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(cancelSql)) {
                ps.setInt(1, transactionId);
                ps.executeUpdate();
            }

            return null;
        });
    }
}
