# 🌾 Roberice POS & Inventory Management System (Java / Eclipse Edition)

A complete desktop Java implementation of the **Roberice Point of Sale & Multi-Packaging Inventory Management System**, ready for instant import and execution in **Eclipse IDE**.

---

## 📂 Project Location
```
c:\xampp\htdocs\Roberice.DSA\
```

---

## 🚀 How to Import into Eclipse (Step-by-Step)

### Option 1: Standard Eclipse Project Import (Recommended — Fastest)
1. Launch **Eclipse IDE**.
2. Click **File** in the top menu and select **Import...**
3. In the Import wizard:
   - Expand the **General** folder.
   - Select **Existing Projects into Workspace**.
   - Click **Next >**.
4. In the **Select root directory** field:
   - Click **Browse...**
   - Navigate to: `c:\xampp\htdocs\Roberice.DSA`
   - Click **Select Folder**.
5. You will see **`RobericePOS`** checked under the Projects box.
6. Click **Finish**.
7. The project will appear in your **Package Explorer** or **Project Explorer** tab with all source packages and libraries pre-configured!

---

### Option 2: Maven Project Import (Alternative)
1. In Eclipse, click **File** > **Import...**
2. Expand **Maven** > Select **Existing Maven Projects** > Click **Next >**.
3. In **Root Directory**, browse to `c:\xampp\htdocs\Roberice.DSA` (where `pom.xml` is located).
4. Click **Finish**.

---

## ▶️ Running the Application

### In Eclipse:
1. In the **Package Explorer**, navigate to:
   ```
   RobericePOS > src > com.roberice.pos.ui > RobericeApp.java
   ```
2. **Right-click** on [`RobericeApp.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/RobericeApp.java).
3. Select **Run As** > **Java Application**.
4. The **Roberice Sign-In** dialog will appear immediately!

### From Windows File Explorer (One-Click):
- Double-click [`run.bat`](file:///c:/xampp/htdocs/Roberice.DSA/run.bat) inside `Roberice.DSA`.

---

## 🔑 Default Login Credentials

| Role | Username | Default Password | Initial Permissions |
|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Full access across all 10 system modules |
| **Cashier** | `cashier1` | `cashier123` | POS Terminal, View Inventory, Transactions, Sales Report |
| **Cashier 2** | `cashier2` | `cashier123` | POS Terminal, View Inventory, Transactions, Sales Report |
| **Inventory Clerk** | `juan_inv` | `inv123` | View Inventory, Manage Inventory, Products & Pricing |

---

## 💾 Dual-Mode Database Engine (MySQL & SQLite Fallback)

The system is equipped with an intelligent **dual-mode database engine**:
1. **XAMPP MySQL Mode**:
   - Automatically connects to MySQL on `localhost:3306`, database `roberice_db`, username `root`, and empty password.
   - Shares the same database with the web application when XAMPP is started.
2. **Zero-Configuration SQLite Fallback**:
   - If MySQL / XAMPP is not running, the system automatically runs on an embedded SQLite database (`roberice.db`).
   - Pre-seeded with all **12 authentic Philippine rice varieties**, active batches, inventory balances, and staff accounts!
   - You can test and grade the program anywhere, even on machines without XAMPP installed.

---

## 📦 Features Implemented in the Java Program

| Module | Java Component | Features |
|---|---|---|
| **Authentication** | [`LoginDialog.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/LoginDialog.java) | Credential check, active account status validation, database engine badge, registration link. |
| **Registration Request** | [`RegisterDialog.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/RegisterDialog.java) | Submission of account requests for Admin approval. |
| **Executive Dashboard** | [`DashboardPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/DashboardPanel.java) | 5 KPI cards, 7-Day sales trend bar chart, centered variety sales donut chart with legend, recent orders table. |
| **Point of Sale** | [`PosPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/PosPanel.java) | Product cards grid with search and filters, Packaging modal (`50kg Whole`, `25kg Half`, `20kg`, `10kg`, `5kg`, `Tinatakal`), live change calculation, atomic checkout. |
| **Automated Sack Opening** | [`SalesDAO.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/db/SalesDAO.java) | Automatically opens whole sacks (50kg) from oldest FIFO batch if customer buys loose rice and loose kg is insufficient! |
| **Official Sales Receipt** | [`ReceiptDialog.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/ReceiptDialog.java) | Formatted official receipt with store header, address, item table, payment, change, and Print button. |
| **View Inventory** | [`ViewInventoryPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/ViewInventoryPanel.java) | Live variety summary, pre-pack bag counts, loose kg, total volume, FIFO batch lot drill-down, and row-level sack opening. |
| **Manage Inventory** | [`InventoryManagePanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/InventoryManagePanel.java) | Restock shipments, manual sack openings, repacking loose rice into consumer bags, physical count corrections, and spoilage write-offs. |
| **Products & Pricing** | [`ProductsPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/ProductsPanel.java) | Add / Edit varieties, origin (`Local`/`Imported`), wholesale sack price, loose kg rate, pre-pack prices, and status toggle. |
| **Transaction History** | [`TransactionsPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/TransactionsPanel.java) | Searchable sales order log with itemized receipt breakdown and reprinting. |
| **Sales Reports** | [`SalesReportPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/SalesReportPanel.java) | Multi-period analytics (Daily, Weekly, Monthly, Quarterly, Yearly, All Time), variety breakdown, CSV export, and print statements. |
| **File Management** | [`FileManagerPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/FileManagerPanel.java) | One-Click live database backup (exports SQL snapshot), document repository, file upload. |
| **Audit History** | [`AuditPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/AuditPanel.java) | Full chronological audit trail of all warehouse restocks, sack openings, repacks, corrections, and depletions. |
| **User Management** | [`UsersPanel.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/ui/panels/UsersPanel.java) | Staff accounts, password resets, role presets, granular 10-module permissions matrix, and registration request approval workflow. |

---

## 🏗️ Architecture & Package Layout

```
Roberice.DSA/
├── .classpath                          # Eclipse classpath descriptor
├── .project                            # Eclipse project descriptor
├── pom.xml                             # Maven build configuration
├── run.bat                             # One-click Windows runner
├── build.bat                           # Compilation script
├── ECLIPSE_IMPORT_GUIDE.md             # This guide
├── lib/                                # Self-contained library JARs
│   ├── flatlaf-3.4.jar                 # Modern desktop UI Look and Feel
│   ├── mysql-connector-j-8.3.0.jar     # MySQL JDBC Driver
│   ├── sqlite-jdbc-3.45.1.0.jar        # SQLite JDBC Driver
│   ├── slf4j-api-2.0.12.jar            # Logging API
│   └── slf4j-simple-2.0.12.jar         # Logging implementation
└── src/
    └── com/roberice/pos/
        ├── model/                      # Entity data models
        │   ├── Product.java
        │   ├── Inventory.java
        │   ├── InventoryBatch.java
        │   ├── InventoryAdjustment.java
        │   ├── SalesTransaction.java
        │   ├── TransactionItem.java
        │   ├── User.java
        │   ├── AccountRequest.java
        │   └── FileRecord.java
        ├── db/                         # Data Access Objects & Connections
        │   ├── DatabaseManager.java    # MySQL & SQLite dual connection manager
        │   ├── ProductDAO.java
        │   ├── InventoryDAO.java
        │   ├── SalesDAO.java
        │   ├── UserDAO.java
        │   ├── AuditDAO.java
        │   └── FileManagerDAO.java
        ├── service/                    # Business Logic Services
        │   ├── AuthSession.java        # User session & permissions state
        │   ├── ReportService.java      # Multi-period sales calculator & CSV exporter
        │   └── ReceiptPrinter.java     # Thermal receipt generator
        ├── ui/                         # User Interface
        │   ├── RobericeApp.java        # Main Application Launcher
        │   ├── MainFrame.java          # Persistent Sidebar & CardLayout Shell
        │   ├── LoginDialog.java        # Sign-in Dialog
        │   ├── RegisterDialog.java     # Account Request Dialog
        │   ├── PackagingModal.java     # POS Packaging & Quantity Selector
        │   ├── ReceiptDialog.java      # Receipt Viewer & Print Dialog
        │   ├── common/
        │   │   ├── UIUtils.java        # Design system tokens, styles & buttons
        │   │   └── CustomCharts.java   # 2D Sales Trend Bar & Donut Charts
        │   └── panels/                 # 10 Module Views
        │       ├── DashboardPanel.java
        │       ├── PosPanel.java
        │       ├── ViewInventoryPanel.java
        │       ├── InventoryManagePanel.java
        │       ├── ProductsPanel.java
        │       ├── TransactionsPanel.java
        │       ├── SalesReportPanel.java
        │       ├── FileManagerPanel.java
        │       ├── AuditPanel.java
        │       └── UsersPanel.java
        └── SystemVerifier.java         # Headless test verification suite
```
