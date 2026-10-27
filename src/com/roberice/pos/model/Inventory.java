package com.roberice.pos.model;

import java.util.Date;

/**
 * Aggregated inventory record for a product.
 */
public class Inventory {
    private int inventoryId;
    private int productId;
    private String productName;
    private String origin;
    private String riceType;
    private int stockSacks;
    private int packaged25kg;
    private int packaged20kg;
    private int packaged10kg;
    private int packaged5kg;
    private double stockKg;
    private String batchNumber;
    private Date receivedDate;
    private double reorderLevelKg = 200.0;
    private Date lastUpdated;

    public Inventory() {}

    public int getInventoryId() { return inventoryId; }
    public void setInventoryId(int inventoryId) { this.inventoryId = inventoryId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getRiceType() { return riceType; }
    public void setRiceType(String riceType) { this.riceType = riceType; }

    public int getStockSacks() { return stockSacks; }
    public void setStockSacks(int stockSacks) { this.stockSacks = stockSacks; }

    public int getPackaged25kg() { return packaged25kg; }
    public void setPackaged25kg(int packaged25kg) { this.packaged25kg = packaged25kg; }

    public int getPackaged20kg() { return packaged20kg; }
    public void setPackaged20kg(int packaged20kg) { this.packaged20kg = packaged20kg; }

    public int getPackaged10kg() { return packaged10kg; }
    public void setPackaged10kg(int packaged10kg) { this.packaged10kg = packaged10kg; }

    public int getPackaged5kg() { return packaged5kg; }
    public void setPackaged5kg(int packaged5kg) { this.packaged5kg = packaged5kg; }

    public double getStockKg() { return stockKg; }
    public void setStockKg(double stockKg) { this.stockKg = stockKg; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public Date getReceivedDate() { return receivedDate; }
    public void setReceivedDate(Date receivedDate) { this.receivedDate = receivedDate; }

    public double getReorderLevelKg() { return reorderLevelKg; }
    public void setReorderLevelKg(double reorderLevelKg) { this.reorderLevelKg = reorderLevelKg; }

    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }

    public double getTotalStockKg() {
        return (stockSacks * 50.0) + (packaged25kg * 25.0) + (packaged20kg * 20.0)
                + (packaged10kg * 10.0) + (packaged5kg * 5.0) + stockKg;
    }

    public String getStockStatus() {
        double totalKg = getTotalStockKg();
        if (totalKg <= 0) {
            return "Out of Stock";
        } else if (totalKg <= reorderLevelKg) {
            return "Low Stock";
        } else {
            return "In Stock";
        }
    }
}
