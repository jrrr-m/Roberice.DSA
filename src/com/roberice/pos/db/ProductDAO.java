package com.roberice.pos.db;

import com.roberice.pos.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product catalog management.
 */
public class ProductDAO {

    public List<Product> getAllProducts(boolean activeOnly) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM product" + (activeOnly ? " WHERE product_status = 'Active'" : "") + " ORDER BY product_id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT * FROM product WHERE product_id = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public boolean addProduct(Product p) throws SQLException {
        String sql = "INSERT INTO product (product_name, category, origin, rice_type, description, price_sack, price_25kg, price_20kg, price_10kg, price_5kg, price_kg, kg_per_sack, product_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getOrigin());
            ps.setString(4, p.getRiceType());
            ps.setString(5, p.getDescription());
            ps.setDouble(6, p.getPriceSack());
            ps.setDouble(7, p.getPrice25kg());
            ps.setDouble(8, p.getPrice20kg());
            ps.setDouble(9, p.getPrice10kg());
            ps.setDouble(10, p.getPrice5kg());
            ps.setDouble(11, p.getPriceKg());
            ps.setDouble(12, p.getKgPerSack());
            ps.setString(13, p.getProductStatus());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        int newId = gk.getInt(1);
                        p.setProductId(newId);
                        // Initialize inventory record
                        initProductInventory(conn, newId);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void initProductInventory(Connection conn, int productId) throws SQLException {
        String sql = "INSERT INTO inventory (product_id, stock_sacks, packaged_25kg, packaged_20kg, packaged_10kg, packaged_5kg, stock_kg, batch_number, received_date, reorder_level_kg) VALUES (?, 0, 0, 0, 0, 0, 0.00, 'BATCH-NEW', CURRENT_DATE, 200.00)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }

    public boolean updateProduct(Product p) throws SQLException {
        String sql = "UPDATE product SET product_name = ?, category = ?, origin = ?, rice_type = ?, description = ?, price_sack = ?, price_25kg = ?, price_20kg = ?, price_10kg = ?, price_5kg = ?, price_kg = ?, kg_per_sack = ?, product_status = ? WHERE product_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getOrigin());
            ps.setString(4, p.getRiceType());
            ps.setString(5, p.getDescription());
            ps.setDouble(6, p.getPriceSack());
            ps.setDouble(7, p.getPrice25kg());
            ps.setDouble(8, p.getPrice20kg());
            ps.setDouble(9, p.getPrice10kg());
            ps.setDouble(10, p.getPrice5kg());
            ps.setDouble(11, p.getPriceKg());
            ps.setDouble(12, p.getKgPerSack());
            ps.setString(13, p.getProductStatus());
            ps.setInt(14, p.getProductId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean setProductStatus(int productId, String status) throws SQLException {
        String sql = "UPDATE product SET product_status = ? WHERE product_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapResultSet(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("category"),
                rs.getString("origin"),
                rs.getString("rice_type"),
                rs.getString("description"),
                rs.getDouble("price_sack"),
                rs.getDouble("price_25kg"),
                rs.getDouble("price_20kg"),
                rs.getDouble("price_10kg"),
                rs.getDouble("price_5kg"),
                rs.getDouble("price_kg"),
                rs.getDouble("kg_per_sack"),
                rs.getString("product_status")
        );
    }
}
