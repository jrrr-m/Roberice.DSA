package com.roberice.dsa.service;

import com.roberice.dsa.db.Database;
import com.roberice.dsa.model.AdjustmentLog;
import com.roberice.dsa.model.InventoryBatch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;

public class InventoryService {

    public static List<InventoryBatch> getBatchesForProduct(int productId) {
        String sql = "SELECT batch_id, product_id, batch_code, received_date, " +
                "DATEDIFF(CURRENT_DATE, received_date) AS days_old, " +
                "stock_sacks, packaged_25kg, packaged_20kg, packaged_10kg, packaged_5kg, stock_kg, status " +
                "FROM inventory_batch " +
                "WHERE product_id = ? " +
                "ORDER BY received_date ASC, batch_id ASC";

        List<Map<String, Object>> rows = Database.queryForList(sql, productId);
        List<InventoryBatch> batches = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            InventoryBatch b = new InventoryBatch();
            b.setBatchId(((Number) r.get("batch_id")).intValue());
            b.setProductId(((Number) r.get("product_id")).intValue());
            b.setBatchCode(String.valueOf(r.get("batch_code")));
            b.setReceivedDate(String.valueOf(r.get("received_date")));
            b.setDaysOld(r.get("days_old") != null ? ((Number) r.get("days_old")).intValue() : 0);
            b.setStockSacks(((Number) r.get("stock_sacks")).intValue());
            b.setPackaged25kg(((Number) r.get("packaged_25kg")).intValue());
            b.setPackaged20kg(((Number) r.get("packaged_20kg")).intValue());
            b.setPackaged10kg(((Number) r.get("packaged_10kg")).intValue());
            b.setPackaged5kg(((Number) r.get("packaged_5kg")).intValue());
            b.setStockKg(((Number) r.get("stock_kg")).doubleValue());
            b.setStatus(String.valueOf(r.get("status")));

            double totalKg = (b.getStockSacks() * 50.0) +
                    (b.getPackaged25kg() * 25.0) +
                    (b.getPackaged20kg() * 20.0) +
                    (b.getPackaged10kg() * 10.0) +
                    (b.getPackaged5kg() * 5.0) +
                    b.getStockKg();
            b.setTotalKg(totalKg);

            batches.add(b);
        }
        return batches;
    }

    public static Map<String, Object> getInventoryOverview() {
        String sql = "SELECT " +
                "COUNT(p.product_id) AS total_products, " +
                "SUM(i.stock_sacks) AS total_sacks, " +
                "SUM(i.stock_kg) AS total_loose_kg, " +
                "SUM(i.packaged_25kg) AS total_25kg, " +
                "SUM(i.packaged_20kg) AS total_20kg, " +
                "SUM(i.packaged_10kg) AS total_10kg, " +
                "SUM(i.packaged_5kg) AS total_5kg, " +
                "SUM((i.stock_sacks * COALESCE(p.kg_per_sack, 50)) + " +
                "    (i.packaged_25kg * 25) + (i.packaged_20kg * 20) + (i.packaged_10kg * 10) + (i.packaged_5kg * 5) + i.stock_kg) AS total_weight_kg, " +
                "SUM((i.stock_sacks * p.price_sack) + (i.stock_kg * p.price_kg) + " +
                "    (i.packaged_25kg * p.price_25kg) + (i.packaged_20kg * p.price_20kg) + (i.packaged_10kg * p.price_10kg) + (i.packaged_5kg * p.price_5kg)) AS total_inventory_value " +
                "FROM product p " +
                "LEFT JOIN inventory i ON p.product_id = i.product_id " +
                "WHERE p.product_status = 'Active'";

        Map<String, Object> summary = Database.queryForMap(sql);
        if (summary == null) summary = new HashMap<>();

        // Low stock count
        String lowStockSql = "SELECT COUNT(*) AS low_stock_count " +
                "FROM product p JOIN inventory i ON p.product_id = i.product_id " +
                "WHERE p.product_status = 'Active' AND " +
                "((i.stock_sacks * COALESCE(p.kg_per_sack, 50)) + (i.packaged_25kg * 25) + (i.packaged_20kg * 20) + (i.packaged_10kg * 10) + (i.packaged_5kg * 5) + i.stock_kg) <= i.reorder_level_kg";

        Map<String, Object> lowStockRow = Database.queryForMap(lowStockSql);
        summary.put("low_stock_count", lowStockRow != null && lowStockRow.get("low_stock_count") != null ? lowStockRow.get("low_stock_count") : 0);

        return summary;
    }

    public static void openSack(int productId, String batchCode, int sacksToOpen, String performedBy) throws Exception {
        if (sacksToOpen <= 0) throw new Exception("Number of sacks to open must be at least 1.");

        Database.inTransaction(conn -> {
            // Check inventory
            String checkInv = "SELECT stock_sacks, stock_kg FROM inventory WHERE product_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(checkInv)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new Exception("Product inventory not found.");
                    int currentSacks = rs.getInt("stock_sacks");
                    if (currentSacks < sacksToOpen) {
                        throw new Exception("Insufficient whole sacks in stock. Available: " + currentSacks);
                    }
                }
            }

            // Deduct from batch if batchCode provided, else oldest active batch
            String targetBatch = batchCode;
            int batchId = 0;
            if (targetBatch != null && !targetBatch.trim().isEmpty()) {
                String checkBatch = "SELECT batch_id, stock_sacks FROM inventory_batch WHERE product_id = ? AND batch_code = ? AND status = 'Active' FOR UPDATE";
                try (PreparedStatement ps = conn.prepareStatement(checkBatch)) {
                    ps.setInt(1, productId);
                    ps.setString(2, targetBatch);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            batchId = rs.getInt("batch_id");
                            int bSacks = rs.getInt("stock_sacks");
                            if (bSacks < sacksToOpen) {
                                throw new Exception("Batch " + targetBatch + " only has " + bSacks + " sacks.");
                            }
                        }
                    }
                }
            } else {
                // Find oldest active batch with sacks
                String oldestBatch = "SELECT batch_id, batch_code, stock_sacks FROM inventory_batch WHERE product_id = ? AND status = 'Active' AND stock_sacks >= ? ORDER BY received_date ASC LIMIT 1 FOR UPDATE";
                try (PreparedStatement ps = conn.prepareStatement(oldestBatch)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, sacksToOpen);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            batchId = rs.getInt("batch_id");
                            targetBatch = rs.getString("batch_code");
                        }
                    }
                }
            }

            // Update inventory
            double kgToAdd = sacksToOpen * 50.0;
            String updInv = "UPDATE inventory SET stock_sacks = stock_sacks - ?, stock_kg = stock_kg + ?, last_updated = NOW() WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updInv)) {
                ps.setInt(1, sacksToOpen);
                ps.setDouble(2, kgToAdd);
                ps.setInt(3, productId);
                ps.executeUpdate();
            }

            // Update batch
            if (batchId > 0) {
                String updBatch = "UPDATE inventory_batch SET stock_sacks = stock_sacks - ?, stock_kg = stock_kg + ? WHERE batch_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updBatch)) {
                    ps.setInt(1, sacksToOpen);
                    ps.setDouble(2, kgToAdd);
                    ps.setInt(3, batchId);
                    ps.executeUpdate();
                }
            }

            // Log adjustment
            String logSql = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, ?, NOW(), 'open_sack', ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                ps.setInt(1, productId);
                ps.setString(2, targetBatch != null ? targetBatch : "BATCH-N/A");
                ps.setInt(3, sacksToOpen);
                ps.setDouble(4, kgToAdd);
                ps.setString(5, "Opened " + sacksToOpen + " sack(s) (" + kgToAdd + "kg) into loose selling bin.");
                ps.setString(6, performedBy != null ? performedBy : "System");
                ps.executeUpdate();
            }

            return null;
        });
    }

    public static void restock(int productId, String batchCode, String receivedDate,
                               int sacks, int packaged25kg, int packaged20kg, int packaged10kg, int packaged5kg, double looseKg,
                               String note, String performedBy) throws Exception {

        if (sacks <= 0 && packaged25kg <= 0 && packaged20kg <= 0 && packaged10kg <= 0 && packaged5kg <= 0 && looseKg <= 0) {
            throw new Exception("Must specify at least one quantity to restock.");
        }

        if (receivedDate == null || receivedDate.trim().isEmpty()) {
            receivedDate = LocalDate.now().toString();
        }

        final String fBatch = (batchCode != null && !batchCode.trim().isEmpty()) ? batchCode.trim() : "BATCH-" + System.currentTimeMillis();
        final String fDate = receivedDate;

        Database.inTransaction(conn -> {
            // Update inventory
            String updInv = "UPDATE inventory SET " +
                    "stock_sacks = stock_sacks + ?, " +
                    "packaged_25kg = packaged_25kg + ?, " +
                    "packaged_20kg = packaged_20kg + ?, " +
                    "packaged_10kg = packaged_10kg + ?, " +
                    "packaged_5kg = packaged_5kg + ?, " +
                    "stock_kg = stock_kg + ?, " +
                    "batch_number = ?, " +
                    "received_date = ?, " +
                    "last_updated = NOW() " +
                    "WHERE product_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(updInv)) {
                ps.setInt(1, sacks);
                ps.setInt(2, packaged25kg);
                ps.setInt(3, packaged20kg);
                ps.setInt(4, packaged10kg);
                ps.setInt(5, packaged5kg);
                ps.setDouble(6, looseKg);
                ps.setString(7, fBatch);
                ps.setString(8, fDate);
                ps.setInt(9, productId);
                ps.executeUpdate();
            }

            // Create new batch lot
            String insBatch = "INSERT INTO inventory_batch " +
                    "(product_id, batch_code, received_date, stock_sacks, packaged_25kg, packaged_20kg, packaged_10kg, packaged_5kg, stock_kg, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active', NOW())";

            try (PreparedStatement ps = conn.prepareStatement(insBatch)) {
                ps.setInt(1, productId);
                ps.setString(2, fBatch);
                ps.setString(3, fDate);
                ps.setInt(4, sacks);
                ps.setInt(5, packaged25kg);
                ps.setInt(6, packaged20kg);
                ps.setInt(7, packaged10kg);
                ps.setInt(8, packaged5kg);
                ps.setDouble(9, looseKg);
                ps.executeUpdate();
            }

            // Log adjustment
            double totalRestockKg = (sacks * 50.0) + (packaged25kg * 25.0) + (packaged20kg * 20.0) + (packaged10kg * 10.0) + (packaged5kg * 5.0) + looseKg;
            String logSql = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, ?, NOW(), 'restock', ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                ps.setInt(1, productId);
                ps.setString(2, fBatch);
                ps.setInt(3, sacks);
                ps.setDouble(4, totalRestockKg);
                ps.setString(5, (note != null && !note.trim().isEmpty()) ? note : "Restocked " + sacks + " sacks and packaging packages (" + totalRestockKg + "kg total).");
                ps.setString(6, performedBy != null ? performedBy : "System");
                ps.executeUpdate();
            }

            return null;
        });
    }

    public static void recordSpoilage(int productId, String batchCode, int sacks, double kg, String note, String performedBy) throws Exception {
        if (sacks <= 0 && kg <= 0) {
            throw new Exception("Must enter spoilage sacks or kg.");
        }

        Database.inTransaction(conn -> {
            // Deduct from inventory
            String updInv = "UPDATE inventory SET " +
                    "stock_sacks = GREATEST(0, stock_sacks - ?), " +
                    "stock_kg = GREATEST(0.0, stock_kg - ?), " +
                    "last_updated = NOW() " +
                    "WHERE product_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(updInv)) {
                ps.setInt(1, sacks);
                ps.setDouble(2, kg);
                ps.setInt(3, productId);
                ps.executeUpdate();
            }

            // Deduct from batch if specified
            if (batchCode != null && !batchCode.trim().isEmpty()) {
                String updBatch = "UPDATE inventory_batch SET " +
                        "stock_sacks = GREATEST(0, stock_sacks - ?), " +
                        "stock_kg = GREATEST(0.0, stock_kg - ?) " +
                        "WHERE product_id = ? AND batch_code = ?";
                try (PreparedStatement ps = conn.prepareStatement(updBatch)) {
                    ps.setInt(1, sacks);
                    ps.setDouble(2, kg);
                    ps.setInt(3, productId);
                    ps.setString(4, batchCode);
                    ps.executeUpdate();
                }
            }

            // Log adjustment
            double totalSpoiledKg = (sacks * 50.0) + kg;
            String logSql = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, ?, NOW(), 'spoilage', ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                ps.setInt(1, productId);
                ps.setString(2, batchCode != null ? batchCode : "BATCH-N/A");
                ps.setInt(3, sacks);
                ps.setDouble(4, totalSpoiledKg);
                ps.setString(5, (note != null && !note.trim().isEmpty()) ? note : "Recorded spoilage: " + sacks + " sacks, " + kg + "kg.");
                ps.setString(6, performedBy != null ? performedBy : "System");
                ps.executeUpdate();
            }

            return null;
        });
    }

    public static void repack(int productId, String batchCode, String sourceUnit, int sourceQty,
                              String targetUnit, int targetQty, String note, String performedBy) throws Exception {

        if (sourceQty <= 0 || targetQty <= 0) {
            throw new Exception("Quantities must be greater than zero.");
        }

        Database.inTransaction(conn -> {
            // Deduct source unit
            String deductCol;
            switch (sourceUnit.toLowerCase()) {
                case "sack": deductCol = "stock_sacks"; break;
                case "25kg": deductCol = "packaged_25kg"; break;
                case "20kg": deductCol = "packaged_20kg"; break;
                case "10kg": deductCol = "packaged_10kg"; break;
                case "5kg": deductCol = "packaged_5kg"; break;
                default: throw new Exception("Invalid source unit: " + sourceUnit);
            }

            String addCol;
            switch (targetUnit.toLowerCase()) {
                case "25kg": addCol = "packaged_25kg"; break;
                case "20kg": addCol = "packaged_20kg"; break;
                case "10kg": addCol = "packaged_10kg"; break;
                case "5kg": addCol = "packaged_5kg"; break;
                default: throw new Exception("Invalid target unit: " + targetUnit);
            }

            String checkSql = "SELECT " + deductCol + " FROM inventory WHERE product_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) < sourceQty) {
                        throw new Exception("Insufficient source stock for repacking.");
                    }
                }
            }

            String updInv = "UPDATE inventory SET " + deductCol + " = " + deductCol + " - ?, " +
                    addCol + " = " + addCol + " + ?, last_updated = NOW() WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updInv)) {
                ps.setInt(1, sourceQty);
                ps.setInt(2, targetQty);
                ps.setInt(3, productId);
                ps.executeUpdate();
            }

            // Log adjustment
            String logSql = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, ?, NOW(), 'repack', ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                ps.setInt(1, productId);
                ps.setString(2, batchCode != null ? batchCode : "BATCH-N/A");
                ps.setInt(3, sourceUnit.equalsIgnoreCase("sack") ? sourceQty : 0);
                ps.setDouble(4, 0.0);
                ps.setString(5, "Repacked " + sourceQty + " " + sourceUnit + " into " + targetQty + " " + targetUnit + ". " + (note != null ? note : ""));
                ps.setString(6, performedBy != null ? performedBy : "System");
                ps.executeUpdate();
            }

            return null;
        });
    }

    public static void updateReorderLevel(int productId, double reorderLevelKg) throws Exception {
        if (reorderLevelKg < 0) throw new Exception("Reorder level cannot be negative.");
        Database.update("UPDATE inventory SET reorder_level_kg = ?, last_updated = NOW() WHERE product_id = ?", reorderLevelKg, productId);
    }

    public static List<AdjustmentLog> getRecentAdjustments(int limit) {
        String sql = "SELECT a.adjustment_id, a.adjustment_date, a.adjustment_type, a.sacks, a.kg, a.note, a.performed_by, " +
                "a.batch_code, p.product_name, p.origin " +
                "FROM inventory_adjustment a " +
                "LEFT JOIN product p ON a.product_id = p.product_id " +
                "ORDER BY a.adjustment_date DESC LIMIT ?";

        List<Map<String, Object>> rows = Database.queryForList(sql, limit > 0 ? limit : 50);
        List<AdjustmentLog> logs = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            AdjustmentLog l = new AdjustmentLog();
            l.setId(String.valueOf(r.get("adjustment_id")));
            l.setRawDate(String.valueOf(r.get("adjustment_date")));
            l.setFormattedDate(l.getRawDate());
            l.setType(String.valueOf(r.get("adjustment_type")));
            l.setProductName(r.get("product_name") != null ? String.valueOf(r.get("product_name")) : "Unknown Product");
            l.setOrigin(r.get("origin") != null ? String.valueOf(r.get("origin")) : "Local");
            l.setBatchCode(r.get("batch_code") != null ? String.valueOf(r.get("batch_code")) : "N/A");
            int s = r.get("sacks") != null ? ((Number) r.get("sacks")).intValue() : 0;
            double k = r.get("kg") != null ? ((Number) r.get("kg")).doubleValue() : 0.0;
            l.setQtyImpact((s > 0 ? s + " sacks " : "") + (k > 0 ? k + " kg" : (s == 0 ? "0 kg" : "")));
            l.setNote(r.get("note") != null ? String.valueOf(r.get("note")) : "");
            l.setUser(r.get("performed_by") != null ? String.valueOf(r.get("performed_by")) : "System");
            logs.add(l);
        }
        return logs;
    }
}
