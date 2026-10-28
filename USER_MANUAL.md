# 🌾 ROBERICE POS & INVENTORY MANAGEMENT SYSTEM
## Comprehensive User's Manual (Desktop Java Application Edition)

**Business Name:** Roberice  
**Location:** 73 Farmers St. Corner Ampalaya, Marikina, Philippines  
**System Type:** Desktop Point of Sale (POS) and Multi-Packaging Inventory Management System (Java Swing / FlatLaf)  
**Document Version:** 2.0 (2026 Desktop Edition)  

---

## 📌 Document Overview & Instructions for Screenshots

This User’s Manual provides complete, step-by-step operating procedures for both **Cashier** and **Administrator** staff operating the **Roberice Desktop Java Application**.

> [!TIP]
> **To the User / Document Owner:**
> Wherever you see a **📸 SCREENSHOT PLACEHOLDER** box throughout this manual, capture a screenshot of that specific screen from your running Java desktop application, save it with the recommended file name in an `images/` or `manual_assets/` folder, and replace the placeholder text with your markdown image tag:
> ```markdown
> ![Descriptive Caption](images/your_image_name.png)
> ```

---

# 📑 TABLE OF CONTENTS

1. [System Access & Technical Prerequisites](#-system-access--technical-prerequisites)
2. [PART 1: CASHIER USER'S MANUAL](#-part-1-cashier-users-manual)
   - [1.1 Launching & Logging In to the Desktop Terminal](#11-launching--logging-in-to-the-desktop-terminal)
   - [1.2 POS Interface Walkthrough](#12-pos-interface-walkthrough)
   - [1.3 Step-by-Step: Processing a Customer Sale](#13-step-by-step-processing-a-customer-sale)
     - [A. Selling Standard Whole Sacks (50kg) & Half Sacks (25kg)](#a-selling-standard-whole-sacks-50kg--half-sacks-25kg)
     - [B. Selling Pre-Packed Bags (20kg, 10kg, 5kg)](#b-selling-pre-packed-bags-20kg-10kg-5kg)
     - [C. Selling Custom Weight Loose Rice (Tinatakal per Kg)](#c-selling-custom-weight-loose-rice-tinatakal-per-kg)
     - [D. Automatic Sack Opening during POS Checkout](#d-automatic-sack-opening-during-pos-checkout)
   - [1.4 Completing Checkout & Payment Processing](#14-completing-checkout--payment-processing)
   - [1.5 Printing & Issuing the Customer Receipt](#15-printing--issuing-the-customer-receipt)
   - [1.6 Manual Sack Opening at Cashier Counter](#16-manual-sack-opening-at-cashier-counter)
   - [1.7 Checking Real-Time Stock (View Inventory)](#17-checking-real-time-stock-view-inventory)
   - [1.8 Viewing Past Transactions & Receipt Reprinting](#18-viewing-past-transactions--receipt-reprinting)
   - [1.9 Shift Balancing & End-of-Day Sales Report](#19-shift-balancing--end-of-day-sales-report)
   - [1.10 Secure Logout Procedure](#110-secure-logout-procedure)
3. [PART 2: ADMINISTRATOR USER'S MANUAL](#-part-2-administrator-users-manual)
   - [2.1 Administrator Login & Access Control](#21-administrator-login--access-control)
   - [2.2 Executive Dashboard & Store Analytics](#22-executive-dashboard--store-analytics)
   - [2.3 Product Catalog & Pricing Management](#23-product-catalog--pricing-management)
     - [A. Adding a New Rice Variety](#a-adding-a-new-rice-variety)
     - [B. Updating Prices & Low-Stock Alert Limits](#b-updating-prices--low-stock-alert-limits)
   - [2.4 Warehouse & Inventory Management](#24-warehouse--inventory-management)
     - [A. Restocking New Rice Shipments & Lot Registration](#a-restocking-new-rice-shipments--lot-registration)
     - [B. Converting Sacks into Loose Inventory](#b-converting-sacks-into-loose-inventory)
     - [C. Performing Physical Inventory Corrections](#c-performing-physical-inventory-corrections)
     - [D. Recording Damaged, Spoilage, or Infested Rice](#d-recording-damaged-spoilage-or-infested-rice)
     - [E. Managing Aging Stock (>60 Days FIFO Compliance)](#e-managing-aging-stock-60-days-fifo-compliance)
   - [2.5 Sales Reports & Financial Audit Statements](#25-sales-reports--financial-audit-statements)
     - [A. Generating Multi-Period Reports (Daily, Weekly, Monthly, Quarterly, Yearly)](#a-generating-multi-period-reports)
     - [B. Exporting Filtered CSV Reports](#b-exporting-filtered-csv-reports)
     - [C. Printing Official Store Audit Statements](#c-printing-official-store-audit-statements)
   - [2.6 Store Documents & One-Click Database Backups](#26-store-documents--one-click-database-backups)
     - [A. Triggering a One-Click Live Database Backup](#a-triggering-a-one-click-live-database-backup)
     - [B. Uploading & Archiving Store Documents](#b-uploading--archiving-store-documents)
   - [2.7 Warehouse Audit Trail & Compliance Log](#27-warehouse-audit-trail--compliance-log)
   - [2.8 User Management & Granular Permissions Matrix](#28-user-management--granular-permissions-matrix)
     - [A. Creating New User Accounts & Roles](#a-creating-new-user-accounts--roles)
     - [B. Configuring Granular Module Permissions](#b-configuring-granular-module-permissions)
     - [C. Password Reset & Account Deactivation](#c-password-reset--account-deactivation)
4. [System Troubleshooting & FAQ](#-system-troubleshooting--faq)

---

# 💻 System Access & Technical Prerequisites

### Hardware Requirements
- **Cashier Terminal / Admin PC:** Desktop PC or Laptop running Windows 10/11 (or macOS / Linux).
- **Display Resolution:** 1280x720 minimum (1920x1080 recommended for optimal dashboard viewing).
- **Receipt Printer:** Standard 58mm or 80mm ESC/POS thermal receipt printer (or any Windows-configured printer).
- **Java Runtime:** Java SE 17 or higher (or Eclipse JustJ OpenJDK runtime).

### Starting the Application
1. Navigate to the project root folder:
   ```
   c:\xampp\htdocs\Roberice.DSA\
   ```
2. Double-click [`run.bat`](file:///c:/xampp/htdocs/Roberice.DSA/run.bat).
3. The application will initialize the database (detecting local MySQL or falling back seamlessly to embedded SQLite `roberice.db`) and immediately display the **Roberice Sign-In Dialog**.

### Default Credentials (Initial Setup)
| Role | Username | Default Password | Initial Permissions |
|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Full access across all 10 system modules |
| **Cashier** | `cashier1` | `cashier123` | POS Terminal, View Inventory, Transactions, Sales Report |
| **Store Manager** | `manager1` | `manager123` | Operational access (Dashboard, POS, Stock, Sales, Audit) |
| **Inventory Clerk** | `clerk1` | `clerk123` | View Inventory, Manage Inventory, Products & Pricing |

---

# 🛒 PART 1: CASHIER USER'S MANUAL

---

## 1.1 Launching & Logging In to the Desktop Terminal

1. Launch [`run.bat`](file:///c:/xampp/htdocs/Roberice.DSA/run.bat) from the desktop or folder shortcut.
2. The modern **Roberice Sign-In Dialog** will appear:
   - Notice the **Database Status Badge** at the bottom right (`🟢 MySQL Connected` or `🟡 SQLite Fallback`).
3. Enter your assigned **Username** (e.g., `cashier1`).
4. Enter your confidential **Password** (e.g., `cashier123`).
5. Click the green **Sign In** button (or press `Enter`).
6. The system verifies credentials, sets up your active session, and opens the main application window with your authorized modules.

> 📸 **SCREENSHOT PLACEHOLDER #01: Desktop Login Dialog**  
> * **Recommended File:** `manual_cashier_01_login.png`  
> * **Instructions:** Capture the modern FlatLaf login dialog showing the Roberice branding, Username & Password fields, Sign In button, and database status badge.

---

## 1.2 POS Interface Walkthrough

Once logged in as a Cashier, the **Point of Sale** module displays an intuitive, high-efficiency split-panel interface:

> 📸 **SCREENSHOT PLACEHOLDER #02: Point of Sale Main Screen**  
> * **Recommended File:** `manual_cashier_02_pos_layout.png`  
> * **Instructions:** Capture the POS screen showing the left product grid, the search & category filters, and the right-hand Current Order cart panel.

### Interface Components:
- **Search Bar (Top Left):** Real-time text filter by rice variety name, origin, or classification.
- **Origin Filter Dropdown:** Quick filter for **All Origins**, **Local (PH)**, or **Imported**.
- **Variety / Grain Filter Dropdown:** Categorical filter (e.g., Sinandomeng, Dinorado, Jasmine, Well Milled).
- **Open Sack Quick Button:** Prominent green button in the top action area to manually open sacks into loose inventory when display bins run low.
- **Product Cards Grid:** Interactive tiles displaying each rice variety with origin tag, wholesale sack price, loose per-kg price, remaining whole sacks, and available loose kilograms.
- **Current Order Cart (Right Panel):** Displays active line items, packaging descriptions, quantity steppers, item totals, removal buttons, payment method selector, amount received input, real-time change calculation, and checkout button.

---

## 1.3 Step-by-Step: Processing a Customer Sale

### A. Selling Standard Whole Sacks (50kg) & Half Sacks (25kg)

1. On the product grid, locate the requested rice variety card (or type in the search bar).
2. Click on the card to open the **Product Packaging & Quantity Selection Modal**.

> 📸 **SCREENSHOT PLACEHOLDER #03: Packaging & Quantity Modal**  
> * **Recommended File:** `manual_cashier_03_packaging_modal.png`  
> * **Instructions:** Capture the Packaging Modal showing the packaging tiles (50kg Whole Sack, 25kg Half Sack, Pre-packs, and Tinatakal), batch selector, and quantity stepper.

3. In the **Rice Batch / Lot (FIFO Selection)** dropdown, the system automatically selects the oldest delivery batch to ensure First-In, First-Out inventory rotation.
4. Click on the **Whole Sack (50kg)** or **Half Sack (25kg)** option tile.
5. In the **Quantity Stepper**, use the `+` / `-` buttons (or type directly) to set the number of sacks.
6. Check the **Item Subtotal** preview at the bottom of the modal.
7. Click **Add to Order**. The item appears immediately in the **Current Order** cart on the right.

---

### B. Selling Pre-Packed Bags (20kg, 10kg, 5kg)

1. Click on the target rice variety card.
2. Inside the packaging modal, select the desired pre-pack size tile:
   - **20kg Bag**
   - **10kg Bag**
   - **5kg Bag**
3. The system checks available pre-pack stock for that specific bag tier.
4. Set the quantity needed.
5. Click **Add to Order**.

---

### C. Selling Custom Weight Loose Rice (Tinatakal per Kg)

For retail customers purchasing loose rice by specific kilogram amounts (e.g., 1 kg, 2.5 kg, 7 kg):

1. Click on the rice variety card in the POS grid.
2. Select the **Tinatakal / Loose Rice (Per Kg)** option tile.
3. The **Custom Kilos Configuration Section** will expand automatically.

> 📸 **SCREENSHOT PLACEHOLDER #04: Custom Kilos (Tinatakal) Selection**  
> * **Recommended File:** `manual_cashier_04_tinatakal_input.png`  
> * **Instructions:** Capture the packaging modal with the "Tinatakal" option active, showing the loose kg input box, quick kilo chips (+1kg, +2kg, +5kg), and the live Loose Stock Info badge.

4. Specify the exact weight in kilograms:
   - Type the exact weight (e.g., `3.5`) into the **KG** input field, OR
   - Click the **Quick Kilo Chips** (`+0.5 kg`, `+1 kg`, `+2 kg`, `+3 kg`, `+5 kg`, `+10 kg`) to increment the weight rapidly.
5. Review the calculated subtotal.
6. Click **Add to Order**.

---

### D. Automatic Sack Opening during POS Checkout

If a customer orders more loose rice than currently available in the display bin:
- An informational banner appears in the packaging modal:
  > *ℹ️ Requested kilos exceed current loose stock. Unopened sack(s) will automatically open upon completing sale.*
- You do **not** need to leave the POS screen.
- When checkout completes, [`SalesDAO.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/db/SalesDAO.java) automatically opens a 50kg whole sack from the oldest active FIFO batch, credits +50 kg to loose stock, deducts the customer's purchase weight, leaves the balance in the bin, and records the audit event!

---

## 1.4 Completing Checkout & Payment Processing

1. **Review Order Cart:**
   - Verify line items, packaging descriptions, and subtotals on the **Current Order** list.
   - To remove an item, click the red **trash bin** icon next to the item row.
   - To cancel the entire order, click the **Clear Cart** button.

> 📸 **SCREENSHOT PLACEHOLDER #05: Cart Summary & Payment Inputs**  
> * **Recommended File:** `manual_cashier_05_cart_checkout.png`  
> * **Instructions:** Capture the right-side cart panel with active items, showing Subtotal, Total Amount, Payment Method dropdown, Amount Received input, and Change calculation.

2. **Select Payment Method:**
   - Click the **Payment Method** dropdown: **Cash**, **GCash**, or **Bank Transfer**.
3. **Enter Amount Received:**
   - In the **Amount Received (₱)** field, type the cash tendered by the customer (e.g., `1000.00`).
   - The system instantly calculates and displays the exact **Change** in green text.
   - *Note: If the amount received is less than the total, the "Complete Sale" button remains disabled to prevent erroneous transactions.*
4. **Complete the Sale:**
   - Click the large green **Complete Sale** button (or press `Ctrl+Enter`).
   - The transaction is atomically saved, inventory is updated, and the **Official Receipt Dialog** appears automatically.

---

## 1.5 Printing & Issuing the Customer Receipt

1. Upon successful checkout, the **Receipt Dialog** displays the official thermal receipt layout:
   - **ROBERICE** store header & Marikina address
   - Official Receipt Reference Number & Timestamp
   - Cashier Username
   - Itemized list with packaging description, weight, unit rate, and line total
   - Subtotal, Amount Tendered, and Change Due
   - Official thank-you message

> 📸 **SCREENSHOT PLACEHOLDER #06: Official Printable Receipt Modal**  
> * **Recommended File:** `manual_cashier_06_receipt_modal.png`  
> * **Instructions:** Capture the thermal receipt preview modal showing the formatted store header, itemized breakdown, payment breakdown, and the "Print Receipt" button.

2. Click the blue **Print Receipt** button.
3. The system dispatches the formatted receipt to your default Windows thermal receipt printer (58mm/80mm) via standard Java Print Services.
4. Hand the printed receipt and change to the customer.
5. Click **Close** to return to an empty cart, ready for the next customer.

---

## 1.6 Manual Sack Opening at Cashier Counter

If display bins are running low during the day:

1. Click the green **Open Sack** button in the POS top action bar (or in View Inventory).
2. The **Open Sack into Loose Inventory Dialog** opens.

> 📸 **SCREENSHOT PLACEHOLDER #07: Cashier Open Sack Modal**  
> * **Recommended File:** `manual_cashier_07_open_sack.png`  
> * **Instructions:** Capture the Open Sack modal showing the Rice Variety selector, FIFO Batch selector, Sack Size to Open dropdown, and the Inventory Sync Preview box.

3. Select the **Rice Variety**.
4. Select the **Rice Batch to Open (FIFO Lot)**.
5. Select the **Sack Size** (e.g., 50kg Whole Sack, 25kg Half Sack, 20kg, 10kg, 5kg).
6. Enter the **Number of Sacks** to open (default: `1`).
7. Observe the **Inventory Sync Preview Box** (e.g., *Opening 1x 50kg sack will deduct 1 sack from whole sacks and add +50.00 kg into Loose inventory*).
8. (Optional) Enter a brief note (e.g., *Opened 1 sack for retail counter display*).
9. Click **Confirm & Open Sack**. The loose rice is immediately available for selling.

---

## 1.7 Checking Real-Time Stock (View Inventory)

1. In the left navigation sidebar, click **View Inventory**.
2. The **View Available Inventory** screen displays:
   - Rice Variety & Grain Classification
   - Whole Sack Price & Per-Kg Loose Rate
   - Whole Sacks Left in Warehouse
   - Pre-Packed Bag counts (25kg, 20kg, 10kg, 5kg)
   - Loose / Tinatakal available in Kg
   - Total Volume in Equivalent Kilograms
   - Single-line Status Badges:
     - 🟢 **In Stock**
     - 🟡 **Low Stock** (below store safety threshold)
     - 🔴 **Out of Stock**

> 📸 **SCREENSHOT PLACEHOLDER #08: View Inventory Table & Filters**  
> * **Recommended File:** `manual_cashier_08_view_inventory.png`  
> * **Instructions:** Capture the View Inventory screen showing the search bar, category filters, and the inventory table with stock count columns and status badges.

3. Use the search bar or filters to quickly look up stock for customer inquiries.
4. Click any row's **Batch Lots** button to see batch dates and received lot numbers.

---

## 1.8 Viewing Past Transactions & Receipt Reprinting

1. Click **Transaction History** in the sidebar.
2. The table displays past transactions chronologically.

> 📸 **SCREENSHOT PLACEHOLDER #09: Transaction History & Receipt Lookup**  
> * **Recommended File:** `manual_cashier_09_transactions.png`  
> * **Instructions:** Capture the Transaction History screen showing the Transactions Count, Total Revenue cards, the searchable table, and the action buttons.

3. Search by **Transaction ID** (e.g., `TXN-20261014-0012`), customer name, or date.
4. Click the **View Receipt** button on any row.
5. The receipt modal opens, allowing verification or one-click reprinting.

---

## 1.9 Shift Balancing & End-of-Day Sales Report

1. Click **Sales Report** in the sidebar.
2. Select the **Daily Report** tab (or click the **Today** shortcut button).
3. The report presents:
   - **Gross Revenue (₱):** Total collections.
   - **Transactions Completed:** Total number of customer orders.
   - **Total Rice Volume Sold (Kg):** Kilograms dispensed.
   - **Variety Sales Breakdown:** Itemized sales volume per rice variety.

> 📸 **SCREENSHOT PLACEHOLDER #10: Cashier Daily Sales Summary & Print Preview**  
> * **Recommended File:** `manual_cashier_10_daily_report.png`  
> * **Instructions:** Capture the Sales Report page showing the Daily tab, the summary metrics, and the top-right "Print Sales Report" action button.

4. Compare physical drawer cash against the **Cash Collected** total.
5. Click **Print Sales Report** in the top action bar to print the daily closing statement for store manager sign-off.

---

## 1.10 Secure Logout Procedure

1. At the bottom of the left sidebar, locate your **User Profile Card** showing your Name and Role.
2. Click the red **Logout** button.
3. The session is securely closed, and the application returns to the login dialog.

> 📸 **SCREENSHOT PLACEHOLDER #11: Sidebar User Card & Logout Button**  
> * **Recommended File:** `manual_cashier_11_logout.png`  
> * **Instructions:** Capture the lower section of the sidebar showing the user avatar, account name, role badge, and the red Logout button.

---

# 👑 PART 2: ADMINISTRATOR USER'S MANUAL

---

## 2.1 Administrator Login & Access Control

1. Launch [`run.bat`](file:///c:/xampp/htdocs/Roberice.DSA/run.bat).
2. Enter your **Admin Username** (default: `admin`) and **Password** (default: `admin123`).
3. Click **Sign In**.
4. The application opens the full **Executive Dashboard** with all 10 modules enabled in the sidebar.

---

## 2.2 Executive Dashboard & Store Analytics

The **Dashboard** serves as the central operational overview for Roberice store management:

> 📸 **SCREENSHOT PLACEHOLDER #12: Executive Admin Dashboard Overview**  
> * **Recommended File:** `manual_admin_01_dashboard.png`  
> * **Instructions:** Capture the full Admin Dashboard showing the 5 top KPI cards, the 7-Day Sales Trend bar chart, the centered Variety Sales donut chart, and the Top Selling / Low Stock lists.

### Dashboard Key Elements:
1. **Top KPI Cards:**
   - **Total Varieties:** Total active rice products registered.
   - **Inventory Value:** Calculated wholesale valuation of all sacks, pre-packs, and loose inventory in stock.
   - **Sales (7 Days):** Total revenue and count of transactions in the past 7 days.
   - **Low Stock Items:** Varieties that have fallen below safety thresholds.
   - **Old Stock Alert (>60 Days):** Warning count of batches remaining in warehouse over 60 days.

2. **Visual Vector Charts:**
   - **7-Day Sales Trend Chart:** Custom vector bar chart showing daily revenue trajectory.
   - **Product Categories / Variety Sales Donut Chart:** Centered chart displaying proportional sales shares across different rice varieties.

3. **Operational Lists:**
   - **Top Selling Products:** Ranking of the fastest-moving rice varieties.
   - **Low Stock & Reorder Alerts:** Direct list of products requiring immediate restocking.

---

## 2.3 Product Catalog & Pricing Management

Manage your product lines, wholesale sack prices, loose kilogram rates, and stock alert parameters via **Products & Pricing**:

> 📸 **SCREENSHOT PLACEHOLDER #13: Products & Pricing Catalog Table**  
> * **Recommended File:** `manual_admin_02_products_table.png`  
> * **Instructions:** Capture the Products & Pricing screen showing the table of rice varieties, pricing columns (Sack, Tinatakal, 5kg, 10kg, 20kg), and the top-right "+ Add New Rice Variety" button.

### A. Adding a New Rice Variety
1. In the top action bar, click **`+ Add New Rice Variety`**.
2. The **Add New Rice Variety Modal** opens:
   - **Variety Name:** Trade name (e.g., *Dinorado Special Mindoro*).
   - **Origin:** Select **Local** or **Imported**.
   - **Rice Type / Grain:** Enter grain type (e.g., *Dinorado*, *Sinandomeng*, *Jasmine*).
   - **Category:** Classification (e.g., *Special Aromatic*, *Premium White*).
   - **Whole Sack Price (50kg) (₱)** & **Half Sack Price (25kg) (₱)**.
   - **Price per Kg / Loose (₱)**.
   - **Pre-Pack Prices (20kg, 10kg, 5kg) (₱)**.
   - **Low-Stock Alert Threshold (Total Kg)**: Minimum safety stock in equivalent kilograms.
   - **Initial Stock Quantities:** Starting sacks, pre-packs, and loose kilograms.
3. Click **Save Rice Variety**.

---

### B. Updating Prices & Low-Stock Alert Limits
1. Locate the rice variety on the Products & Pricing table.
2. Click the **Edit** (pencil) button on the row.
3. Modify the desired prices or threshold.
4. Click **Save Rice Variety**. Changes reflect immediately in the POS register.

---

## 2.4 Warehouse & Inventory Management

Access **Manage Inventory** to perform restocking, repackaging, physical audit corrections, and spoilage logging:

> 📸 **SCREENSHOT PLACEHOLDER #14: Manage Inventory Table & Row Actions**  
> * **Recommended File:** `manual_admin_04_inventory_manage.png`  
> * **Instructions:** Capture the Manage Inventory screen showing the stock aging alert banner, variety inventory table, and the contextual row-level buttons (Restock, Open Sack, Adjust/Repack).

### A. Restocking New Rice Shipments & Lot Registration
When a delivery truck arrives:
1. Locate the delivered rice variety on the inventory table.
2. Click the green **Restock** button on that row.
3. The **Restock Modal** opens:
   - **Whole Sacks to Add (50kg)**
   - **Pre-Packaged Stock Bags to Add (5kg, 10kg, 20kg)**
   - **Date Received** (initializes FIFO age counter)
   - **Batch Number / Lot** (e.g., `LOT-202610-A1`)
   - **Supplier / Notes**
4. Click **Save Restock**. Stock and equivalent volume update instantly, and an audit trail entry is created.

---

### B. Converting Sacks into Loose Inventory
1. Click the blue **Open Sack** button on the target variety row.
2. Select the **FIFO Delivery Lot**.
3. Select the **Sack Size** (50kg, 25kg, 20kg, 10kg, or 5kg).
4. Enter the number of sacks to convert.
5. Review the **Inventory Sync Preview**.
6. Click **Confirm & Open Sack**.

---

### C. Performing Physical Inventory Corrections
1. Click **Adjust / Repack** on the variety row.
2. Under Adjustment Type, select **Inventory Correction (Set / Adjust Physical Count)**.
3. Enter actual audited physical counts (Whole Sacks, Pre-Packs, Loose Kg).
4. Enter a detailed reason (e.g., *Monthly Physical Warehouse Inventory Audit*).
5. Click **Save Adjustment**.

---

### D. Recording Damaged, Spoilage, or Infested Rice
1. Click **Adjust / Repack** on the variety row.
2. Select **Damage / Spoilage (Remove Damaged / Infested Stock)**.
3. Enter the damaged quantity to write off.
4. Enter a detailed reason (e.g., *Damaged sack due to rain water during transport*).
5. Click **Save Adjustment**. Stock is deducted, and the loss is logged for financial write-off.

---

### E. Managing Aging Stock (>60 Days FIFO Compliance)
1. When rice batches remain in the warehouse for over 60 days, the **Old Stock Alert Banner** highlights aging varieties.
2. Click **View Old Stocks** to filter aging inventory.
3. Prioritize these batches for loose sales or promotional retail discounts.

---

## 2.5 Sales Reports & Financial Audit Statements

Access the **Sales Report** module for multi-period sales analytics, CSV auditing, and printable management statements:

> 📸 **SCREENSHOT PLACEHOLDER #15: Multi-Period Sales Report Overview**  
> * **Recommended File:** `manual_admin_07_sales_report_tabs.png`  
> * **Instructions:** Capture the Sales Report screen showing the period tabs (Daily, Weekly, Monthly, Quarterly, Yearly, Custom), summary metrics, and the variety breakdown table.

### A. Generating Multi-Period Reports
Select the desired tab:
- **📅 Daily Report:** Pick a date (or click **Today**).
- **📆 Weekly Report:** Select a calendar week (displays Monday–Sunday date range).
- **🗓️ Monthly Report:** Select target month and year.
- **📊 Quarterly Report:** Select Q1, Q2, Q3, or Q4 and year.
- **📈 Yearly Report:** Select calendar year.
- **🔍 Custom Range & All Time:** Specify custom start and end dates.

---

### B. Exporting Filtered CSV Reports
1. Filter the report to your desired period.
2. Click **Export CSV** in the top action bar.
3. The system generates an itemized spreadsheet file in the `reports/` folder (e.g., `sales_report_monthly_2026-10.csv`) with complete order timestamps, customer names, receipt IDs, payment methods, and line totals.

---

### C. Printing Official Store Audit Statements
1. After filtering your target period, click **Print Sales Report** in the top action bar.
2. The formal **Official Sales Report Statement** includes:
   - Official Roberice header & business details
   - Active reporting period & timestamp
   - Total Gross Revenue, Sales Count, and Total Kilograms Sold
   - Detailed Rice Variety Volume & Revenue Table
   - Payment Method breakdown (Cash vs. Digital)
   - Formal sign-off lines: **Prepared By (Cashier)**, **Audited By (Admin)**, and **Approved By (Store Owner)**.

---

## 2.6 Store Documents & One-Click Database Backups

Access **File Management** to safeguard store data and archive financial documents:

> 📸 **SCREENSHOT PLACEHOLDER #16: File Management Repository & Backup Controls**  
> * **Recommended File:** `manual_admin_09_file_manager.png`  
> * **Instructions:** Capture the File Management screen showing the storage summary cards, file list, and the top-right "One-Click Database Backup" and "Upload New Document" buttons.

### A. Triggering a One-Click Live Database Backup
1. In the top action bar, click the purple **One-Click Database Backup** button.
2. The system executes a live backup of the database, exporting all tables, transactions, inventory lots, and user accounts.
3. An automated timestamped SQL file (e.g., `roberice_db_backup_2026-10-28_08-00-00.sql`) is created in the `backups/` folder and registered in the file list.

---

### B. Uploading & Archiving Store Documents
1. In the top action bar, click **Upload New Document**.
2. Select your file (PDF, CSV, Excel, Word, JPG, PNG, SQL up to 25MB).
3. Select the **Document Category** (e.g., *Supplier Invoice & Delivery Receipt*, *Sales Report*, *Inventory Audit*).
4. Enter descriptive notes.
5. Click **Upload File**.

---

## 2.7 Warehouse Audit Trail & Compliance Log

Access **Audit History** to inspect all warehouse events and operational logs:

> 📸 **SCREENSHOT PLACEHOLDER #17: Audit History & System Trail**  
> * **Recommended File:** `manual_admin_10_audit_history.png`  
> * **Instructions:** Capture the Audit History screen showing the 4 KPI metric cards (Total Events, Sacks Opened, Batches Depleted, Restocks), the event filters, and the chronological audit log table.

- **Automatic Event Recording:** Every restock, sack opening, physical count correction, damage write-off, and batch depletion is permanently logged with timestamps and user names.
- **Filtering:** Filter by event type or date ranges (**Today**, **Past 7 Days**, **Past 30 Days**).
- **Export Audit Log:** Click **Export CSV** in the top action bar to download the entire chronological audit trail.

---

## 2.8 User Management & Granular Permissions Matrix

Manage employee accounts via **User Management**:

> 📸 **SCREENSHOT PLACEHOLDER #18: User Management & Accounts Table**  
> * **Recommended File:** `manual_admin_11_user_management.png`  
> * **Instructions:** Capture the User Management screen showing the user statistics cards, search bar, and the users table with assigned roles, permission badges, and account statuses.

### A. Creating New User Accounts & Roles
1. Click the green **`+ Add New User`** button.
2. Fill in **Full Name**, **Username**, and initial **Password**.
3. Choose a **Role Preset**:
   - **Administrator (Full Access):** Automatically checks all 10 module permissions.
   - **Cashier:** POS, View Inventory, Transactions, and Sales Report.
   - **Inventory Clerk:** View Inventory, Manage Inventory, Products & Pricing.
   - **Store Manager:** All operational modules.
   - **Custom Staff:** Customize as desired.

---

### B. Configuring Granular Module Permissions
Check or uncheck any of the **10 Independent Module Tokens**:

| Module Token | Module Name | Permission Scope |
|---|---|---|
| `dashboard` | **Dashboard** | View revenue charts, KPIs, and store analytics |
| `pos` | **Point of Sale** | Operate cash register, ring sales, and print receipts |
| `view-inventory` | **View Inventory** | Look up stock quantities, sack counts, and batch lots |
| `inventory` | **Manage Inventory** | Restock shipments, open sacks, adjust counts, log spoilage |
| `products` | **Products & Pricing** | Add varieties, edit sack/kg prices, and set alerts |
| `transactions` | **Transactions** | View past customer orders and reprint receipts |
| `sales-report` | **Sales Report** | View daily/monthly sales, export CSV, print statements |
| `file-manager` | **File Manager** | Download backups, upload supplier receipts and invoices |
| `audit` | **Audit History** | Review warehouse adjustments and compliance logs |
| `users` | **User Management** | Create accounts, reset passwords, and edit permissions |

4. Click **Save User Account**.

---

### C. Password Reset & Account Deactivation
1. **Password Reset:** Click **Edit** on the user's row, type a new password, and click **Save**.
2. **Deactivation:** Change status from `Active` to `Inactive`. The employee is immediately blocked from signing in while preserving all historical transaction and audit records.

---

# 🔧 System Troubleshooting & FAQ

### Q1: When double-clicking `run.bat`, nothing happens or a console window flashes and closes.
- **Solution:** Verify that Java 17+ or Eclipse JustJ JRE is installed. Open a Command Prompt and type `java -version`. If Java is not found on your system PATH, ensure JDK 17+ is installed.

### Q2: What database is the desktop application using?
- **Solution:** The application features an automated dual-engine database manager ([`DatabaseManager.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/db/DatabaseManager.java)). If XAMPP MySQL (`localhost:3306`) is running, it connects to MySQL. If MySQL is not running or not installed, it automatically uses the embedded SQLite database (`roberice.db`). The active database engine is clearly indicated by the badge on the login dialog and bottom status bar.

### Q3: How do I configure thermal receipt printing?
- **Solution:** In Windows **Settings > Bluetooth & devices > Printers & scanners**, set your 58mm or 80mm thermal receipt printer as the default printer (or select it when the Java Print dialog appears upon clicking "Print Receipt").

### Q4: Can I run this system on multiple cashier terminals?
- **Solution:** Yes! When running in MySQL mode, point [`DatabaseManager.java`](file:///c:/xampp/htdocs/Roberice.DSA/src/com/roberice/pos/db/DatabaseManager.java) to the central store server's local IP address (e.g., `192.168.1.100:3306`), and all cashier terminals will sync sales and inventory in real time.

---

*Roberice POS & Multi-Packaging Inventory Management System — User's Manual (2026 Desktop Edition)*  
*Prepared for Roberice Rice Merchandising, Marikina, Philippines.*
