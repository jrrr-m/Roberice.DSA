package com.roberice.pos.db;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database connections for Roberice POS.
 * Supports MySQL (XAMPP localhost:3306 roberice_db) with automated fallback to SQLite (roberice.db).
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    private static String mysqlHost = "localhost";
    private static int mysqlPort = 3306;
    private static String mysqlDb = "roberice_db";
    private static String mysqlUser = "root";
    private static String mysqlPass = "";

    private static boolean isMySqlActive = false;
    private static String activeDbInfo = "";
    private static Connection singleConnection = null;

    /**
     * Initializes the database connection and ensures tables and seed data exist.
     */
    public static synchronized void initialize() {
        // Try MySQL first
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String mysqlUrlNoDb = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/?serverTimezone=UTC&allowMultiQueries=true";
            
            try (Connection testConn = DriverManager.getConnection(mysqlUrlNoDb, mysqlUser, mysqlPass);
                 Statement stmt = testConn.createStatement()) {
                // Ensure roberice_db database exists
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + mysqlDb + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
            }

            String mysqlUrl = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDb + "?serverTimezone=UTC&allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8";
            Connection conn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
            conn.close();

            isMySqlActive = true;
            activeDbInfo = "MySQL (localhost:3306 / " + mysqlDb + ")";
            LOGGER.info("[Roberice] Connected to MySQL successfully.");
            
            // If MySQL roberice_db.sql file exists and product table is empty, import the entire SQL dump
            importSqlDumpIfAvailable();

            ensureTablesAndSeed(true);
            return;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[Roberice] MySQL not available (" + e.getMessage() + "). Falling back to SQLite local database.");
        }

        // Fallback to SQLite
        try {
            Class.forName("org.sqlite.JDBC");
            File dbFile = new File("roberice.db");
            isMySqlActive = false;
            activeDbInfo = "SQLite (" + dbFile.getAbsolutePath() + ")";
            LOGGER.info("[Roberice] Connected to SQLite local database: " + dbFile.getAbsolutePath());
            ensureTablesAndSeed(false);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "[Roberice] Failed to initialize SQLite database.", ex);
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (isMySqlActive) {
            String url = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDb + "?serverTimezone=UTC&allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8";
            return DriverManager.getConnection(url, mysqlUser, mysqlPass);
        } else {
            String url = "jdbc:sqlite:roberice.db";
            Connection conn = DriverManager.getConnection(url);
            try (Statement s = conn.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        }
    }

    public static boolean isMySql() {
        return isMySqlActive;
    }

    public static String getActiveDbInfo() {
        return activeDbInfo;
    }

    public static java.util.Date parseDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.sql.Timestamp) return new java.util.Date(((java.sql.Timestamp) obj).getTime());
        if (obj instanceof java.sql.Date) return new java.util.Date(((java.sql.Date) obj).getTime());
        if (obj instanceof java.util.Date) return (java.util.Date) obj;
        String s = obj.toString().trim();
        if (s.isEmpty()) return null;
        try {
            if (s.length() == 10) {
                return java.sql.Date.valueOf(s);
            } else if (s.contains("T")) {
                s = s.replace("T", " ");
            }
            if (s.length() >= 19) {
                return java.sql.Timestamp.valueOf(s.substring(0, 19));
            }
        } catch (Exception ignored) {}
        return new java.util.Date();
    }

    private static void importSqlDumpIfAvailable() {
        File sqlFile = new File("roberice_db.sql");
        if (!sqlFile.exists()) {
            return;
        }
        try (Connection conn = getConnection(); Statement s = conn.createStatement()) {
            // Check if product table already exists and has records
            try (ResultSet rs = s.executeQuery("SHOW TABLES LIKE 'product'")) {
                if (rs.next()) {
                    try (ResultSet countRs = s.executeQuery("SELECT COUNT(*) FROM `product`")) {
                        if (countRs.next() && countRs.getInt(1) > 0) {
                            return; // Already initialized
                        }
                    }
                }
            }
            
            LOGGER.info("[Roberice] Initializing MySQL from roberice_db.sql...");
            String sqlContent = java.nio.file.Files.readString(sqlFile.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            s.execute(sqlContent);
            LOGGER.info("[Roberice] Successfully imported roberice_db.sql into MySQL.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[Roberice] Could not run full roberice_db.sql import: " + e.getMessage());
        }
    }

    private static void ensureTablesAndSeed(boolean isMysql) {
        try (Connection conn = getConnection(); Statement s = conn.createStatement()) {
            if (isMysql) {
                // MySQL Tables
                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `product` (
                      `product_id` int(11) NOT NULL AUTO_INCREMENT,
                      `product_name` varchar(100) NOT NULL,
                      `category` varchar(100) DEFAULT NULL,
                      `origin` varchar(20) NOT NULL DEFAULT 'Local',
                      `rice_type` varchar(50) NOT NULL DEFAULT 'White Rice',
                      `description` text DEFAULT NULL,
                      `price_sack` decimal(10,2) NOT NULL,
                      `price_25kg` decimal(10,2) DEFAULT NULL,
                      `price_kg` decimal(10,2) NOT NULL,
                      `price_5kg` decimal(10,2) DEFAULT NULL,
                      `price_10kg` decimal(10,2) DEFAULT NULL,
                      `price_20kg` decimal(10,2) DEFAULT NULL,
                      `kg_per_sack` decimal(10,2) DEFAULT 50.00,
                      `product_status` varchar(20) DEFAULT 'Active',
                      PRIMARY KEY (`product_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `inventory` (
                      `inventory_id` int(11) NOT NULL AUTO_INCREMENT,
                      `product_id` int(11) NOT NULL,
                      `stock_sacks` int(11) DEFAULT 0,
                      `packaged_25kg` int(11) NOT NULL DEFAULT 0,
                      `stock_kg` decimal(10,2) DEFAULT 0.00,
                      `packaged_5kg` int(11) NOT NULL DEFAULT 0,
                      `packaged_10kg` int(11) NOT NULL DEFAULT 0,
                      `packaged_20kg` int(11) NOT NULL DEFAULT 0,
                      `received_date` date NOT NULL DEFAULT '2026-09-01',
                      `batch_number` varchar(50) NOT NULL DEFAULT 'BATCH-001',
                      `reorder_level_kg` decimal(10,2) DEFAULT 200.00,
                      `last_updated` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      PRIMARY KEY (`inventory_id`),
                      UNIQUE KEY `product_id` (`product_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `inventory_batch` (
                      `batch_id` int(11) NOT NULL AUTO_INCREMENT,
                      `product_id` int(11) NOT NULL,
                      `batch_code` varchar(50) NOT NULL,
                      `received_date` date NOT NULL,
                      `stock_sacks` int(11) NOT NULL DEFAULT 0,
                      `packaged_25kg` int(11) NOT NULL DEFAULT 0,
                      `packaged_20kg` int(11) NOT NULL DEFAULT 0,
                      `packaged_10kg` int(11) NOT NULL DEFAULT 0,
                      `packaged_5kg` int(11) NOT NULL DEFAULT 0,
                      `stock_kg` decimal(10,2) NOT NULL DEFAULT 0.00,
                      `status` varchar(20) NOT NULL DEFAULT 'Active',
                      `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      PRIMARY KEY (`batch_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `inventory_adjustment` (
                      `adjustment_id` int(11) NOT NULL AUTO_INCREMENT,
                      `product_id` int(11) NOT NULL,
                      `batch_code` varchar(50) DEFAULT NULL,
                      `adjustment_date` datetime DEFAULT CURRENT_TIMESTAMP,
                      `adjustment_type` varchar(50) NOT NULL,
                      `sacks` int(11) DEFAULT 0,
                      `kg` decimal(10,2) DEFAULT 0.00,
                      `note` text DEFAULT NULL,
                      `performed_by` varchar(100) DEFAULT NULL,
                      PRIMARY KEY (`adjustment_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `users` (
                      `user_id` int(11) NOT NULL AUTO_INCREMENT,
                      `username` varchar(50) NOT NULL UNIQUE,
                      `password` varchar(255) NOT NULL,
                      `full_name` varchar(100) NOT NULL,
                      `email` varchar(150) DEFAULT NULL,
                      `role` varchar(50) NOT NULL DEFAULT 'Cashier',
                      `account_status` varchar(20) DEFAULT 'Active',
                      `permissions` text DEFAULT NULL,
                      PRIMARY KEY (`user_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `account_requests` (
                      `request_id` int(11) NOT NULL AUTO_INCREMENT,
                      `full_name` varchar(100) NOT NULL,
                      `username` varchar(50) NOT NULL,
                      `email` varchar(150) NOT NULL,
                      `password` varchar(255) NOT NULL,
                      `role` varchar(50) NOT NULL DEFAULT 'Cashier',
                      `status` varchar(20) NOT NULL DEFAULT 'Pending',
                      `rejection_reason` varchar(255) DEFAULT NULL,
                      `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      `reviewed_at` datetime DEFAULT NULL,
                      `reviewed_by` int(11) DEFAULT NULL,
                      PRIMARY KEY (`request_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `sales_transaction` (
                      `transaction_id` int(11) NOT NULL AUTO_INCREMENT,
                      `user_id` int(11) NOT NULL,
                      `transaction_date` datetime DEFAULT CURRENT_TIMESTAMP,
                      `total_amount` decimal(10,2) NOT NULL,
                      `payment_amount` decimal(10,2) NOT NULL,
                      `change_amount` decimal(10,2) NOT NULL,
                      `payment_method` varchar(30) NOT NULL DEFAULT 'Cash',
                      `transaction_status` varchar(20) DEFAULT 'Completed',
                      PRIMARY KEY (`transaction_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `transaction_item` (
                      `transaction_item_id` int(11) NOT NULL AUTO_INCREMENT,
                      `transaction_id` int(11) NOT NULL,
                      `product_id` int(11) NOT NULL,
                      `unit` varchar(20) NOT NULL,
                      `quantity` decimal(10,2) NOT NULL,
                      `unit_price` decimal(10,2) NOT NULL,
                      `subtotal` decimal(10,2) NOT NULL,
                      PRIMARY KEY (`transaction_item_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `custom_roles` (
                      `role_id` int(11) NOT NULL AUTO_INCREMENT,
                      `role_name` varchar(50) NOT NULL,
                      `default_permissions` text DEFAULT NULL,
                      `is_system` tinyint(1) NOT NULL DEFAULT 0,
                      `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                      PRIMARY KEY (`role_id`),
                      UNIQUE KEY `role_name` (`role_name`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `file_records` (
                      `file_id` int(11) NOT NULL AUTO_INCREMENT,
                      `file_name` varchar(255) NOT NULL,
                      `original_name` varchar(255) NOT NULL,
                      `file_path` varchar(255) NOT NULL,
                      `file_size` bigint(20) NOT NULL,
                      `file_type` varchar(100) NOT NULL,
                      `category` varchar(50) NOT NULL DEFAULT 'Document',
                      `description` text DEFAULT NULL,
                      `uploaded_by` varchar(100) DEFAULT 'Admin',
                      `uploaded_at` datetime DEFAULT CURRENT_TIMESTAMP,
                      PRIMARY KEY (`file_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

            } else {
                // SQLite Tables
                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS product (
                      product_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      product_name TEXT NOT NULL,
                      category TEXT,
                      origin TEXT NOT NULL DEFAULT 'Local',
                      rice_type TEXT NOT NULL DEFAULT 'White Rice',
                      description TEXT,
                      price_sack REAL NOT NULL,
                      price_25kg REAL,
                      price_kg REAL NOT NULL,
                      price_5kg REAL,
                      price_10kg REAL,
                      price_20kg REAL,
                      kg_per_sack REAL DEFAULT 50.00,
                      product_status TEXT DEFAULT 'Active'
                    );
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS inventory (
                      inventory_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      product_id INTEGER NOT NULL UNIQUE,
                      stock_sacks INTEGER DEFAULT 0,
                      packaged_25kg INTEGER DEFAULT 0,
                      stock_kg REAL DEFAULT 0.00,
                      packaged_5kg INTEGER DEFAULT 0,
                      packaged_10kg INTEGER DEFAULT 0,
                      packaged_20kg INTEGER DEFAULT 0,
                      received_date TEXT DEFAULT '2026-09-01',
                      batch_number TEXT DEFAULT 'BATCH-001',
                      reorder_level_kg REAL DEFAULT 200.00,
                      last_updated TEXT DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS inventory_batch (
                      batch_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      product_id INTEGER NOT NULL,
                      batch_code TEXT NOT NULL,
                      received_date TEXT NOT NULL,
                      stock_sacks INTEGER DEFAULT 0,
                      packaged_25kg INTEGER DEFAULT 0,
                      packaged_20kg INTEGER DEFAULT 0,
                      packaged_10kg INTEGER DEFAULT 0,
                      packaged_5kg INTEGER DEFAULT 0,
                      stock_kg REAL DEFAULT 0.00,
                      status TEXT DEFAULT 'Active',
                      created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                      updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS inventory_adjustment (
                      adjustment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      product_id INTEGER NOT NULL,
                      batch_code TEXT,
                      adjustment_date TEXT DEFAULT CURRENT_TIMESTAMP,
                      adjustment_type TEXT NOT NULL,
                      sacks INTEGER DEFAULT 0,
                      kg REAL DEFAULT 0.00,
                      note TEXT,
                      performed_by TEXT
                    );
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                      user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      username TEXT NOT NULL UNIQUE,
                      password TEXT NOT NULL,
                      full_name TEXT NOT NULL,
                      email TEXT,
                      role TEXT DEFAULT 'Cashier',
                      account_status TEXT DEFAULT 'Active',
                      permissions TEXT
                    );
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS account_requests (
                      request_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      full_name TEXT NOT NULL,
                      username TEXT NOT NULL,
                      email TEXT NOT NULL,
                      password TEXT NOT NULL,
                      role TEXT DEFAULT 'Cashier',
                      status TEXT DEFAULT 'Pending',
                      rejection_reason TEXT,
                      created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                      reviewed_at TEXT,
                      reviewed_by INTEGER
                    );
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS sales_transaction (
                      transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      user_id INTEGER NOT NULL,
                      transaction_date TEXT DEFAULT CURRENT_TIMESTAMP,
                      total_amount REAL NOT NULL,
                      payment_amount REAL NOT NULL,
                      change_amount REAL NOT NULL,
                      payment_method TEXT DEFAULT 'Cash',
                      transaction_status TEXT DEFAULT 'Completed'
                    );
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS transaction_item (
                      transaction_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      transaction_id INTEGER NOT NULL,
                      product_id INTEGER NOT NULL,
                      unit TEXT NOT NULL,
                      quantity REAL NOT NULL,
                      unit_price REAL NOT NULL,
                      subtotal REAL NOT NULL
                    );
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS custom_roles (
                      role_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      role_name TEXT NOT NULL UNIQUE,
                      default_permissions TEXT,
                      is_system INTEGER DEFAULT 0,
                      created_at TEXT DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS file_records (
                      file_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      file_name TEXT NOT NULL,
                      original_name TEXT NOT NULL,
                      file_path TEXT NOT NULL,
                      file_size INTEGER NOT NULL,
                      file_type TEXT NOT NULL,
                      category TEXT DEFAULT 'Document',
                      description TEXT,
                      uploaded_by TEXT DEFAULT 'Admin',
                      uploaded_at TEXT DEFAULT CURRENT_TIMESTAMP
                    );
                """);
            }

            // Check if custom_roles is populated; if not, seed default roles
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM custom_roles")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedCustomRoles(conn);
                }
            }

            // Check if users table is populated; if not, seed default users
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedDefaultUsers(conn);
                }
            }

            // Check if products table is populated; if not, seed 12 authentic varieties & inventory
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM product")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedProductsAndInventory(conn);
                }
            }

            // Check if file_records is populated; if not, seed system files
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM file_records")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedDefaultFiles(conn);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Roberice] Error ensuring database tables.", e);
        }
    }

    private static void seedCustomRoles(Connection conn) throws SQLException {
        String[][] roles = {
            {"Admin", "[\"dashboard\",\"pos\",\"view-inventory\",\"inventory\",\"products\",\"transactions\",\"sales-report\",\"file-manager\",\"audit\",\"users\"]", "1"},
            {"Cashier", "[\"pos\",\"view-inventory\",\"transactions\",\"sales-report\"]", "1"},
            {"Inventory Clerk", "[\"view-inventory\",\"inventory\",\"products\"]", "1"},
            {"Store Manager", "[\"dashboard\",\"pos\",\"view-inventory\",\"products\",\"transactions\",\"sales-report\",\"file-manager\",\"audit\"]", "1"},
            {"Auditor", "[\"dashboard\",\"transactions\",\"sales-report\",\"audit\"]", "0"}
        };
        String sql = "INSERT INTO custom_roles (role_name, default_permissions, is_system) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] r : roles) {
                ps.setString(1, r[0]);
                ps.setString(2, r[1]);
                ps.setInt(3, Integer.parseInt(r[2]));
                ps.executeUpdate();
            }
        }
    }

    private static void seedDefaultFiles(Connection conn) throws SQLException {
        Object[][] files = {
            {"roberice_db_backup_20251231.sql", "roberice_db_backup_20251231.sql", "uploads/roberice_db_backup_20251231.sql", 184520L, "application/sql", "Database Backup", "Year-End FY2025 comprehensive database archive snapshot.", "Admin User", "2025-12-31 23:55:00"},
            {"annual_sales_audit_fy2025.pdf", "annual_sales_audit_fy2025.pdf", "uploads/annual_sales_audit_fy2025.pdf", 342110L, "application/pdf", "Financial Report", "Official 2025 Annual Financial & POS Reconciliation Statement.", "Admin User", "2026-01-05 10:15:00"},
            {"supplier_contract_isabela_mills.pdf", "supplier_contract_isabela_mills.pdf", "uploads/supplier_contract_isabela_mills.pdf", 125430L, "application/pdf", "Supplier Document", "Grain procurement agreement and seasonal pricing terms for Isabela Rice Mills.", "Admin User", "2026-03-01 14:30:00"},
            {"roberice_db_backup_20260630.sql", "roberice_db_backup_20260630.sql", "uploads/roberice_db_backup_20260630.sql", 312500L, "application/sql", "Database Backup", "Mid-Year FY2026 complete database schema and transaction state backup.", "Admin User", "2026-06-30 23:30:00"},
            {"roberice_db_backup_20261012_163344.sql", "roberice_db_backup_20261012_163344.sql", "uploads/roberice_db_backup_20261012_163344.sql", 452900L, "application/sql", "Database Backup", "Automated SQL database snapshot prior to quarterly stock reconciliation.", "Admin User", "2026-10-12 16:33:44"}
        };
        String sql = "INSERT INTO file_records (file_name, original_name, file_path, file_size, file_type, category, description, uploaded_by, uploaded_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] f : files) {
                ps.setString(1, (String) f[0]);
                ps.setString(2, (String) f[1]);
                ps.setString(3, (String) f[2]);
                ps.setLong(4, (Long) f[3]);
                ps.setString(5, (String) f[4]);
                ps.setString(6, (String) f[5]);
                ps.setString(7, (String) f[6]);
                ps.setString(8, (String) f[7]);
                ps.setString(9, (String) f[8]);
                ps.executeUpdate();
            }
        }
    }

    private static void seedDefaultUsers(Connection conn) throws SQLException {
        String adminPerms = "[\"dashboard\",\"pos\",\"view-inventory\",\"inventory\",\"products\",\"transactions\",\"sales-report\",\"file-manager\",\"audit\",\"users\"]";
        String cashierPerms = "[\"pos\",\"view-inventory\",\"transactions\",\"sales-report\"]";
        String clerkPerms = "[\"view-inventory\",\"inventory\",\"products\",\"transactions\"]";
        String auditorPerms = "[\"dashboard\",\"transactions\",\"sales-report\",\"audit\"]";

        String insertSql = "INSERT INTO users (username, password, full_name, email, role, account_status, permissions) VALUES (?, ?, ?, ?, ?, 'Active', ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            // Admin
            ps.setString(1, "admin");
            ps.setString(2, "admin123");
            ps.setString(3, "Admin User");
            ps.setString(4, "admin@roberice.com");
            ps.setString(5, "Admin");
            ps.setString(6, adminPerms);
            ps.executeUpdate();

            // Cashier 1
            ps.setString(1, "cashier1");
            ps.setString(2, "cashier123");
            ps.setString(3, "Maria Santos");
            ps.setString(4, "maria.santos@roberice.com");
            ps.setString(5, "Cashier");
            ps.setString(6, cashierPerms);
            ps.executeUpdate();

            // Cashier 2
            ps.setString(1, "cashier2");
            ps.setString(2, "cashier123");
            ps.setString(3, "Juan Dela Cruz");
            ps.setString(4, "juan.delacruz@roberice.com");
            ps.setString(5, "Cashier");
            ps.setString(6, cashierPerms);
            ps.executeUpdate();

            // Inventory Clerk
            ps.setString(1, "juan_inv");
            ps.setString(2, "inv123");
            ps.setString(3, "Juan");
            ps.setString(4, "juan.inv@roberice.com");
            ps.setString(5, "Inventory Clerk");
            ps.setString(6, clerkPerms);
            ps.executeUpdate();

            // Cashier (Maria Santos) - username 'cashier' as well
            ps.setString(1, "cashier");
            ps.setString(2, "cashier123");
            ps.setString(3, "Maria Santos");
            ps.setString(4, "maria.santos@roberice.com");
            ps.setString(5, "Cashier");
            ps.setString(6, cashierPerms);
            ps.executeUpdate();

            // Auditor (John Rex)
            ps.setString(1, "johnAudit");
            ps.setString(2, "audit123");
            ps.setString(3, "John Rex");
            ps.setString(4, "johnrexmartinez96@gmail.com");
            ps.setString(5, "Auditor");
            ps.setString(6, auditorPerms);
            ps.executeUpdate();

            // Cashier 3
            ps.setString(1, "cashier3");
            ps.setString(2, "cashier123");
            ps.setString(3, "Cashier 3");
            ps.setString(4, "cashier3@gmail.com");
            ps.setString(5, "Cashier");
            ps.setString(6, cashierPerms);
            ps.executeUpdate();
        }
    }

    private static void seedProductsAndInventory(Connection conn) throws SQLException {
        Object[][] products = {
            {1, "Sinandomeng Special", "Premium White", "Local", "Sinandomeng", "All-time favorite Filipino staple with long white grains, soft texture when cooked, and great volume.", 2150.0, 1075.0, 960.0, 485.0, 245.0, 48.0, 50.0, 45, 16, 8, 14, 20, 64.50, "BATCH-SIN-2610", 250.0},
            {2, "Dinorado Fancy Special", "Aromatic", "Local", "Dinorado", "Naturally aromatic local pinkish rice known for its sweet scent and sticky, soft grain texture.", 2450.0, 1225.0, 1080.0, 545.0, 275.0, 55.0, 50.0, 38, 12, 7, 15, 16, 48.00, "BATCH-DIN-2610", 200.0},
            {3, "Jasmine Fragrant Premium", "Fragrant Import", "Imported", "Jasmine", "Top-grade imported whole grain fragrant rice with silky smooth texture and fragrant floral aroma.", 2800.0, 1400.0, 1220.0, 615.0, 310.0, 62.0, 50.0, 34, 10, 9, 22, 24, 42.50, "BATCH-JAS-2610", 200.0},
            {4, "Angelika White Rice", "Regular Milled", "Local", "Angelika", "Affordable, high-yield white rice favored by carinderias and local food businesses for its firm texture.", 1950.0, 975.0, 860.0, 435.0, 220.0, 44.0, 50.0, 72, 14, 10, 18, 19, 58.00, "BATCH-ANG-2610", 250.0},
            {5, "Malagkit Glutinous Rice", "Specialty Rice", "Local", "Glutinous / Malagkit", "Pure sticky glutinous rice essential for traditional kakanin, champorado, biko, and holiday dishes.", 3100.0, 1550.0, 1370.0, 690.0, 350.0, 70.0, 50.0, 18, 8, 5, 11, 14, 46.00, "BATCH-MAL-2610", 150.0},
            {6, "IR-64 Well-Milled Rice", "Standard Local", "Local", "Well-Milled (IR-64)", "Standard commercial well-milled rice offering excellent value, uniform grain length, and everyday reliability.", 1850.0, 925.0, 820.0, 415.0, 210.0, 42.0, 50.0, 48, 18, 8, 16, 22, 52.50, "BATCH-IR64-2610", 300.0},
            {7, "Milagrosa Super Fragrant", "Aromatic", "Local", "Milagrosa", "Heritage Philippine aromatic rice variety with slender grains, pleasant fragrance, and tender mouthfeel.", 2600.0, 1300.0, 1140.0, 575.0, 290.0, 58.0, 50.0, 14, 6, 5, 8, 10, 44.00, "BATCH-MIL-2610", 180.0},
            {8, "Coco Pandan Premium Aromatic", "Fragrant Import", "Imported", "Coco Pandan", "Imported aromatic rice infused with sweet pandan aroma, soft chewing texture, and pure white grains.", 2700.0, 1350.0, 1180.0, 595.0, 300.0, 60.0, 50.0, 52, 10, 6, 12, 15, 68.00, "BATCH-CPAN-2610", 180.0},
            {9, "Brown Rice Organic Whole Grain", "Healthy Specialty", "Local", "Brown Rice", "Unpolished whole grain brown rice rich in dietary fiber, nutrients, and low glycemic index for health conscious buyers.", 3300.0, 1650.0, 1450.0, 730.0, 370.0, 75.0, 50.0, 24, 9, 4, 10, 12, 38.50, "BATCH-BRN-2610", 100.0},
            {10, "Japanese Calrose Rice", "Specialty Rice", "Imported", "Calrose", "Authentic medium grain sushi-grade rice with balanced moisture retention and tender, clinging grains.", 3400.0, 1700.0, 1500.0, 755.0, 380.0, 78.0, 50.0, 16, 6, 5, 8, 9, 36.00, "BATCH-CAL-2610", 100.0},
            {11, "Maharlika Premium Long Grain", "Premium White", "Local", "Maharlika", "Premium milled long grain local rice that cooks into fluffy, separate, non-sticky fragrant grains.", 2250.0, 1125.0, 990.0, 500.0, 255.0, 51.0, 50.0, 36, 12, 6, 14, 20, 72.00, "BATCH-MAH-2610", 200.0},
            {12, "Super Angelica Supreme", "Premium White", "Local", "Angelica", "Extra well-milled supreme Angelica rice featuring clean white kernels, soft fluffiness, and versatile cooking quality.", 2050.0, 1025.0, 900.0, 455.0, 230.0, 46.0, 50.0, 26, 5, 6, 11, 16, 55.00, "BATCH-SANG-2610", 220.0}
        };

        String pSql = "INSERT INTO product (product_id, product_name, category, origin, rice_type, description, price_sack, price_25kg, price_20kg, price_10kg, price_5kg, price_kg, kg_per_sack, product_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active')";
        String iSql = "INSERT INTO inventory (product_id, stock_sacks, packaged_25kg, packaged_20kg, packaged_10kg, packaged_5kg, stock_kg, batch_number, received_date, reorder_level_kg) VALUES (?, ?, ?, ?, ?, ?, ?, ?, '2026-10-04', ?)";
        String bSql = "INSERT INTO inventory_batch (product_id, batch_code, received_date, stock_sacks, packaged_25kg, packaged_20kg, packaged_10kg, packaged_5kg, stock_kg, status) VALUES (?, ?, '2026-10-04', ?, ?, ?, ?, ?, ?, 'Active')";

        try (PreparedStatement psP = conn.prepareStatement(pSql);
             PreparedStatement psI = conn.prepareStatement(iSql);
             PreparedStatement psB = conn.prepareStatement(bSql)) {

            for (Object[] row : products) {
                int pid = (Integer) row[0];
                psP.setInt(1, pid);
                psP.setString(2, (String) row[1]);
                psP.setString(3, (String) row[2]);
                psP.setString(4, (String) row[3]);
                psP.setString(5, (String) row[4]);
                psP.setString(6, (String) row[5]);
                psP.setDouble(7, (Double) row[6]);
                psP.setDouble(8, (Double) row[7]);
                psP.setDouble(9, (Double) row[8]);
                psP.setDouble(10, (Double) row[9]);
                psP.setDouble(11, (Double) row[10]);
                psP.setDouble(12, (Double) row[11]);
                psP.setDouble(13, (Double) row[12]);
                psP.executeUpdate();

                int sacks = (Integer) row[13];
                int p25 = (Integer) row[14];
                int p20 = (Integer) row[15];
                int p10 = (Integer) row[16];
                int p5 = (Integer) row[17];
                double kg = (Double) row[18];
                String batchCode = (String) row[19];
                double reorder = (Double) row[20];

                psI.setInt(1, pid);
                psI.setInt(2, sacks);
                psI.setInt(3, p25);
                psI.setInt(4, p20);
                psI.setInt(5, p10);
                psI.setInt(6, p5);
                psI.setDouble(7, kg);
                psI.setString(8, batchCode);
                psI.setDouble(9, reorder);
                psI.executeUpdate();

                psB.setInt(1, pid);
                psB.setString(2, batchCode);
                psB.setInt(3, sacks);
                psB.setInt(4, p25);
                psB.setInt(5, p20);
                psB.setInt(6, p10);
                psB.setInt(7, p5);
                psB.setDouble(8, kg);
                psB.executeUpdate();
            }
        }
    }
}
