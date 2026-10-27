package com.roberice.pos.db;

import com.roberice.pos.model.InventoryAdjustment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Warehouse Audit Trail logs.
 */
public class AuditDAO {

    public List<InventoryAdjustment> getAuditLogs(String typeFilter, int limit) throws SQLException {
        List<InventoryAdjustment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT 
                a.*, 
                p.product_name 
            FROM inventory_adjustment a
            LEFT JOIN product p ON a.product_id = p.product_id
        """);

        boolean hasFilter = typeFilter != null && !typeFilter.equalsIgnoreCase("all") && !typeFilter.trim().isEmpty();
        if (hasFilter) {
            sql.append(" WHERE a.adjustment_type = ? ");
        }

        sql.append(" ORDER BY a.adjustment_id DESC LIMIT ?");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (hasFilter) {
                ps.setString(idx++, typeFilter.toLowerCase());
            }
            ps.setInt(idx, limit > 0 ? limit : 250);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryAdjustment adj = new InventoryAdjustment();
                    adj.setAdjustmentId(rs.getInt("adjustment_id"));
                    adj.setProductId(rs.getInt("product_id"));
                    adj.setProductName(rs.getString("product_name") != null ? rs.getString("product_name") : "Variety #" + adj.getProductId());
                    adj.setBatchCode(rs.getString("batch_code"));
                    adj.setAdjustmentDate(DatabaseManager.parseDate(rs.getObject("adjustment_date")));
                    adj.setAdjustmentType(rs.getString("adjustment_type"));
                    adj.setSacks(rs.getInt("sacks"));
                    adj.setKg(rs.getDouble("kg"));
                    adj.setNote(rs.getString("note"));
                    adj.setPerformedBy(rs.getString("performed_by"));
                    list.add(adj);
                }
            }
        }
        return list;
    }
}
