package com.roberice.pos.service;

import com.roberice.pos.db.DatabaseManager;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Service for calculating Multi-Period Sales Reports and generating CSV statements.
 */
public class ReportService {

    public static class ReportSummary {
        public String periodTitle;
        public String periodLabel;
        public double totalRevenue = 0;
        public int transactionCount = 0;
        public int totalSacksSold = 0;
        public double totalKgSold = 0;
        public double averageOrderValue = 0;
        public List<VarietySalesRow> varietyRows = new ArrayList<>();
    }

    public static class VarietySalesRow {
        public int productId;
        public String productName;
        public String origin;
        public int sacksSold;
        public int p25Sold;
        public int p20Sold;
        public int p10Sold;
        public int p5Sold;
        public double looseKgSold;
        public double totalVolumeKg;
        public double totalRevenue;
    }

    public ReportSummary generateReport(String periodType, String customDate, int quarter, int year) throws SQLException {
        ReportSummary summary = new ReportSummary();
        String dateCondition = "";
        List<Object> params = new ArrayList<>();

        LocalDate today = LocalDate.now();

        switch (periodType.toLowerCase()) {
            case "daily": {
                LocalDate target = (customDate != null && !customDate.isEmpty()) ? LocalDate.parse(customDate) : today;
                summary.periodTitle = "OFFICIAL DAILY SALES REPORT";
                summary.periodLabel = "Daily: " + target.toString();
                dateCondition = "DATE(t.transaction_date) = ?";
                params.add(target.toString());
                break;
            }
            case "weekly": {
                LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                summary.periodTitle = "OFFICIAL WEEKLY SALES REPORT";
                summary.periodLabel = "Weekly: " + monday.toString() + " to " + sunday.toString();
                dateCondition = "DATE(t.transaction_date) BETWEEN ? AND ?";
                params.add(monday.toString());
                params.add(sunday.toString());
                break;
            }
            case "monthly": {
                String ym = (customDate != null && !customDate.isEmpty()) ? customDate : String.format("%04d-%02d", today.getYear(), today.getMonthValue());
                summary.periodTitle = "OFFICIAL MONTHLY SALES REPORT";
                summary.periodLabel = "Monthly: " + ym;
                if (DatabaseManager.isMySql()) {
                    dateCondition = "DATE_FORMAT(t.transaction_date, '%Y-%m') = ?";
                } else {
                    dateCondition = "strftime('%Y-%m', t.transaction_date) = ?";
                }
                params.add(ym);
                break;
            }
            case "quarterly": {
                int q = quarter > 0 ? quarter : 1;
                int y = year > 0 ? year : today.getYear();
                summary.periodTitle = "OFFICIAL QUARTERLY SALES REPORT";
                String startM = String.format("%02d-01", (q - 1) * 3 + 1);
                String endM = (q == 1) ? "03-31" : (q == 2) ? "06-30" : (q == 3) ? "09-30" : "12-31";
                String qStart = y + "-" + startM;
                String qEnd = y + "-" + endM;
                summary.periodLabel = "Quarterly: Q" + q + " " + y + " (" + qStart + " to " + qEnd + ")";
                dateCondition = "DATE(t.transaction_date) BETWEEN ? AND ?";
                params.add(qStart);
                params.add(qEnd);
                break;
            }
            case "yearly": {
                int y = year > 0 ? year : today.getYear();
                summary.periodTitle = "OFFICIAL YEARLY SALES REPORT";
                summary.periodLabel = "Yearly: " + y;
                if (DatabaseManager.isMySql()) {
                    dateCondition = "YEAR(t.transaction_date) = ?";
                    params.add(y);
                } else {
                    dateCondition = "strftime('%Y', t.transaction_date) = ?";
                    params.add(String.valueOf(y));
                }
                break;
            }
            default: // All recorded time
                summary.periodTitle = "OFFICIAL SALES SUMMARY STATEMENT";
                summary.periodLabel = "All Recorded History";
                dateCondition = "1=1";
                break;
        }

        // 1. Calculate KPI Metrics
        String kpiSql = "SELECT COUNT(*) AS total_orders, COALESCE(SUM(total_amount), 0) AS gross_sales FROM sales_transaction t WHERE t.transaction_status = 'Completed' AND " + dateCondition;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(kpiSql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    summary.transactionCount = rs.getInt("total_orders");
                    summary.totalRevenue = rs.getDouble("gross_sales");
                    if (summary.transactionCount > 0) {
                        summary.averageOrderValue = summary.totalRevenue / summary.transactionCount;
                    }
                }
            }
        }

        // 2. Calculate Variety Sales Breakdown
        String varietySql = """
            SELECT 
                p.product_id, 
                p.product_name, 
                p.origin,
                COALESCE(SUM(CASE WHEN LOWER(ti.unit) IN ('sack', '50kg') THEN ti.quantity ELSE 0 END), 0) AS sacks_sold,
                COALESCE(SUM(CASE WHEN LOWER(ti.unit) IN ('25kg', 'half', 'half_sack') THEN ti.quantity ELSE 0 END), 0) AS p25_sold,
                COALESCE(SUM(CASE WHEN LOWER(ti.unit) = '20kg' THEN ti.quantity ELSE 0 END), 0) AS p20_sold,
                COALESCE(SUM(CASE WHEN LOWER(ti.unit) = '10kg' THEN ti.quantity ELSE 0 END), 0) AS p10_sold,
                COALESCE(SUM(CASE WHEN LOWER(ti.unit) = '5kg' THEN ti.quantity ELSE 0 END), 0) AS p5_sold,
                COALESCE(SUM(CASE WHEN LOWER(ti.unit) IN ('tinatakal', 'kg') THEN ti.quantity ELSE 0 END), 0) AS loose_kg_sold,
                COALESCE(SUM(ti.subtotal), 0) AS variety_revenue
            FROM product p
            LEFT JOIN transaction_item ti ON p.product_id = ti.product_id
            LEFT JOIN sales_transaction t ON ti.transaction_id = t.transaction_id AND t.transaction_status = 'Completed' AND ("""
                + dateCondition + """
            )
            GROUP BY p.product_id, p.product_name, p.origin
            ORDER BY variety_revenue DESC, p.product_id ASC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(varietySql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VarietySalesRow row = new VarietySalesRow();
                    row.productId = rs.getInt("product_id");
                    row.productName = rs.getString("product_name");
                    row.origin = rs.getString("origin");
                    row.sacksSold = rs.getInt("sacks_sold");
                    row.p25Sold = rs.getInt("p25_sold");
                    row.p20Sold = rs.getInt("p20_sold");
                    row.p10Sold = rs.getInt("p10_sold");
                    row.p5Sold = rs.getInt("p5_sold");
                    row.looseKgSold = rs.getDouble("loose_kg_sold");
                    row.totalRevenue = rs.getDouble("variety_revenue");

                    row.totalVolumeKg = (row.sacksSold * 50.0) + (row.p25Sold * 25.0) + (row.p20Sold * 20.0)
                            + (row.p10Sold * 10.0) + (row.p5Sold * 5.0) + row.looseKgSold;

                    summary.varietyRows.add(row);
                    summary.totalSacksSold += row.sacksSold;
                    summary.totalKgSold += row.totalVolumeKg;
                }
            }
        }

        return summary;
    }

    /**
     * Exports the sales report into an official CSV file.
     */
    public File exportToCsv(ReportSummary report) throws Exception {
        File dir = new File("reports");
        if (!dir.exists()) dir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        File file = new File(dir, "sales_report_" + timeStamp + ".csv");

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("ROBERICE - POINT OF SALE & INVENTORY MANAGEMENT SYSTEM");
            pw.println("73 Farmers St. Corner Ampalaya, Marikina 1800");
            pw.println(report.periodTitle);
            pw.println("Period," + report.periodLabel);
            pw.println("Generated Date," + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            pw.println();
            pw.println("KEY PERFORMANCE INDICATORS");
            pw.println("Total Gross Sales,PHP " + String.format("%.2f", report.totalRevenue));
            pw.println("Completed Orders," + report.transactionCount);
            pw.println("Total Whole Sacks Sold," + report.totalSacksSold);
            pw.println("Total Volume Sold (Kg)," + String.format("%.2f", report.totalKgSold));
            pw.println("Average Order Basket,PHP " + String.format("%.2f", report.averageOrderValue));
            pw.println();
            pw.println("VARIETY SALES BREAKDOWN");
            pw.println("Rice Variety,Origin,50kg Sacks,25kg Bags,20kg Bags,10kg Bags,5kg Bags,Loose (Kg),Total Volume (Kg),Gross Revenue (PHP)");

            for (VarietySalesRow r : report.varietyRows) {
                pw.printf("\"%s\",\"%s\",%d,%d,%d,%d,%d,%.2f,%.2f,%.2f\n",
                        r.productName, r.origin, r.sacksSold, r.p25Sold, r.p20Sold, r.p10Sold, r.p5Sold,
                        r.looseKgSold, r.totalVolumeKg, r.totalRevenue);
            }
        }

        return file;
    }
}
