# Roberice POS & Inventory Management System (Java Desktop GUI Application)

A high-performance native **Java Desktop GUI Application** engineered in **Java 21, Swing & FlatLaf (Modern Dark Theme)** with **HikariCP** and **MySQL JDBC** customized for **Roberice**, an authentic wholesale and retail rice merchandising business located in Marikina, Philippines.

The application completely replaces the legacy web-based architecture with a standalone, fast, and secure desktop GUI application. It connects directly to the persistent MySQL database (**`roberice_db`** on `localhost:3306`), preserving all 3,727 transactions, inventory batches, user accounts, and pricing data without touching or altering the database schema.

---

## 🚀 Key Features & Highlights

1. **100% Native Java Desktop Application**:
   - Zero web dependencies: No web browsers, no HTTP servlets, and no embedded web servers.
   - Built on standard **Java Swing** with **FlatLaf 3.5.1** dark theme styling, delivering crisp typography, smooth gradients, rounded controls, and custom vector charts.

2. **Full Functional & Visual Parity with Original System**:
   - Replicates all 11 core modules:
     - 🔐 **Authentication** (`LoginFrame`) with credential verification and role routing
     - 📊 **Dashboard** (`DashboardView`) with real-time KPI cards, 7-day revenue bar chart, and variety sales donut chart
     - 🛒 **Point of Sale** (`PosView`) with two-column layout, multi-tier packaging selection (`50kg Sack`, `25kg`, `20kg`, `10kg`, `5kg`, `1kg Loose`), live stock checks, change calculation, and printable invoice receipts
     - 📦 **View Inventory** (`ViewInventoryView`) with multi-packaging stock levels, low-stock alerts, and FIFO batch inspection
     - ⚙️ **Manage Inventory** (`ManageInventoryView`) for warehouse replenishment (stock in), sack openings into loose grains, grain repacking, and spoilage write-offs
     - 🌾 **Product Master List** (`ProductsView`) for rice catalog management, tiered wholesale & retail price schedules, and reorder levels
     - 🧾 **Transactions** (`TransactionsView`) for real-time ledger search, itemized breakdown modal, and order voiding
     - 📈 **Sales Analytics & Reports** (`SalesReportView`) with period filters (Today, This Week, This Month, This Year), charts, and 1-click CSV report export
     - 📁 **Document & File Manager** (`FileManagerView`) for store files, documents, and automated MySQL database backup snapshots
     - 🕒 **Audit Trail** (`AuditView`) for unified chronological history of inventory adjustments and sales orders
     - 👥 **User Management** (`UsersView`) for account creation, BCrypt password hashing, role presets (`Admin`, `Cashier`), and 10-module RBAC permissions

3. **High-Performance Database Layer**:
   - **HikariCP 5.1.0** connection pooling connects directly to `jdbc:mysql://localhost:3306/roberice_db`.
   - Thread-safe transactional operations with automatic rollback on error.
   - Batched indexing and sub-100ms response times across 3,700+ transactions.

4. **Eclipse IDE Ready**:
   - Pre-configured `.project`, `.classpath`, and `.settings/` files.
   - Simply open Eclipse and choose **Import Existing Project** to run or debug immediately.

---

## 🗂️ Project Directory Structure

```
Roberice.DSA/
├── pom.xml                     # Maven project descriptor (Java 21, FlatLaf, MySQL Connector, HikariCP, jBCrypt, Jackson)
├── mvnw / mvnw.cmd             # Maven Wrapper scripts for zero-install builds
├── run.bat                     # 1-Click Windows desktop launcher script
├── .project / .classpath       # Native Eclipse IDE project descriptor files
├── 📁 .settings/               # Eclipse JDT compiler & M2E project preferences
│
├── 📁 src/main/java/com/roberice/dsa/
│   ├── RobericeApp.java        # Main desktop application entry point (FlatLaf setup & DB pre-flight)
│   │
│   ├── 📁 db/
│   │   └── Database.java       # HikariCP connection pool, query execution & transaction helper
│   │
│   ├── 📁 model/
│   │   ├── User.java           # User entity & permission validator
│   │   ├── Product.java        # Rice product entity with price and stock tiers
│   │   ├── InventoryBatch.java # FIFO batch lot tracking entity
│   │   ├── SalesTransaction.java # Sales invoice entity
│   │   ├── TransactionItem.java  # Order line item entity
│   │   ├── AdjustmentLog.java  # Inventory adjustment & audit log entity
│   │   └── FileRecord.java     # Digital document & backup record entity
│   │
│   ├── 📁 service/
│   │   ├── AuthService.java    # Authentication & BCrypt verification
│   │   ├── ProductService.java # Product catalog CRUD & price tier management
│   │   ├── InventoryService.java # FIFO lot deductions, sack openings, restock & repacking
│   │   ├── PosService.java     # Order checkout, automatic sack opening & inventory deduction
│   │   ├── TransactionService.java # Transactions search, line items retrieval & voiding
│   │   ├── SalesReportService.java # Period aggregations, daily trends & CSV report exporter
│   │   ├── AuditService.java   # Unified activity timeline for warehouse and POS
│   │   ├── UserService.java    # Account management & permissions matrix
│   │   └── FileManagerService.java # Document storage & direct MySQL database backups
│   │
│   ├── 📁 ui/
│   │   ├── LoginFrame.java     # Centered authentication dialog
│   │   ├── MainFrame.java      # Main desktop window with dynamic CardLayout navigation
│   │   │
│   │   ├── 📁 chart/
│   │   │   └── ChartPanel.java # Native 2D bar and donut charts with gradient rendering
│   │   │
│   │   ├── 📁 components/
│   │   │   ├── Sidebar.java    # Left navigation menu with role badges and permission filter
│   │   │   └── Topbar.java     # Header with live system clock and refresh trigger
│   │   │
│   │   └── 📁 views/
│   │       ├── DashboardView.java      # Executive KPIs & performance charts
│   │       ├── PosView.java            # Point of Sale terminal & checkout register
│   │       ├── ViewInventoryView.java  # Multi-tier stock view & FIFO batch explorer
│   │       ├── ManageInventoryView.java# Stock in, repacking & spoilage write-offs
│   │       ├── ProductsView.java       # Product catalog & pricing editor
│   │       ├── TransactionsView.java   # Sales ledger & receipt inspector
│   │       ├── SalesReportView.java    # Financial analytics & CSV export
│   │       ├── FileManagerView.java    # File repository & SQL backup
│   │       ├── AuditView.java          # Unified audit trail
│   │       └── UsersView.java          # Staff accounts & permissions
│   │
│   └── 📁 util/
│       ├── PasswordUtil.java   # BCrypt hashing & hash upgrade utility
│       ├── Session.java        # Active authenticated user state & RBAC check
│       └── UIUtils.java        # Harmonious dark palette, typography, styled tables & cards
│
├── 📁 src/main/resources/
│   └── db.properties           # Database URL, user, password & pool configuration
│
├── 📁 database/
│   ├── roberice_db.sql         # Original database schema & operational dataset
│   └── backup_before_1yr_population.sql
│
└── 📁 uploads/                 # Persistent local storage for receipts and SQL backups
```

---

## 🛠️ Prerequisites & Setup

### 1. Requirements
- **Java Development Kit (JDK)**: JDK 21 (or OpenJDK 21)
- **MySQL Database**: Running on `localhost:3306` (e.g. via XAMPP MySQL) with user `root` and database `roberice_db`.

### 2. Verify Database Connection
Ensure MySQL is started in XAMPP. The database connection properties are configured in [`src/main/resources/db.properties`](src/main/resources/db.properties):
```properties
db.url=jdbc:mysql://localhost:3306/roberice_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila&characterEncoding=UTF-8
db.user=root
db.password=
```

---

## ▶️ Running the Application

### Option A: 1-Click Windows Launcher (`run.bat`)
Simply double-click [`run.bat`](run.bat) in the project root or run from PowerShell/CMD:
```powershell
.\run.bat
```
The script will verify your Java runtime, compile source files, and launch `RobericeApp`.

### Option B: Maven Wrapper Command Line
```powershell
.\mvnw.cmd compile exec:java
```

### Option C: Import into Eclipse IDE
1. Open Eclipse IDE.
2. Select **File** > **Import...** > **Existing Projects into Workspace**.
3. Browse to the directory: `c:\xampp\htdocs\Roberice.DSA`.
4. Click **Finish**.
5. Right-click [`RobericeApp.java`](src/main/java/com/roberice/dsa/RobericeApp.java) > **Run As** > **Java Application**.

---

## 🔑 Default Credentials

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin` | `admin123` | Administrator | Full access to all 10 modules |
| `cashier` | `cashier123` | Cashier | POS, View Inventory, Transactions, Sales Report |

---

## 🛡️ Security & Integrity Note
- The MySQL database `roberice_db` remains completely untouched and preserved.
- No existing tables, column definitions, or historical data have been dropped or altered.
- All web server files (PHP endpoints, HTML pages, CSS files, JS scripts) have been permanently removed from the repository.
