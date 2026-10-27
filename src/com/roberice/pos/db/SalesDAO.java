package com.roberice.pos.db;

import com.roberice.pos.model.SalesTransaction;
import com.roberice.pos.model.TransactionItem;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object for Point of Sale Checkout, Transaction Logs, and Multi-Period Sales Reports.
 */
public class SalesDAO {

    /**
     * Executes atomic POS checkout with FIFO stock deduction, automated sack opening, and receipt creation.
     */
    public synchronized SalesTransaction processSale(SalesTransaction transaction, String cashierUsername) throws SQLException {
        if (transaction.getItems() == null || transaction.getItems().isEmpty()) {
            throw new SQLException("Cannot checkout: Cart is empty.");
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Verify User ID
                int userId = transaction.getUserId();
                String cashierName = transaction.getCashierName();

                if (userId <= 0) {
                    String uSql = "SELECT user_id, full_name FROM users WHERE username = ? LIMIT 1";
                    try (PreparedStatement ps = conn.prepareStatement(uSql)) {
                        ps.setString(1, cashierUsername);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                userId = rs.getInt("user_id");
                                cashierName = rs.getString("full_name");
                            }
                        }
                    }
                }

                if (userId <= 0) {
                    // Fallback to first user
                    try (Statement s = conn.createStatement();
                         ResultSet rs = s.executeQuery("SELECT user_id, full_name FROM users LIMIT 1")) {
                        if (rs.next()) {
                            userId = rs.getInt("user_id");
                            cashierName = rs.getString("full_name");
                        } else {
                            throw new SQLException("No active cashier account found to associate with transaction.");
                        }
                    }
                }

                transaction.setUserId(userId);
                transaction.setCashierName(cashierName);

                // 2. Process each item: Stock deduction + Auto Sack Opening
                InventoryDAO invDao = new InventoryDAO();

                for (TransactionItem item : transaction.getItems()) {
                    int pId = item.getProductId();
                    String unit = item.getUnit() != null ? item.getUnit().toLowerCase() : "tinatakal";
                    double qty = item.getQuantity();

                    // Fetch active batches for this product in FIFO order
                    List<Map<String, Object>> batches = new ArrayList<>();
                    String batchSql = "SELECT batch_id, batch_code, stock_sacks, packaged_25kg, packaged_20kg, packaged_10kg, packaged_5kg, stock_kg " +
                            "FROM inventory_batch WHERE product_id = ? AND status = 'Active' ORDER BY received_date ASC, batch_id ASC";
                    
                    try (PreparedStatement ps = conn.prepareStatement(batchSql)) {
                        ps.setInt(1, pId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                Map<String, Object> b = new HashMap<>();
                                b.put("batch_id", rs.getInt("batch_id"));
                                b.put("batch_code", rs.getString("batch_code"));
                                b.put("stock_sacks", rs.getInt("stock_sacks"));
                                b.put("packaged_25kg", rs.getInt("packaged_25kg"));
                                b.put("packaged_20kg", rs.getInt("packaged_20kg"));
                                b.put("packaged_10kg", rs.getInt("packaged_10kg"));
                                b.put("packaged_5kg", rs.getInt("packaged_5kg"));
                                b.put("stock_kg", rs.getDouble("stock_kg"));
                                batches.add(b);
                            }
                        }
                    }

                    if (batches.isEmpty()) {
                        throw new SQLException("No active inventory batches found for " + item.getProductName());
                    }

                    // Deduct according to unit packaging
                    if (unit.equals("sack") || unit.equals("50kg")) {
                        int totalSacks = batches.stream().mapToInt(b -> (int) b.get("stock_sacks")).sum();
                        if (qty > totalSacks) {
                            throw new SQLException("Insufficient whole sacks for " + item.getProductName() + ". Available: " + totalSacks);
                        }
                        int rem = (int) qty;
                        for (Map<String, Object> b : batches) {
                            int cur = (int) b.get("stock_sacks");
                            if (cur <= 0) continue;
                            int deduct = Math.min(rem, cur);
                            b.put("stock_sacks", cur - deduct);
                            rem -= deduct;
                            if (rem <= 0) break;
                        }
                    } else if (unit.equals("25kg") || unit.equals("half") || unit.equals("half_sack")) {
                        int total = batches.stream().mapToInt(b -> (int) b.get("packaged_25kg")).sum();
                        if (qty > total) {
                            throw new SQLException("Insufficient 25kg half sacks for " + item.getProductName() + ". Available: " + total);
                        }
                        int rem = (int) qty;
                        for (Map<String, Object> b : batches) {
                            int cur = (int) b.get("packaged_25kg");
                            if (cur <= 0) continue;
                            int deduct = Math.min(rem, cur);
                            b.put("packaged_25kg", cur - deduct);
                            rem -= deduct;
                            if (rem <= 0) break;
                        }
                    } else if (unit.equals("20kg")) {
                        int total = batches.stream().mapToInt(b -> (int) b.get("packaged_20kg")).sum();
                        if (qty > total) {
                            throw new SQLException("Insufficient 20kg bags for " + item.getProductName() + ". Available: " + total);
                        }
                        int rem = (int) qty;
                        for (Map<String, Object> b : batches) {
                            int cur = (int) b.get("packaged_20kg");
                            if (cur <= 0) continue;
                            int deduct = Math.min(rem, cur);
                            b.put("packaged_20kg", cur - deduct);
                            rem -= deduct;
                            if (rem <= 0) break;
                        }
                    } else if (unit.equals("10kg")) {
                        int total = batches.stream().mapToInt(b -> (int) b.get("packaged_10kg")).sum();
                        if (qty > total) {
                            throw new SQLException("Insufficient 10kg bags for " + item.getProductName() + ". Available: " + total);
                        }
                        int rem = (int) qty;
                        for (Map<String, Object> b : batches) {
                            int cur = (int) b.get("packaged_10kg");
                            if (cur <= 0) continue;
                            int deduct = Math.min(rem, cur);
                            b.put("packaged_10kg", cur - deduct);
                            rem -= deduct;
                            if (rem <= 0) break;
                        }
                    } else if (unit.equals("5kg")) {
                        int total = batches.stream().mapToInt(b -> (int) b.get("packaged_5kg")).sum();
                        if (qty > total) {
                            throw new SQLException("Insufficient 5kg bags for " + item.getProductName() + ". Available: " + total);
                        }
                        int rem = (int) qty;
                        for (Map<String, Object> b : batches) {
                            int cur = (int) b.get("packaged_5kg");
                            if (cur <= 0) continue;
                            int deduct = Math.min(rem, cur);
                            b.put("packaged_5kg", cur - deduct);
                            rem -= deduct;
                            if (rem <= 0) break;
                        }
                    } else {
                        // Loose / Tinatakal per kg -> Auto sack opening if needed!
                        double totalLooseKg = batches.stream().mapToDouble(b -> (double) b.get("stock_kg")).sum();
                        if (totalLooseKg < qty) {
                            double neededKg = qty - totalLooseKg;
                            int sacksNeeded = (int) Math.ceil(neededKg / 50.0);

                            // Auto open from oldest batch having whole sacks
                            for (Map<String, Object> b : batches) {
                                if (sacksNeeded <= 0) break;
                                int sCount = (int) b.get("stock_sacks");
                                if (sCount <= 0) continue;

                                int openNow = Math.min(sacksNeeded, sCount);
                                b.put("stock_sacks", sCount - openNow);
                                double addKg = openNow * 50.0;
                                b.put("stock_kg", ((double) b.get("stock_kg")) + addKg);
                                sacksNeeded -= openNow;

                                // Log auto sack opening
                                String logAdj = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, ?, CURRENT_TIMESTAMP, 'open_sack', ?, ?, ?, ?)";
                                try (PreparedStatement psA = conn.prepareStatement(logAdj)) {
                                    psA.setInt(1, pId);
                                    psA.setString(2, (String) b.get("batch_code"));
                                    psA.setInt(3, openNow);
                                    psA.setDouble(4, addKg);
                                    psA.setString(5, "Auto-opened " + openNow + "x Whole Sack (50kg) from Batch " + b.get("batch_code") + " for " + qty + " kg retail checkout.");
                                    psA.setString(6, cashierName);
                                    psA.executeUpdate();
                                }
                            }

                            if (sacksNeeded > 0) {
                                throw new SQLException("Cannot process sale: Insufficient loose stock and whole sacks for " + item.getProductName());
                            }
                        }

                        // Deduct loose kg in FIFO order
                        double remKg = qty;
                        for (Map<String, Object> b : batches) {
                            double curKg = (double) b.get("stock_kg");
                            if (curKg <= 0.0) continue;
                            double deduct = Math.min(remKg, curKg);
                            b.put("stock_kg", Math.max(0.0, curKg - deduct));
                            remKg -= deduct;
                            if (remKg <= 0.0001) break;
                        }
                    }

                    // Update batches in database and check for depletion
                    String updBatchSql = "UPDATE inventory_batch SET stock_sacks = ?, packaged_25kg = ?, packaged_20kg = ?, packaged_10kg = ?, packaged_5kg = ?, stock_kg = ?, status = ? WHERE batch_id = ?";
                    for (Map<String, Object> b : batches) {
                        int s = (int) b.get("stock_sacks");
                        int p25 = (int) b.get("packaged_25kg");
                        int p20 = (int) b.get("packaged_20kg");
                        int p10 = (int) b.get("packaged_10kg");
                        int p5 = (int) b.get("packaged_5kg");
                        double k = (double) b.get("stock_kg");
                        double vol = (s * 50.0) + (p25 * 25.0) + (p20 * 20.0) + (p10 * 10.0) + (p5 * 5.0) + k;
                        String newStatus = (vol <= 0.001) ? "Depleted" : "Active";

                        try (PreparedStatement psU = conn.prepareStatement(updBatchSql)) {
                            psU.setInt(1, s);
                            psU.setInt(2, p25);
                            psU.setInt(3, p20);
                            psU.setInt(4, p10);
                            psU.setInt(5, p5);
                            psU.setDouble(6, Math.round(k * 100.0) / 100.0);
                            psU.setString(7, newStatus);
                            psU.setInt(8, (int) b.get("batch_id"));
                            psU.executeUpdate();
                        }

                        if ("Depleted".equals(newStatus)) {
                            String depSql = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, ?, CURRENT_TIMESTAMP, 'batch_depleted', 0, 0, ?, ?)";
                            try (PreparedStatement psD = conn.prepareStatement(depSql)) {
                                psD.setInt(1, pId);
                                psD.setString(2, (String) b.get("batch_code"));
                                psD.setString(3, "Batch " + b.get("batch_code") + " fully sold out and archived.");
                                psD.setString(4, "System Auto-Audit");
                                psD.executeUpdate();
                            }
                        }
                    }

                    // Sync product inventory
                    invDao.syncAggregatedInventory(conn, pId);
                }

                // 3. Insert sales_transaction record
                String insTx = "INSERT INTO sales_transaction (user_id, transaction_date, total_amount, payment_amount, change_amount, payment_method, transaction_status) VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?, ?, 'Completed')";
                int txId = 0;
                try (PreparedStatement psT = conn.prepareStatement(insTx, Statement.RETURN_GENERATED_KEYS)) {
                    psT.setInt(1, transaction.getUserId());
                    psT.setDouble(2, transaction.getTotalAmount());
                    psT.setDouble(3, transaction.getPaymentAmount());
                    psT.setDouble(4, transaction.getChangeAmount());
                    psT.setString(5, transaction.getPaymentMethod());
                    psT.executeUpdate();

                    try (ResultSet gk = psT.getGeneratedKeys()) {
                        if (gk.next()) {
                            txId = gk.getInt(1);
                            transaction.setTransactionId(txId);
                        }
                    }
                }

                // 4. Insert transaction_item records
                String insItem = "INSERT INTO transaction_item (transaction_id, product_id, unit, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psI = conn.prepareStatement(insItem, Statement.RETURN_GENERATED_KEYS)) {
                    for (TransactionItem item : transaction.getItems()) {
                        psI.setInt(1, txId);
                        psI.setInt(2, item.getProductId());
                        psI.setString(3, item.getUnit());
                        psI.setDouble(4, item.getQuantity());
                        psI.setDouble(5, item.getUnitPrice());
                        psI.setDouble(6, item.getSubtotal());
                        psI.executeUpdate();
                    }
                }

                transaction.setTransactionDate(new java.util.Date());
                conn.commit();
                return transaction;

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Retrieves transactions matching optional search/filter criteria.
     */
    public List<SalesTransaction> getTransactions(int limit, String search) throws SQLException {
        List<SalesTransaction> list = new ArrayList<>();
        String sql = """
            SELECT 
                t.*, 
                u.full_name AS cashier_name
            FROM sales_transaction t
            LEFT JOIN users u ON t.user_id = u.user_id
            WHERE t.transaction_status = 'Completed'
            ORDER BY t.transaction_id DESC
            LIMIT ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 200);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SalesTransaction tx = new SalesTransaction();
                    tx.setTransactionId(rs.getInt("transaction_id"));
                    tx.setUserId(rs.getInt("user_id"));
                    tx.setCashierName(rs.getString("cashier_name") != null ? rs.getString("cashier_name") : "Cashier #" + tx.getUserId());
                    tx.setTransactionDate(DatabaseManager.parseDate(rs.getObject("transaction_date")));
                    tx.setTotalAmount(rs.getDouble("total_amount"));
                    tx.setPaymentAmount(rs.getDouble("payment_amount"));
                    tx.setChangeAmount(rs.getDouble("change_amount"));
                    tx.setPaymentMethod(rs.getString("payment_method"));
                    tx.setTransactionStatus(rs.getString("transaction_status"));
                    list.add(tx);
                }
            }
        }
        return list;
    }

    /**
     * Retrieves line items for a specific transaction.
     */
    public List<TransactionItem> getItemsForTransaction(int transactionId) throws SQLException {
        List<TransactionItem> list = new ArrayList<>();
        String sql = """
            SELECT 
                ti.*, 
                p.product_name
            FROM transaction_item ti
            JOIN product p ON ti.product_id = p.product_id
            WHERE ti.transaction_id = ?
            ORDER BY ti.transaction_item_id ASC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TransactionItem item = new TransactionItem();
                    item.setTransactionItemId(rs.getInt("transaction_item_id"));
                    item.setTransactionId(rs.getInt("transaction_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setUnit(rs.getString("unit"));
                    item.setQuantity(rs.getDouble("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setSubtotal(rs.getDouble("subtotal"));
                    list.add(item);
                }
            }
        }
        return list;
    }
}
