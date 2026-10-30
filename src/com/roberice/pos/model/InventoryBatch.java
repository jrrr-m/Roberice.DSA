package com.roberice.pos.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Represents an individual FIFO delivery batch / lot.
 */
public class InventoryBatch {
    private int batchId;
    private int productId;
    private String productName;
    private String batchCode;
    private Date receivedDate;
    private int stockSacks;
    private int packaged25kg;
    private int packaged20kg;
    private int packaged10kg;
    private int packaged5kg;
    private double stockKg;
    private String status = "Active"; // "Active" or "Depleted"
    private Date createdAt;
    private Date updatedAt;

    public InventoryBatch() {}

    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public Date getReceivedDate() { return receivedDate; }
    public void setReceivedDate(Date receivedDate) { this.receivedDate = receivedDate; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public double getTotalKg() {
        return (stockSacks * 50.0) + (packaged25kg * 25.0) + (packaged20kg * 20.0)
                + (packaged10kg * 10.0) + (packaged5kg * 5.0) + stockKg;
    }

    public long getAgeInDays() {
        if (receivedDate == null) return 0;
        LocalDate rec;
        if (receivedDate instanceof java.sql.Date) {
            rec = ((java.sql.Date) receivedDate).toLocalDate();
        } else {
            rec = receivedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        LocalDate now = LocalDate.now();
        return ChronoUnit.DAYS.between(rec, now);
    }

    public boolean isOldStock() {
        return "Active".equalsIgnoreCase(status) && getAgeInDays() > 60;
    }

    @Override
    public String toString() {
        return batchCode + " (" + stockSacks + " sacks, " + String.format("%.1f", stockKg) + " kg)";
    }
}
