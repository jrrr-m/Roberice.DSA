package com.roberice.dsa.service;

import com.roberice.dsa.db.Database;
import com.roberice.dsa.model.SalesTransaction;
import com.roberice.dsa.model.TransactionItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PosService {

    public static SalesTransaction checkout(int userId, String cashierName, String paymentMethod,
                                            double paymentAmount, List<TransactionItem> items) throws Exception {

        if (items == null || items.isEmpty()) {
            throw new Exception("Cart is empty. Please add items before checking out.");
        }

        double totalAmount = 0.0;
        for (TransactionItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new Exception("Invalid quantity for item " + item.getProductName());
            }
            totalAmount += item.getSubtotal();
        }

        if (paymentAmount < totalAmount) {
            throw new Exception(String.format("Insufficient payment. Total is ₱%.2f, received ₱%.2f.", totalAmount, paymentAmount));
        }

        final double fTotalAmount = totalAmount;
        final double changeAmount = paymentAmount - totalAmount;
        final String fPaymentMethod = (paymentMethod != null && !paymentMethod.trim().isEmpty()) ? paymentMethod : "Cash";

        return Database.inTransaction(conn -> {
            // 1. Validate & Deduct Inventory for each item
            for (TransactionItem item : items) {
                int productId = item.getProductId();
                String unit = item.getUnit().toLowerCase();
                double qty = item.getQuantity();

                // Lock inventory row
                String invQuery = "SELECT stock_sacks, packaged_25kg, packaged_20kg, packaged_10kg, packaged_5kg, stock_kg " +
                        "FROM inventory WHERE product_id = ? FOR UPDATE";

                int curSacks = 0, cur25 = 0, cur20 = 0, cur10 = 0, cur5 = 0;
                double curKg = 0.0;

                try (PreparedStatement ps = conn.prepareStatement(invQuery)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new Exception("Inventory record missing for product ID: " + productId);
                        curSacks = rs.getInt("stock_sacks");
                        cur25 = rs.getInt("packaged_25kg");
                        cur20 = rs.getInt("packaged_20kg");
                        cur10 = rs.getInt("packaged_10kg");
                        cur5 = rs.getInt("packaged_5kg");
                        curKg = rs.getDouble("stock_kg");
                    }
                }

                if ("sack".equals(unit)) {
                    int sacksQty = (int) Math.round(qty);
                    if (curSacks < sacksQty) {
                        throw new Exception("Insufficient whole sacks for " + item.getProductName() + ". Available: " + curSacks + ", Requested: " + sacksQty);
                    }
                    // Deduct inventory
                    String upd = "UPDATE inventory SET stock_sacks = stock_sacks - ?, last_updated = NOW() WHERE product_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(upd)) {
                        ps.setInt(1, sacksQty);
                        ps.setInt(2, productId);
                        ps.executeUpdate();
                    }
                    // Deduct FIFO batches
                    deductBatchesSacks(conn, productId, sacksQty);

                } else if ("25kg".equals(unit)) {
                    int q = (int) Math.round(qty);
                    if (cur25 < q) {
                        throw new Exception("Insufficient 25kg pre-packs for " + item.getProductName() + ". Available: " + cur25);
                    }
                    String upd = "UPDATE inventory SET packaged_25kg = packaged_25kg - ?, last_updated = NOW() WHERE product_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(upd)) {
                        ps.setInt(1, q);
                        ps.setInt(2, productId);
                        ps.executeUpdate();
                    }

                } else if ("20kg".equals(unit)) {
                    int q = (int) Math.round(qty);
                    if (cur20 < q) {
                        throw new Exception("Insufficient 20kg pre-packs for " + item.getProductName() + ". Available: " + cur20);
                    }
                    String upd = "UPDATE inventory SET packaged_20kg = packaged_20kg - ?, last_updated = NOW() WHERE product_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(upd)) {
                        ps.setInt(1, q);
                        ps.setInt(2, productId);
                        ps.executeUpdate();
                    }

                } else if ("10kg".equals(unit)) {
                    int q = (int) Math.round(qty);
                    if (cur10 < q) {
                        throw new Exception("Insufficient 10kg pre-packs for " + item.getProductName() + ". Available: " + cur10);
                    }
                    String upd = "UPDATE inventory SET packaged_10kg = packaged_10kg - ?, last_updated = NOW() WHERE product_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(upd)) {
                        ps.setInt(1, q);
                        ps.setInt(2, productId);
                        ps.executeUpdate();
                    }

                } else if ("5kg".equals(unit)) {
                    int q = (int) Math.round(qty);
                    if (cur5 < q) {
                        throw new Exception("Insufficient 5kg pre-packs for " + item.getProductName() + ". Available: " + cur5);
                    }
                    String upd = "UPDATE inventory SET packaged_5kg = packaged_5kg - ?, last_updated = NOW() WHERE product_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(upd)) {
                        ps.setInt(1, q);
                        ps.setInt(2, productId);
                        ps.executeUpdate();
                    }

                } else { // "kg"
                    if (curKg < qty) {
                        double neededKg = qty - curKg;
                        int sacksToOpen = (int) Math.ceil(neededKg / 50.0);
                        if (curSacks < sacksToOpen) {
                            throw new Exception("Insufficient loose grain stock for " + item.getProductName() +
                                    ". Available loose: " + String.format("%.2f", curKg) + "kg, sacks available: " + curSacks);
                        }
                        // Auto-open sacks
                        double openedKg = sacksToOpen * 50.0;
                        String openUpd = "UPDATE inventory SET stock_sacks = stock_sacks - ?, stock_kg = stock_kg + ?, last_updated = NOW() WHERE product_id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(openUpd)) {
                            ps.setInt(1, sacksToOpen);
                            ps.setDouble(2, openedKg);
                            ps.setInt(3, productId);
                            ps.executeUpdate();
                        }
                        // Log open sack
                        String logSql = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, 'AUTO-POS', NOW(), 'open_sack', ?, ?, ?, ?)";
                        try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                            ps.setInt(1, productId);
                            ps.setInt(2, sacksToOpen);
                            ps.setDouble(3, openedKg);
                            ps.setString(4, "Auto-opened " + sacksToOpen + " sack(s) to fulfill POS sale of " + qty + "kg.");
                            ps.setString(5, cashierName != null ? cashierName : "Cashier");
                            ps.executeUpdate();
                        }
                        // Deduct batches sacks
                        deductBatchesSacks(conn, productId, sacksToOpen);
                    }

                    // Now deduct loose kg
                    String upd = "UPDATE inventory SET stock_kg = stock_kg - ?, last_updated = NOW() WHERE product_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(upd)) {
                        ps.setDouble(1, qty);
                        ps.setInt(2, productId);
                        ps.executeUpdate();
                    }
                    // Deduct from batch loose kg
                    deductBatchesLooseKg(conn, productId, qty);
                }
            }

            // 2. Insert sales_transaction
            int transactionId = 0;
            String txSql = "INSERT INTO sales_transaction (user_id, transaction_date, total_amount, payment_amount, change_amount, payment_method, transaction_status) " +
                    "VALUES (?, NOW(), ?, ?, ?, ?, 'Completed')";

            try (PreparedStatement ps = conn.prepareStatement(txSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setDouble(2, fTotalAmount);
                ps.setDouble(3, paymentAmount);
                ps.setDouble(4, changeAmount);
                ps.setString(5, fPaymentMethod);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        transactionId = rs.getInt(1);
                    }
                }
            }

            // 3. Insert transaction_item
            String itemSql = "INSERT INTO transaction_item (transaction_id, product_id, unit, quantity, unit_price, subtotal) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (TransactionItem item : items) {
                    item.setTransactionId(transactionId);
                    ps.setInt(1, transactionId);
                    ps.setInt(2, item.getProductId());
                    ps.setString(3, item.getUnit());
                    ps.setDouble(4, item.getQuantity());
                    ps.setDouble(5, item.getUnitPrice());
                    ps.setDouble(6, item.getSubtotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            SalesTransaction tx = new SalesTransaction();
            tx.setTransactionId(transactionId);
            tx.setUserId(userId);
            tx.setCashierName(cashierName);
            tx.setTotalAmount(fTotalAmount);
            tx.setPaymentAmount(paymentAmount);
            tx.setChangeAmount(changeAmount);
            tx.setPaymentMethod(fPaymentMethod);
            tx.setTransactionStatus("Completed");
            tx.setItems(items);
            return tx;
        });
    }

    private static void deductBatchesSacks(Connection conn, int productId, int sacksToDeduct) throws Exception {
        String batchSql = "SELECT batch_id, stock_sacks FROM inventory_batch WHERE product_id = ? AND status = 'Active' AND stock_sacks > 0 ORDER BY received_date ASC, batch_id ASC FOR UPDATE";
        int remaining = sacksToDeduct;

        try (PreparedStatement ps = conn.prepareStatement(batchSql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next() && remaining > 0) {
                    int bId = rs.getInt("batch_id");
                    int bSacks = rs.getInt("stock_sacks");
                    int take = Math.min(remaining, bSacks);

                    String upd = "UPDATE inventory_batch SET stock_sacks = stock_sacks - ? WHERE batch_id = ?";
                    try (PreparedStatement uPs = conn.prepareStatement(upd)) {
                        uPs.setInt(1, take);
                        uPs.setInt(2, bId);
                        uPs.executeUpdate();
                    }
                    remaining -= take;
                }
            }
        }
    }

    private static void deductBatchesLooseKg(Connection conn, int productId, double kgToDeduct) throws Exception {
        String batchSql = "SELECT batch_id, stock_kg FROM inventory_batch WHERE product_id = ? AND status = 'Active' AND stock_kg > 0 ORDER BY received_date ASC, batch_id ASC FOR UPDATE";
        double remaining = kgToDeduct;

        try (PreparedStatement ps = conn.prepareStatement(batchSql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next() && remaining > 0.001) {
                    int bId = rs.getInt("batch_id");
                    double bKg = rs.getDouble("stock_kg");
                    double take = Math.min(remaining, bKg);

                    String upd = "UPDATE inventory_batch SET stock_kg = stock_kg - ? WHERE batch_id = ?";
                    try (PreparedStatement uPs = conn.prepareStatement(upd)) {
                        uPs.setDouble(1, take);
                        uPs.setInt(2, bId);
                        uPs.executeUpdate();
                    }
                    remaining -= take;
                }
            }
        }
    }
}
