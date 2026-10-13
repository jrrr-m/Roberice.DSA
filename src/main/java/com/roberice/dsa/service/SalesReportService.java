package com.roberice.dsa.service;

import com.roberice.dsa.db.Database;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;

public class SalesReportService {

    public static class ReportSummary {
        public double totalRevenue = 0.0;
        public int totalTransactions = 0;
        public double averageOrderValue = 0.0;
        public double totalKgSold = 0.0;
        public int totalSacksSold = 0;

        public List<Map<String, Object>> productSales = new ArrayList<>();
        public List<Map<String, Object>> dailyTrend = new ArrayList<>();
        public List<Map<String, Object>> categoryBreakdown = new ArrayList<>();
        public List<Map<String, Object>> paymentMethodBreakdown = new ArrayList<>();
    }

    public static ReportSummary generateReport(String period, String customStart, String customEnd, Integer cashierUserId) {
        String dateCondition;
        List<Object> params = new ArrayList<>();

        if ("Today".equalsIgnoreCase(period)) {
            dateCondition = "DATE(t.transaction_date) = CURRENT_DATE ";
        } else if ("This Week".equalsIgnoreCase(period)) {
            dateCondition = "t.transaction_date >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY) ";
        } else if ("This Month".equalsIgnoreCase(period)) {
            dateCondition = "t.transaction_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) ";
        } else if ("This Year".equalsIgnoreCase(period)) {
            dateCondition = "YEAR(t.transaction_date) = YEAR(CURRENT_DATE) ";
        } else if ("Custom".equalsIgnoreCase(period) && customStart != null && customEnd != null) {
            dateCondition = "DATE(t.transaction_date) BETWEEN ? AND ? ";
            params.add(customStart);
            params.add(customEnd);
        } else {
            // Default 30 days
            dateCondition = "t.transaction_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) ";
        }

        String cashierCondition = "";
        if (cashierUserId != null && cashierUserId > 0) {
            cashierCondition = "AND t.user_id = ? ";
            params.add(cashierUserId);
        }

        ReportSummary summary = new ReportSummary();

        // 1. Overall stats
        String statsSql = "SELECT " +
                "COUNT(t.transaction_id) AS tx_count, " +
                "COALESCE(SUM(t.total_amount), 0.0) AS rev, " +
                "COALESCE(AVG(t.total_amount), 0.0) AS aov " +
                "FROM sales_transaction t " +
                "WHERE t.transaction_status = 'Completed' AND " + dateCondition + cashierCondition;

        Map<String, Object> statsRow = Database.queryForMap(statsSql, params.toArray());
        if (statsRow != null) {
            summary.totalTransactions = statsRow.get("tx_count") != null ? ((Number) statsRow.get("tx_count")).intValue() : 0;
            summary.totalRevenue = statsRow.get("rev") != null ? ((Number) statsRow.get("rev")).doubleValue() : 0.0;
            summary.averageOrderValue = statsRow.get("aov") != null ? ((Number) statsRow.get("aov")).doubleValue() : 0.0;
        }

        // 2. Product sales breakdown
        String prodSql = "SELECT " +
                "p.product_id, p.product_name, p.category, p.rice_type, " +
                "COALESCE(SUM(ti.subtotal), 0.0) AS product_revenue, " +
                "COALESCE(SUM(CASE WHEN ti.unit = 'sack' THEN ti.quantity ELSE 0 END), 0) AS sacks_sold, " +
                "COALESCE(SUM(CASE " +
                "  WHEN ti.unit = 'sack' THEN ti.quantity * 50 " +
                "  WHEN ti.unit = '25kg' THEN ti.quantity * 25 " +
                "  WHEN ti.unit = '20kg' THEN ti.quantity * 20 " +
                "  WHEN ti.unit = '10kg' THEN ti.quantity * 10 " +
                "  WHEN ti.unit = '5kg' THEN ti.quantity * 5 " +
                "  ELSE ti.quantity END), 0.0) AS total_kg_sold " +
                "FROM transaction_item ti " +
                "JOIN sales_transaction t ON ti.transaction_id = t.transaction_id " +
                "JOIN product p ON ti.product_id = p.product_id " +
                "WHERE t.transaction_status = 'Completed' AND " + dateCondition + cashierCondition +
                "GROUP BY p.product_id, p.product_name, p.category, p.rice_type " +
                "ORDER BY product_revenue DESC";

        summary.productSales = Database.queryForList(prodSql, params.toArray());

        double totalKg = 0.0;
        int totalSacks = 0;
        for (Map<String, Object> ps : summary.productSales) {
            totalKg += ps.get("total_kg_sold") != null ? ((Number) ps.get("total_kg_sold")).doubleValue() : 0.0;
            totalSacks += ps.get("sacks_sold") != null ? ((Number) ps.get("sacks_sold")).intValue() : 0;
        }
        summary.totalKgSold = totalKg;
        summary.totalSacksSold = totalSacks;

        // 3. Daily trend
        String dailySql = "SELECT " +
                "DATE(t.transaction_date) AS sale_date, " +
                "COUNT(t.transaction_id) AS tx_count, " +
                "COALESCE(SUM(t.total_amount), 0.0) AS daily_revenue " +
                "FROM sales_transaction t " +
                "WHERE t.transaction_status = 'Completed' AND " + dateCondition + cashierCondition +
                "GROUP BY DATE(t.transaction_date) " +
                "ORDER BY sale_date ASC";

        summary.dailyTrend = Database.queryForList(dailySql, params.toArray());

        // 4. Category breakdown
        String catSql = "SELECT " +
                "COALESCE(p.rice_type, p.category) AS variety, " +
                "COALESCE(SUM(ti.subtotal), 0.0) AS variety_revenue " +
                "FROM transaction_item ti " +
                "JOIN sales_transaction t ON ti.transaction_id = t.transaction_id " +
                "JOIN product p ON ti.product_id = p.product_id " +
                "WHERE t.transaction_status = 'Completed' AND " + dateCondition + cashierCondition +
                "GROUP BY COALESCE(p.rice_type, p.category) " +
                "ORDER BY variety_revenue DESC";

        summary.categoryBreakdown = Database.queryForList(catSql, params.toArray());

        // 5. Payment method breakdown
        String paySql = "SELECT " +
                "t.payment_method, " +
                "COUNT(t.transaction_id) AS count, " +
                "COALESCE(SUM(t.total_amount), 0.0) AS method_total " +
                "FROM sales_transaction t " +
                "WHERE t.transaction_status = 'Completed' AND " + dateCondition + cashierCondition +
                "GROUP BY t.payment_method " +
                "ORDER BY method_total DESC";

        summary.paymentMethodBreakdown = Database.queryForList(paySql, params.toArray());

        return summary;
    }

    public static File exportReportToCsv(ReportSummary summary, File targetFile) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("ROBERICE SALES REPORT");
            writer.println("Generated At," + LocalDate.now());
            writer.println("Total Revenue," + String.format("%.2f", summary.totalRevenue));
            writer.println("Total Transactions," + summary.totalTransactions);
            writer.println("Average Order Value," + String.format("%.2f", summary.averageOrderValue));
            writer.println("Total Sacks Sold," + summary.totalSacksSold);
            writer.println("Total Volume Sold (kg)," + String.format("%.2f", summary.totalKgSold));
            writer.println();

            writer.println("PRODUCT PERFORMANCE BREAKDOWN");
            writer.println("Product ID,Product Name,Category,Rice Type,Sacks Sold,Total KG Sold,Revenue (PHP)");
            for (Map<String, Object> r : summary.productSales) {
                writer.printf("%s,\"%s\",\"%s\",\"%s\",%s,%.2f,%.2f%n",
                        r.get("product_id"),
                        String.valueOf(r.get("product_name")).replace("\"", "\"\""),
                        r.get("category"),
                        r.get("rice_type"),
                        r.get("sacks_sold"),
                        ((Number) r.get("total_kg_sold")).doubleValue(),
                        ((Number) r.get("product_revenue")).doubleValue()
                );
            }
            writer.println();

            writer.println("DAILY REVENUE TREND");
            writer.println("Date,Transactions,Revenue (PHP)");
            for (Map<String, Object> d : summary.dailyTrend) {
                writer.printf("%s,%s,%.2f%n",
                        d.get("sale_date"),
                        d.get("tx_count"),
                        ((Number) d.get("daily_revenue")).doubleValue()
                );
            }
        }
        return targetFile;
    }
}
