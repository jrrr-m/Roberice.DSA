package com.roberice.pos.db;

import com.roberice.pos.model.Inventory;
import com.roberice.pos.model.InventoryBatch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Inventory, FIFO Batches, and Warehouse Adjustments.
 */
public class InventoryDAO {

    public List<Inventory> getInventorySummary() throws SQLException {
        List<Inventory> list = new ArrayList<>();
        String sql = """
            SELECT 
                i.*, 
                p.product_name, 
                p.origin, 
                p.rice_type
            FROM inventory i
            JOIN product p ON i.product_id = p.product_id
            WHERE p.product_status = 'Active'
            ORDER BY i.product_id ASC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Inventory inv = new Inventory();
                inv.setInventoryId(rs.getInt("inventory_id"));
                inv.setProductId(rs.getInt("product_id"));
                inv.setProductName(rs.getString("product_name"));
                inv.setOrigin(rs.getString("origin"));
                inv.setRiceType(rs.getString("rice_type"));
                inv.setStockSacks(rs.getInt("stock_sacks"));
                inv.setPackaged25kg(rs.getInt("packaged_25kg"));
                inv.setPackaged20kg(rs.getInt("packaged_20kg"));
                inv.setPackaged10kg(rs.getInt("packaged_10kg"));
                inv.setPackaged5kg(rs.getInt("packaged_5kg"));
                inv.setStockKg(rs.getDouble("stock_kg"));
                inv.setBatchNumber(rs.getString("batch_number"));
                inv.setReceivedDate(DatabaseManager.parseDate(rs.getObject("received_date")));
                inv.setReorderLevelKg(rs.getDouble("reorder_level_kg"));
                inv.setLastUpdated(DatabaseManager.parseDate(rs.getObject("last_updated")));
                list.add(inv);
            }
        }
        return list;
    }

    public List<InventoryBatch> getBatchesForProduct(int productId, boolean activeOnly) throws SQLException {
        List<InventoryBatch> list = new ArrayList<>();
        String sql = "SELECT b.*, p.product_name FROM inventory_batch b JOIN product p ON b.product_id = p.product_id WHERE b.product_id = ?"
                + (activeOnly ? " AND b.status = 'Active'" : "")
                + " ORDER BY b.received_date ASC, b.batch_id ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryBatch b = new InventoryBatch();
                    b.setBatchId(rs.getInt("batch_id"));
                    b.setProductId(rs.getInt("product_id"));
                    b.setProductName(rs.getString("product_name"));
                    b.setBatchCode(rs.getString("batch_code"));
                    b.setReceivedDate(DatabaseManager.parseDate(rs.getObject("received_date")));
                    b.setStockSacks(rs.getInt("stock_sacks"));
                    b.setPackaged25kg(rs.getInt("packaged_25kg"));
                    b.setPackaged20kg(rs.getInt("packaged_20kg"));
                    b.setPackaged10kg(rs.getInt("packaged_10kg"));
                    b.setPackaged5kg(rs.getInt("packaged_5kg"));
                    b.setStockKg(rs.getDouble("stock_kg"));
                    b.setStatus(rs.getString("status"));
                    b.setCreatedAt(DatabaseManager.parseDate(rs.getObject("created_at")));
                    b.setUpdatedAt(DatabaseManager.parseDate(rs.getObject("updated_at")));
                    list.add(b);
                }
            }
        }
        return list;
    }

    /**
     * Converts whole sacks into loose inventory (50kg per sack).
     */
    public boolean openSack(int productId, String batchCode, int sacksToOpen, String performedBy) throws SQLException {
        if (sacksToOpen <= 0) return false;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Find target batch
                String findBatch = "SELECT batch_id, stock_sacks, stock_kg FROM inventory_batch WHERE product_id = ? AND batch_code = ? AND status = 'Active' LIMIT 1";
                int batchId = -1;
                int currentSacks = 0;
                double currentKg = 0;

                try (PreparedStatement ps = conn.prepareStatement(findBatch)) {
                    ps.setInt(1, productId);
                    ps.setString(2, batchCode);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            batchId = rs.getInt("batch_id");
                            currentSacks = rs.getInt("stock_sacks");
                            currentKg = rs.getDouble("stock_kg");
                        }
                    }
                }

                if (batchId == -1 || currentSacks < sacksToOpen) {
                    // Fallback to any active batch with enough sacks
                    String findAny = "SELECT batch_id, batch_code, stock_sacks, stock_kg FROM inventory_batch WHERE product_id = ? AND status = 'Active' AND stock_sacks >= ? ORDER BY received_date ASC LIMIT 1";
                    try (PreparedStatement ps = conn.prepareStatement(findAny)) {
                        ps.setInt(1, productId);
                        ps.setInt(2, sacksToOpen);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                batchId = rs.getInt("batch_id");
                                batchCode = rs.getString("batch_code");
                                currentSacks = rs.getInt("stock_sacks");
                                currentKg = rs.getDouble("stock_kg");
                            }
                        }
                    }
                }

                if (batchId == -1 || currentSacks < sacksToOpen) {
                    throw new SQLException("Insufficient whole sacks in active batch to open.");
                }

                double addedKg = sacksToOpen * 50.0;
                int newSacks = currentSacks - sacksToOpen;
                double newKg = currentKg + addedKg;

                // Update batch
                String updBatch = "UPDATE inventory_batch SET stock_sacks = ?, stock_kg = ? WHERE batch_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updBatch)) {
                    ps.setInt(1, newSacks);
                    ps.setDouble(2, newKg);
                    ps.setInt(3, batchId);
                    ps.executeUpdate();
                }

                // Log adjustment
                String logSql = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performedBy) "
                        + (DatabaseManager.isMySql() ? "" : "") + "VALUES (?, ?, CURRENT_TIMESTAMP, 'open_sack', ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, ?, CURRENT_TIMESTAMP, 'open_sack', ?, ?, ?, ?)")) {
                    ps.setInt(1, productId);
                    ps.setString(2, batchCode);
                    ps.setInt(3, sacksToOpen);
                    ps.setDouble(4, addedKg);
                    ps.setString(5, "Opened " + sacksToOpen + "x 50kg whole sack from batch " + batchCode + " into loose selling bin.");
                    ps.setString(6, performedBy);
                    ps.executeUpdate();
                }

                syncAggregatedInventory(conn, productId);
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Restocks new rice shipments into inventory and creates a new FIFO lot.
     */
    public boolean restock(int productId, String batchCode, String receivedDate, int sacks, int p25, int p20, int p10, int p5, double kg, String note, String performedBy) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert new lot batch
                String insBatch = "INSERT INTO inventory_batch (product_id, batch_code, received_date, stock_sacks, packaged_25kg, packaged_20kg, packaged_10kg, packaged_5kg, stock_kg, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active')";
                try (PreparedStatement ps = conn.prepareStatement(insBatch)) {
                    ps.setInt(1, productId);
                    ps.setString(2, batchCode);
                    ps.setString(3, receivedDate);
                    ps.setInt(4, sacks);
                    ps.setInt(5, p25);
                    ps.setInt(6, p20);
                    ps.setInt(7, p10);
                    ps.setInt(8, p5);
                    ps.setDouble(9, kg);
                    ps.executeUpdate();
                }

                // Log audit
                String insAdj = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, ?, CURRENT_TIMESTAMP, 'restock', ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insAdj)) {
                    ps.setInt(1, productId);
                    ps.setString(2, batchCode);
                    ps.setInt(3, sacks);
                    ps.setDouble(4, kg);
                    ps.setString(5, note.isEmpty() ? "Shipment batch " + batchCode + " received." : note);
                    ps.setString(6, performedBy);
                    ps.executeUpdate();
                }

                syncAggregatedInventory(conn, productId);
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Repacks loose rice into smaller consumer packaging (5kg, 10kg, 20kg, 25kg).
     */
    public boolean repack(int productId, int p25, int p20, int p10, int p5, String note, String performedBy) throws SQLException {
        double neededKg = (p25 * 25.0) + (p20 * 20.0) + (p10 * 10.0) + (p5 * 5.0);
        if (neededKg <= 0) return false;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check total loose kg available
                String checkKg = "SELECT SUM(stock_kg) FROM inventory_batch WHERE product_id = ? AND status = 'Active'";
                double availKg = 0;
                try (PreparedStatement ps = conn.prepareStatement(checkKg)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) availKg = rs.getDouble(1);
                    }
                }

                if (availKg < neededKg) {
                    throw new SQLException("Insufficient loose rice (need " + neededKg + " kg, available " + availKg + " kg). Please open a sack first.");
                }

                // Deduct from batches in FIFO order
                String getBatches = "SELECT batch_id, batch_code, stock_kg, packaged_25kg, packaged_20kg, packaged_10kg, packaged_5kg FROM inventory_batch WHERE product_id = ? AND status = 'Active' AND stock_kg > 0 ORDER BY received_date ASC";
                double remKg = neededKg;
                int rem25 = p25, rem20 = p20, rem10 = p10, rem5 = p5;

                try (PreparedStatement ps = conn.prepareStatement(getBatches, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next() && remKg > 0.001) {
                        int bId = rs.getInt("batch_id");
                        double bKg = rs.getDouble("stock_kg");
                        double deduct = Math.min(remKg, bKg);
                        double newKg = bKg - deduct;
                        remKg -= deduct;

                        // Add packaged quantities to the batch where repacking took place
                        int cur25 = rs.getInt("packaged_25kg") + rem25;
                        int cur20 = rs.getInt("packaged_20kg") + rem20;
                        int cur10 = rs.getInt("packaged_10kg") + rem10;
                        int cur5 = rs.getInt("packaged_5kg") + rem5;
                        rem25 = 0; rem20 = 0; rem10 = 0; rem5 = 0; // all assigned to this batch

                        String upd = "UPDATE inventory_batch SET stock_kg = ?, packaged_25kg = ?, packaged_20kg = ?, packaged_10kg = ?, packaged_5kg = ? WHERE batch_id = ?";
                        try (PreparedStatement psU = conn.prepareStatement(upd)) {
                            psU.setDouble(1, newKg);
                            psU.setInt(2, cur25);
                            psU.setInt(3, cur20);
                            psU.setInt(4, cur10);
                            psU.setInt(5, cur5);
                            psU.setInt(6, bId);
                            psU.executeUpdate();
                        }
                    }
                }

                // Log audit
                String logSql = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, 'REPACK', CURRENT_TIMESTAMP, 'repack', 0, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                    ps.setInt(1, productId);
                    ps.setDouble(2, neededKg);
                    ps.setString(3, "Repacked " + neededKg + "kg loose into "
                            + (p25 > 0 ? p25 + "x 25kg, " : "")
                            + (p20 > 0 ? p20 + "x 20kg, " : "")
                            + (p10 > 0 ? p10 + "x 10kg, " : "")
                            + (p5 > 0 ? p5 + "x 5kg. " : "") + note);
                    ps.setString(4, performedBy);
                    ps.executeUpdate();
                }

                syncAggregatedInventory(conn, productId);
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Records physical inventory counts or spoilage write-offs.
     */
    public boolean recordAdjustment(int productId, String type, int sacks, int p25, int p20, int p10, int p5, double kg, String note, String performedBy) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Apply change to latest active batch
                String findLatest = "SELECT batch_id, batch_code, stock_sacks, packaged_25kg, packaged_20kg, packaged_10kg, packaged_5kg, stock_kg FROM inventory_batch WHERE product_id = ? AND status = 'Active' ORDER BY received_date DESC LIMIT 1";
                int bId = -1;
                String bCode = "BATCH-ADJ";

                try (PreparedStatement ps = conn.prepareStatement(findLatest)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            bId = rs.getInt("batch_id");
                            bCode = rs.getString("batch_code");
                            int newSacks = Math.max(0, rs.getInt("stock_sacks") + ("spoilage".equals(type) ? -sacks : sacks));
                            int new25 = Math.max(0, rs.getInt("packaged_25kg") + ("spoilage".equals(type) ? -p25 : p25));
                            int new20 = Math.max(0, rs.getInt("packaged_20kg") + ("spoilage".equals(type) ? -p20 : p20));
                            int new10 = Math.max(0, rs.getInt("packaged_10kg") + ("spoilage".equals(type) ? -p10 : p10));
                            int new5 = Math.max(0, rs.getInt("packaged_5kg") + ("spoilage".equals(type) ? -p5 : p5));
                            double newKg = Math.max(0, rs.getDouble("stock_kg") + ("spoilage".equals(type) ? -kg : kg));

                            String upd = "UPDATE inventory_batch SET stock_sacks = ?, packaged_25kg = ?, packaged_20kg = ?, packaged_10kg = ?, packaged_5kg = ?, stock_kg = ? WHERE batch_id = ?";
                            try (PreparedStatement psU = conn.prepareStatement(upd)) {
                                psU.setInt(1, newSacks);
                                psU.setInt(2, new25);
                                psU.setInt(3, new20);
                                psU.setInt(4, new10);
                                psU.setInt(5, new5);
                                psU.setDouble(6, newKg);
                                psU.setInt(7, bId);
                                psU.executeUpdate();
                            }
                        }
                    }
                }

                // Log audit
                String logSql = "INSERT INTO inventory_adjustment (product_id, batch_code, adjustment_date, adjustment_type, sacks, kg, note, performed_by) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                    ps.setInt(1, productId);
                    ps.setString(2, bCode);
                    ps.setString(3, type);
                    ps.setInt(4, sacks);
                    ps.setDouble(5, kg);
                    ps.setString(6, note);
                    ps.setString(7, performedBy);
                    ps.executeUpdate();
                }

                syncAggregatedInventory(conn, productId);
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Synchronizes the inventory table with the sum of active batches for this product.
     */
    public void syncAggregatedInventory(Connection conn, int productId) throws SQLException {
        String sumSql = """
            SELECT 
                COALESCE(SUM(stock_sacks), 0) AS total_sacks,
                COALESCE(SUM(packaged_25kg), 0) AS total_25kg,
                COALESCE(SUM(packaged_20kg), 0) AS total_20kg,
                COALESCE(SUM(packaged_10kg), 0) AS total_10kg,
                COALESCE(SUM(packaged_5kg), 0) AS total_5kg,
                COALESCE(SUM(stock_kg), 0.0) AS total_kg,
                MAX(batch_code) AS latest_batch,
                MAX(received_date) AS latest_date
            FROM inventory_batch 
            WHERE product_id = ? AND status = 'Active'
        """;

        int sacks = 0, p25 = 0, p20 = 0, p10 = 0, p5 = 0;
        double kg = 0;
        String latestBatch = "BATCH-001";
        java.util.Date latestDate = new java.util.Date();

        try (PreparedStatement ps = conn.prepareStatement(sumSql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sacks = rs.getInt("total_sacks");
                    p25 = rs.getInt("total_25kg");
                    p20 = rs.getInt("total_20kg");
                    p10 = rs.getInt("total_10kg");
                    p5 = rs.getInt("total_5kg");
                    kg = rs.getDouble("total_kg");
                    if (rs.getString("latest_batch") != null) {
                        latestBatch = rs.getString("latest_batch");
                    }
                    if (rs.getObject("latest_date") != null) {
                        java.util.Date d = DatabaseManager.parseDate(rs.getObject("latest_date"));
                        if (d != null) latestDate = d;
                    }
                }
            }
        }

        String updInv = """
            UPDATE inventory SET 
                stock_sacks = ?, 
                packaged_25kg = ?, 
                packaged_20kg = ?, 
                packaged_10kg = ?, 
                packaged_5kg = ?, 
                stock_kg = ?, 
                batch_number = ?, 
                received_date = ?, 
                last_updated = CURRENT_TIMESTAMP
            WHERE product_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(updInv)) {
            ps.setInt(1, sacks);
            ps.setInt(2, p25);
            ps.setInt(3, p20);
            ps.setInt(4, p10);
            ps.setInt(5, p5);
            ps.setDouble(6, kg);
            ps.setString(7, latestBatch);
            ps.setString(8, new java.text.SimpleDateFormat("yyyy-MM-dd").format(latestDate));
            ps.setInt(9, productId);
            ps.executeUpdate();
        }
    }
}
