# 🌾 Roberice POS & Multi-Packaging Inventory Management System (Desktop Java Edition)

A high-performance, enterprise-grade desktop Point of Sale (POS) and Multi-Packaging Inventory Management System customized for **Roberice**, a wholesale and retail rice merchandising business located in Marikina, Philippines.

Built entirely in **Java Swing** with modern **FlatLaf** design, supporting **Dual-Database Engines (MySQL with automated SQLite fallback)**, **FIFO automated sack depletions**, **multi-tier packaging calculations**, and **granular 10-module role-based permissions**.

---

## 📂 Repository Layout & Project Structure

The repository root is organized as a standard, self-contained Java/Maven and Eclipse project:

```
Roberice.DSA/
├── .classpath                          # Eclipse classpath configuration
├── .project                            # Eclipse project descriptor
├── .settings/                          # Eclipse JDT core preferences
├── pom.xml                             # Maven POM descriptor with all dependencies & build plugins
├── run.bat                             # One-click Windows application launcher
├── build.bat                           # Command-line Java compiler script
├── ECLIPSE_IMPORT_GUIDE.md             # Visual guide for importing and running in Eclipse
├── USER_MANUAL.md                      # Comprehensive Administrator & Cashier User's Manual
├── README.md                           # Master project documentation
│
├── roberice.db                         # Pre-seeded embedded SQLite database (1-year history: 3,727 orders)
├── roberice_db.sql                     # MySQL database schema and operational dataset
│
├── lib/                                # Self-contained library JARs (no Maven installation required)
│   ├── flatlaf-3.4.jar                 # Modern Swing desktop Look and Feel
│   ├── sqlite-jdbc-3.45.1.0.jar        # Embedded SQLite JDBC driver
│   ├── mysql-connector-j-8.3.0.jar     # Official MySQL JDBC connector
│   ├── slf4j-api-2.0.12.jar            # Logging facade
│   └── slf4j-simple-2.0.12.jar         # Logging implementation
│
└── src/                                # Java source code
    └── com/roberice/pos/
        ├── ui/                         # Desktop UI Frames, Dialogs, and Custom Controls
        │   ├── RobericeApp.java        # Main application entry point & FlatLaf bootstrap
        │   ├── MainFrame.java          # Primary application window with dynamic sidebar & top navbar
        │   ├── LoginDialog.java        # Modal sign-in dialog with dual-engine badge
        │   ├── RegisterDialog.java     # Account request registration dialog
        │   ├── PackagingModal.java     # Packaging tier modal (50kg, 25kg, 20kg, 10kg, 5kg, Tinatakal)
        │   ├── ReceiptDialog.java      # Official thermal receipt modal & print preview
        │   ├── common/
        │   │   ├── UIUtils.java        # Palette tokens, typography, badges, and component builders
        │   │   └── CustomCharts.java   # Lightweight Java2D vector bar charts & donut charts
        │   └── panels/                 # 10 Dedicated Module Panels
        │       ├── DashboardPanel.java       # Executive KPIs, 7-day sales trend, and variety donut
        │       ├── PosPanel.java             # POS register, search/filters, cart, and checkout
        │       ├── ViewInventoryPanel.java   # Stock lookup, bag counts, loose kg, and FIFO lot drawer
        │       ├── InventoryManagePanel.java # Restocking, manual sack opening, adjustments, spoilage
        │       ├── ProductsPanel.java        # Rice variety catalog & multi-tier price management
        │       ├── TransactionsPanel.java    # Searchable order log with receipt viewing & reprint
        │       ├── SalesReportPanel.java     # Multi-period sales analytics, CSV export, print statement
        │       ├── FileManagerPanel.java     # Live database backup & store document repository
        │       ├── AuditPanel.java           # Chronological warehouse & inventory adjustment audit trail
        │       └── UsersPanel.java           # Staff accounts, roles, and 10-module permissions matrix
        │
        ├── model/                      # POJO Domain Entities
        │   ├── Product.java            # Rice variety model with multi-tier prices & stock limits
        │   ├── Inventory.java          # Aggregated inventory balance
        │   ├── InventoryBatch.java     # Traceable delivery batch with supplier, date, and lot number
        │   ├── InventoryAdjustment.java# Audit adjustment record (restock, open, repack, correction, damage)
        │   ├── SalesTransaction.java   # Sales order header
        │   ├── TransactionItem.java    # Order line item with packaging type & weight
        │   ├── User.java               # Staff user account with hashed password & permission tokens
        │   ├── AccountRequest.java     # Registration submission for admin approval
        │   └── FileRecord.java         # Document and database backup registry entry
        │
        ├── db/                         # Data Access Objects (DAO) & Persistence
        │   ├── DatabaseManager.java    # Dual-Engine connection manager (MySQL + SQLite fallback)
        │   ├── ProductDAO.java         # CRUD queries for rice products & catalog
        │   ├── InventoryDAO.java       # Real-time stock queries, batch drilldowns & FIFO tracking
        │   ├── SalesDAO.java           # Atomic sale checkout, receipt generation & FIFO sack opening
        │   ├── UserDAO.java            # User authentication, password hashing & permissions matrix
        │   ├── AuditDAO.java           # Warehouse audit trail logging & query filtering
        │   └── FileManagerDAO.java     # SQL backup dump generator & document metadata storage
        │
        └── service/                    # Business Logic & Infrastructure Services
            ├── AuthSession.java        # Singleton current user session & permission checker
            ├── ReportService.java      # Multi-period sales aggregator & CSV file writer
            └── ReceiptPrinter.java     # Java Print Service integration for POS thermal printers
```

---

## ⚡ Quick Start: Running the System

### Option 1: One-Click Windows Launch (No IDE Needed)
Double-click [`run.bat`](file:///c:/xampp/htdocs/Roberice.DSA/run.bat) in the project root:
- Automatically detects standard `java` or Eclipse JustJ JRE on your system.
- Launches the application directly with all pre-packaged dependencies in `lib/`.
- Ready instantly!

### Option 2: Command-Line Compilation & Run
1. To compile all source files into `bin/`:
   ```cmd
   build.bat
   ```
2. To run the compiled application:
   ```cmd
   run.bat
   ```

### Option 3: Maven CLI
```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.roberice.pos.ui.RobericeApp"
```

### Option 4: Eclipse IDE
1. Open **Eclipse IDE**.
2. Click **File** > **Import...** > **General** > **Existing Projects into Workspace**.
3. Browse to `c:\xampp\htdocs\Roberice.DSA` and click **Finish**.
4. In the Package Explorer, right-click [`RobericeApp.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/RobericeApp.java) > **Run As** > **Java Application**.

---

## 🔑 Default User Accounts & Credentials

| Role | Username | Default Password | Initial Permissions |
|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Full access across all 10 system modules |
| **Cashier** | `cashier1` | `cashier123` | Point of Sale, View Inventory, Transactions, Sales Report |
| **Store Manager** | `manager1` | `manager123` | Dashboard, POS, View Inventory, Products, Transactions, Sales Report, Files, Audit |
| **Inventory Clerk** | `clerk1` | `clerk123` | View Inventory, Manage Inventory, Products & Pricing |

---

## 🌟 Key Application Features

### 1. Dual-Database Architecture (MySQL + Zero-Config SQLite Fallback)
[`DatabaseManager.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/db/DatabaseManager.java) handles database connectivity automatically:
- First attempts to connect to **MySQL** via XAMPP (`localhost:3306/roberice_db`).
- If MySQL is offline or not installed, it **automatically falls back to the embedded SQLite database (`roberice.db`)**.
- Displays an engine status badge (`🟢 MySQL Connected` or `🟡 SQLite Fallback`) on the login dialog and status bar.
- Pre-seeded with a **complete 1-year historical dataset** (3,727 orders, ₱3.4M revenue, 12 varieties, active FIFO delivery batches).

### 2. Multi-Packaging Pricing & Custom Tinatakal Selection
[`PackagingModal.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/PackagingModal.java) provides cashier selection across all commercial packaging tiers:
- **50kg Whole Sack** (wholesale)
- **25kg Half Sack**
- **20kg Bag**, **10kg Bag**, **5kg Bag** (consumer pre-packs)
- **Tinatakal (Custom Loose Kilograms)**: Direct numeric input or one-click quick kilo chips (`+0.5kg`, `+1kg`, `+2kg`, `+5kg`, `+10kg`).

### 3. FIFO Automated Sack Opening during Checkout
When a customer purchases loose rice and the display bin's loose stock is insufficient, [`SalesDAO.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/db/SalesDAO.java) automatically:
- Finds the oldest active delivery batch (First-In, First-Out).
- Opens an unopened 50kg whole sack.
- Adds +50 kg to the loose inventory balance.
- Deducts the requested purchase weight and leaves the remaining kilograms in the loose bin.
- Logs an audit adjustment event in real time.

### 4. Official Thermal Receipt Printing & Reprinting
[`ReceiptDialog.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/ReceiptDialog.java) and [`ReceiptPrinter.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/service/ReceiptPrinter.java):
- Renders an official receipt layout formatted for 58mm/80mm POS thermal printers.
- Includes store header, business registration details, date/time, cashier username, itemized breakdown, payment method, cash received, and change due.
- Provides a one-click **Print Receipt** action invoking native Java Print Service.
- Past orders can be re-viewed and reprinted at any time from [`TransactionsPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/TransactionsPanel.java).

### 5. Multi-Period Sales Reports & CSV Exporter
[`SalesReportPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/SalesReportPanel.java) and [`ReportService.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/service/ReportService.java):
- **Daily Report**: Filter by date with one-click "Today" shortcut.
- **Weekly Report**: Select by calendar week with Monday–Sunday date range calculation.
- **Monthly Report**: Filter by calendar month.
- **Quarterly Report**: Q1, Q2, Q3, Q4 breakdown.
- **Yearly Report**: Full-year fiscal analysis.
- **All Time & Custom**: Date-range filtering.
- **One-Click Actions**: **Export CSV** (generates formatted spreadsheet in `reports/`) and **Print Official Statement** for store management sign-offs.

### 6. One-Click Database Backups & Document Archive
[`FileManagerPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/FileManagerPanel.java):
- **One-Click Live Backup**: Generates timestamped SQL snapshots stored in the `backups/` directory.
- **Document Management**: Archive supplier delivery receipts, invoices, and audit PDFs.

### 7. Warehouse FIFO Batch Lots & Inventory Adjustments
[`InventoryManagePanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/InventoryManagePanel.java):
- Restock delivery shipments with batch/lot numbers and supplier details.
- Manual sack opening with live sync preview.
- Repacking loose kilograms into pre-packaged consumer bags (20kg, 10kg, 5kg).
- Monthly physical inventory reconciliation and discrepancy notes.
- Damage, spoilage, and infestation write-off tracking.
- Stock aging alerts (>60 days in warehouse).

### 8. Granular 10-Module Permissions Matrix
[`UsersPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/UsersPanel.java) allows administrators to assign individual access tokens:
`dashboard`, `pos`, `view-inventory`, `inventory`, `products`, `transactions`, `sales-report`, `file-manager`, `audit`, `users`.
- Dynamic sidebar adjusts visible tabs based on the active user session.
- Secure registration request approval workflow.

---

## 🌾 Rice Varieties in Pre-Populated Dataset

| # | Variety Name | Classification | Origin | Whole Sack (50kg) | Retail Rate / Kg |
|---|---|---|---|---|---|
| 1 | **Sinandomeng Special** | Premium White | Local | ₱2,350.00 | ₱50.00 / kg |
| 2 | **Dinorado Fancy Special** | Aromatic | Local | ₱2,650.00 | ₱56.00 / kg |
| 3 | **Jasmine Fragrant Premium** | Fragrant Import | Imported | ₱2,800.00 | ₱60.00 / kg |
| 4 | **Angelika White Rice** | Regular Milled | Local | ₱2,200.00 | ₱46.00 / kg |
| 5 | **Malagkit Glutinous Rice** | Specialty Rice | Local | ₱3,100.00 | ₱68.00 / kg |
| 6 | **IR-64 Well-Milled Rice** | Standard Local | Local | ₱2,100.00 | ₱44.00 / kg |
| 7 | **Milagrosa Super Fragrant** | Aromatic | Local | ₱2,700.00 | ₱58.00 / kg |
| 8 | **Coco Pandan Premium** | Fragrant Import | Imported | ₱2,900.00 | ₱62.00 / kg |
| 9 | **Brown Rice Organic** | Whole Grain | Local | ₱3,250.00 | ₱72.00 / kg |
| 10 | **Japanese Calrose Rice** | Specialty Grain | Imported | ₱3,400.00 | ₱75.00 / kg |
| 11 | **Maharlika Premium** | Long Grain | Local | ₱2,450.00 | ₱52.00 / kg |
| 12 | **Super Angelica Supreme** | Premium White | Local | ₱2,300.00 | ₱48.00 / kg |

---

## 🏢 Store Information
- **Business Name**: Roberice
- **Address**: 73 Farmers St. Corner Ampalaya, Marikina 1800, Philippines
- **Line of Business**: Wholesale & Retail Rice Merchandising
- **Platform**: Desktop Java POS & Inventory System
