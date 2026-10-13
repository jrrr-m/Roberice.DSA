package com.roberice.dsa.service;

import com.roberice.dsa.db.Database;
import com.roberice.dsa.model.AdjustmentLog;

import java.util.*;

public class AuditService {

    public static List<AdjustmentLog> getAuditLogs(String typeFilter, String search, int limit) {
        List<AdjustmentLog> list = new ArrayList<>();

        boolean includeAdj = typeFilter == null || "All".equalsIgnoreCase(typeFilter) || "Inventory".equalsIgnoreCase(typeFilter) || "Adjustments".equalsIgnoreCase(typeFilter);
        boolean includeSales = typeFilter == null || "All".equalsIgnoreCase(typeFilter) || "Sales".equalsIgnoreCase(typeFilter);

        if (includeAdj) {
            String adjSql = "SELECT a.adjustment_id, a.adjustment_date, a.adjustment_type, a.sacks, a.kg, a.note, a.performed_by, " +
                    "a.batch_code, p.product_name, p.origin " +
                    "FROM inventory_adjustment a " +
                    "LEFT JOIN product p ON a.product_id = p.product_id " +
                    "ORDER BY a.adjustment_date DESC LIMIT ?";

            List<Map<String, Object>> adjRows = Database.queryForList(adjSql, limit > 0 ? limit : 100);
            for (Map<String, Object> r : adjRows) {
                AdjustmentLog log = new AdjustmentLog();
                log.setId("ADJ-" + r.get("adjustment_id"));
                log.setRawDate(String.valueOf(r.get("adjustment_date")));
                log.setFormattedDate(log.getRawDate());
                String type = String.valueOf(r.get("adjustment_type"));
                log.setType(type);
                log.setLabel(type.toUpperCase().replace('_', ' '));
                log.setCategory("Inventory");
                log.setProductName(r.get("product_name") != null ? String.valueOf(r.get("product_name")) : "Product");
                log.setOrigin(r.get("origin") != null ? String.valueOf(r.get("origin")) : "Local");
                log.setBatchCode(r.get("batch_code") != null ? String.valueOf(r.get("batch_code")) : "N/A");

                int s = r.get("sacks") != null ? ((Number) r.get("sacks")).intValue() : 0;
                double k = r.get("kg") != null ? ((Number) r.get("kg")).doubleValue() : 0.0;
                log.setQtyImpact((s > 0 ? s + " sacks " : "") + (k > 0 ? String.format("%.2f kg", k) : (s == 0 ? "0 kg" : "")));

                log.setNote(r.get("note") != null ? String.valueOf(r.get("note")) : "");
                log.setUser(r.get("performed_by") != null ? String.valueOf(r.get("performed_by")) : "System");
                list.add(log);
            }
        }

        if (includeSales) {
            String txSql = "SELECT t.transaction_id, t.transaction_date, t.total_amount, t.payment_method, t.transaction_status, " +
                    "u.username, u.full_name " +
                    "FROM sales_transaction t " +
                    "LEFT JOIN users u ON t.user_id = u.user_id " +
                    "ORDER BY t.transaction_date DESC LIMIT ?";

            List<Map<String, Object>> txRows = Database.queryForList(txSql, limit > 0 ? limit : 100);
            for (Map<String, Object> r : txRows) {
                AdjustmentLog log = new AdjustmentLog();
                log.setId("TX-" + r.get("transaction_id"));
                log.setRawDate(String.valueOf(r.get("transaction_date")));
                log.setFormattedDate(log.getRawDate());
                log.setType("sale");
                log.setLabel("SALE (" + r.get("payment_method") + ")");
                log.setCategory("POS");
                log.setProductName("Order #" + r.get("transaction_id"));
                log.setOrigin("-");
                log.setBatchCode("-");
                double amt = r.get("total_amount") != null ? ((Number) r.get("total_amount")).doubleValue() : 0.0;
                log.setQtyImpact(String.format("₱%,.2f", amt));
                log.setNote("Status: " + r.get("transaction_status") + " via " + r.get("payment_method"));
                log.setUser(r.get("full_name") != null ? String.valueOf(r.get("full_name")) : String.valueOf(r.get("username")));
                list.add(log);
            }
        }

        // Sort descending by rawDate
        list.sort((a, b) -> b.getRawDate().compareTo(a.getRawDate()));

        // Filter by search
        if (search != null && !search.trim().isEmpty()) {
            String query = search.toLowerCase().trim();
            list.removeIf(item ->
                    !item.getId().toLowerCase().contains(query) &&
                    !item.getLabel().toLowerCase().contains(query) &&
                    !item.getProductName().toLowerCase().contains(query) &&
                    !item.getUser().toLowerCase().contains(query) &&
                    !item.getNote().toLowerCase().contains(query)
            );
        }

        if (limit > 0 && list.size() > limit) {
            return list.subList(0, limit);
        }

        return list;
    }
}
