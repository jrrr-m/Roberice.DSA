package com.roberice.dsa.service;

import com.roberice.dsa.db.Database;
import com.roberice.dsa.model.Product;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProductService {

    public static List<Product> getProducts() {
        String sql = "SELECT " +
                "p.product_id, p.product_name, p.category, p.origin, p.rice_type, p.description, " +
                "p.price_sack, p.price_25kg, p.price_kg, p.price_5kg, p.price_10kg, p.price_20kg, p.kg_per_sack, p.product_status, " +
                "i.inventory_id, i.stock_sacks, i.packaged_25kg, i.stock_kg, i.packaged_5kg, i.packaged_10kg, i.packaged_20kg, " +
                "i.received_date, i.batch_number, DATEDIFF(CURRENT_DATE, COALESCE(i.received_date, CURRENT_DATE)) AS days_old, " +
                "i.reorder_level_kg " +
                "FROM product p LEFT JOIN inventory i ON p.product_id = i.product_id " +
                "WHERE p.product_status = 'Active' " +
                "ORDER BY p.product_name";

        List<Map<String, Object>> rows = Database.queryForList(sql);
        List<Product> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Product p = new Product();
            p.setProductId(getInt(r.get("product_id"), 0));
            p.setProductName(String.valueOf(r.get("product_name")));
            p.setCategory(String.valueOf(r.get("category")));
            p.setOrigin(String.valueOf(r.get("origin")));
            p.setRiceType(String.valueOf(r.get("rice_type")));
            p.setDescription(r.get("description") != null ? String.valueOf(r.get("description")) : "");
            p.setPriceSack(getDouble(r.get("price_sack"), 0.0));
            p.setPrice25kg(getDouble(r.get("price_25kg"), 0.0));
            p.setPriceKg(getDouble(r.get("price_kg"), 0.0));
            p.setPrice5kg(getDouble(r.get("price_5kg"), 0.0));
            p.setPrice10kg(getDouble(r.get("price_10kg"), 0.0));
            p.setPrice20kg(getDouble(r.get("price_20kg"), 0.0));
            p.setKgPerSack(getDouble(r.get("kg_per_sack"), 50.0));
            p.setProductStatus(String.valueOf(r.get("product_status")));

            p.setStockSacks(getInt(r.get("stock_sacks"), 0));
            p.setPackaged25kg(getInt(r.get("packaged_25kg"), 0));
            p.setStockKg(getDouble(r.get("stock_kg"), 0.0));
            p.setPackaged5kg(getInt(r.get("packaged_5kg"), 0));
            p.setPackaged10kg(getInt(r.get("packaged_10kg"), 0));
            p.setPackaged20kg(getInt(r.get("packaged_20kg"), 0));
            p.setReceivedDate(r.get("received_date") != null ? String.valueOf(r.get("received_date")) : "");
            p.setBatchNumber(r.get("batch_number") != null ? String.valueOf(r.get("batch_number")) : "");
            p.setDaysOld(getInt(r.get("days_old"), 0));
            p.setReorderLevelKg(getDouble(r.get("reorder_level_kg"), 100.0));

            list.add(p);
        }
        return list;
    }

    public static void addProduct(String name, String category, String origin, String riceType,
                                  double priceSack, double price25kg, double priceKg, double price5kg, double price10kg, double price20kg,
                                  int initialSacks, double initialKg, int packaged25kg, int packaged5kg, int packaged10kg, int packaged20kg,
                                  String batchNumber, String receivedDate, double thresholdKg) throws Exception {

        if (name == null || name.trim().isEmpty()) {
            throw new Exception("Product name is required.");
        }

        if (!"Local".equalsIgnoreCase(origin) && !"Imported".equalsIgnoreCase(origin)) {
            origin = "Local";
        }
        if (riceType == null || riceType.trim().isEmpty()) {
            riceType = category;
        }

        if (price25kg <= 0) price25kg = priceSack / 2.0;
        if (price5kg <= 0) price5kg = priceKg * 5.0;
        if (price10kg <= 0) price10kg = priceKg * 10.0;
        if (price20kg <= 0) price20kg = priceKg * 20.0;

        String cleanPrefix = name.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (cleanPrefix.length() > 3) cleanPrefix = cleanPrefix.substring(0, 3);
        if (cleanPrefix.isEmpty()) cleanPrefix = "RIC";
        String ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        if (batchNumber == null || batchNumber.trim().isEmpty()) {
            batchNumber = "BATCH-" + cleanPrefix + "-" + ym;
        }
        if (receivedDate == null || receivedDate.trim().isEmpty()) {
            receivedDate = LocalDate.now().toString();
        }

        final String fOrigin = origin;
        final String fRiceType = riceType;
        final double fPriceSack = priceSack;
        final double fPrice25kg = price25kg;
        final double fPriceKg = priceKg;
        final double fPrice5kg = price5kg;
        final double fPrice10kg = price10kg;
        final double fPrice20kg = price20kg;
        final String fBatch = batchNumber;
        final String fRecDate = receivedDate;

        Database.inTransaction(conn -> {
            String insProd = "INSERT INTO product (product_name, category, origin, rice_type, description, price_sack, price_25kg, price_kg, price_5kg, price_10kg, price_20kg, kg_per_sack, product_status) " +
                    "VALUES (?, ?, ?, ?, '', ?, ?, ?, ?, ?, ?, 50.00, 'Active')";
            long pId;
            try (var ps = conn.prepareStatement(insProd, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name.trim());
                ps.setString(2, category);
                ps.setString(3, fOrigin);
                ps.setString(4, fRiceType);
                ps.setDouble(5, fPriceSack);
                ps.setDouble(6, fPrice25kg);
                ps.setDouble(7, fPriceKg);
                ps.setDouble(8, fPrice5kg);
                ps.setDouble(9, fPrice10kg);
                ps.setDouble(10, fPrice20kg);
                ps.executeUpdate();
                try (var rs = ps.getGeneratedKeys()) {
                    rs.next();
                    pId = rs.getLong(1);
                }
            }

            String insInv = "INSERT INTO inventory (product_id, stock_sacks, packaged_25kg, stock_kg, packaged_5kg, packaged_10kg, packaged_20kg, received_date, batch_number, reorder_level_kg) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (var ps = conn.prepareStatement(insInv)) {
                ps.setLong(1, pId);
                ps.setInt(2, initialSacks);
                ps.setInt(3, packaged25kg);
                ps.setDouble(4, initialKg);
                ps.setInt(5, packaged5kg);
                ps.setInt(6, packaged10kg);
                ps.setInt(7, packaged20kg);
                ps.setString(8, fRecDate);
                ps.setString(9, fBatch);
                ps.setDouble(10, thresholdKg);
                ps.executeUpdate();
            }

            double totalKg = (initialSacks * 50.0) + (packaged25kg * 25.0) + (packaged20kg * 20.0) + (packaged10kg * 10.0) + (packaged5kg * 5.0) + initialKg;
            if (totalKg > 0) {
                String insBatch = "INSERT INTO inventory_batch (product_id, batch_code, received_date, stock_sacks, packaged_25kg, packaged_20kg, packaged_10kg, packaged_5kg, stock_kg, status, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active', NOW(), NOW())";
                try (var ps = conn.prepareStatement(insBatch)) {
                    ps.setLong(1, pId);
                    ps.setString(2, fBatch);
                    ps.setString(3, fRecDate);
                    ps.setInt(4, initialSacks);
                    ps.setInt(5, packaged25kg);
                    ps.setInt(6, packaged20kg);
                    ps.setInt(7, packaged10kg);
                    ps.setInt(8, packaged5kg);
                    ps.setDouble(9, initialKg);
                    ps.executeUpdate();
                }
            }
            return null;
        });
    }

    public static void updateProduct(int productId, String name, String category, String origin, String riceType, String description,
                                     double priceSack, double price25kg, double priceKg, double price5kg, double price10kg, double price20kg,
                                     double thresholdKg) throws Exception {

        if (name == null || name.trim().isEmpty()) {
            throw new Exception("Product name is required.");
        }

        if (price25kg <= 0) price25kg = priceSack / 2.0;
        if (price5kg <= 0) price5kg = priceKg * 5.0;
        if (price10kg <= 0) price10kg = priceKg * 10.0;
        if (price20kg <= 0) price20kg = priceKg * 20.0;

        Database.update(
                "UPDATE product SET product_name = ?, category = ?, origin = ?, rice_type = ?, description = ?, price_sack = ?, price_25kg = ?, price_kg = ?, price_5kg = ?, price_10kg = ?, price_20kg = ? WHERE product_id = ?",
                name.trim(), category, origin, riceType, description, priceSack, price25kg, priceKg, price5kg, price10kg, price20kg, productId
        );

        Database.update("UPDATE inventory SET reorder_level_kg = ? WHERE product_id = ?", thresholdKg, productId);
    }

    public static void updateProduct(int productId, String name, String category, String origin, String riceType,
                                     double priceSack, double price25kg, double priceKg, double price5kg, double price10kg, double price20kg,
                                     double thresholdKg) throws Exception {
        updateProduct(productId, name, category, origin, riceType, "", priceSack, price25kg, priceKg, price5kg, price10kg, price20kg, thresholdKg);
    }

    public static void deleteProduct(int productId) {
        Database.update("UPDATE product SET product_status = 'Inactive' WHERE product_id = ?", productId);
    }

    private static int getInt(Object obj, int def) {
        if (obj == null) return def;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try { return Integer.parseInt(String.valueOf(obj).trim()); } catch (Exception e) { return def; }
    }

    private static double getDouble(Object obj, double def) {
        if (obj == null) return def;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try { return Double.parseDouble(String.valueOf(obj).trim()); } catch (Exception e) { return def; }
    }
}
