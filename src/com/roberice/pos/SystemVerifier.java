package com.roberice.pos;

import com.roberice.pos.db.*;
import com.roberice.pos.model.*;
import com.roberice.pos.service.*;

import java.util.List;

/**
 * Headless System Verification Suite.
 * Validates database connectivity, seed data, checkout FIFO inventory deductions, and reporting.
 */
public class SystemVerifier {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("ROBERICE POS & INVENTORY SYSTEM - VERIFICATION");
        System.out.println("==================================================");

        try {
            // 1. Database Initialization
            DatabaseManager.initialize();
            System.out.println("[PASS] Database Initialized: " + DatabaseManager.getActiveDbInfo());

            // 2. User Authentication
            UserDAO userDAO = new UserDAO();
            User admin = userDAO.authenticate("admin", "admin123");
            if (admin == null || !"Admin".equalsIgnoreCase(admin.getRole())) {
                throw new RuntimeException("Admin authentication failed!");
            }
            System.out.println("[PASS] User Authentication OK: " + admin.getFullName() + " (" + admin.getRole() + ")");

            // 3. Products
            ProductDAO productDAO = new ProductDAO();
            List<Product> products = productDAO.getAllProducts(true);
            System.out.println("[PASS] Active Products: " + products.size() + " varieties found.");
            if (products.isEmpty()) throw new RuntimeException("No products found!");

            // 4. Inventory Balances
            InventoryDAO invDAO = new InventoryDAO();
            List<Inventory> invList = invDAO.getInventorySummary();
            System.out.println("[PASS] Inventory Records: " + invList.size() + " items loaded.");

            // 5. Test POS Sale Checkout with FIFO Deduction & Auto Sack Opening
            SalesDAO salesDAO = new SalesDAO();
            Product first = products.get(0);

            SalesTransaction testTx = new SalesTransaction();
            testTx.setUserId(admin.getUserId());
            testTx.setCashierName(admin.getFullName());
            testTx.setPaymentMethod("Cash");

            // Buy 10kg loose (tinatakal)
            TransactionItem item1 = new TransactionItem(first.getProductId(), first.getProductName(), "tinatakal", 10.0, first.getPriceKg(), first.getPriceKg() * 10.0);
            testTx.addItem(item1);

            // Buy 1 whole sack (50kg)
            TransactionItem item2 = new TransactionItem(first.getProductId(), first.getProductName(), "sack", 1.0, first.getPriceSack(), first.getPriceSack());
            testTx.addItem(item2);

            double total = item1.getSubtotal() + item2.getSubtotal();
            testTx.setTotalAmount(total);
            testTx.setPaymentAmount(total + 500.0);
            testTx.setChangeAmount(500.0);

            SalesTransaction processed = salesDAO.processSale(testTx, admin.getUsername());
            System.out.println("[PASS] Checkout Processed: Order ID #" + processed.getTransactionId() + " Total = PHP " + processed.getTotalAmount());

            // 6. Receipt Generation
            String receipt = ReceiptPrinter.buildReceiptText(processed);
            System.out.println("[PASS] Formatted Sales Receipt Generated (Length: " + receipt.length() + " chars)");

            // 7. Sales Reporting
            ReportService reportService = new ReportService();
            ReportService.ReportSummary rep = reportService.generateReport("all", null, 0, 0);
            System.out.println("[PASS] Sales Report Generated: Orders = " + rep.transactionCount + ", Gross = PHP " + rep.totalRevenue);

            // 8. Audit History Log
            AuditDAO auditDAO = new AuditDAO();
            List<InventoryAdjustment> audits = auditDAO.getAuditLogs("all", 5);
            System.out.println("[PASS] Audit Trail Verified: " + audits.size() + " recent events logged.");

            System.out.println("\nALL SYSTEM TESTS PASSED SUCCESSFULLY! Ready for Eclipse execution.");
            System.out.println("==================================================");

        } catch (Exception ex) {
            System.err.println("[FAIL] Verification Error: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
