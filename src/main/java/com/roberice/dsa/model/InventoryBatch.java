package com.roberice.dsa.model;

public class InventoryBatch {
    private int batchId;
    private int productId;
    private String batchCode;
    private String receivedDate;
    private int daysOld;
    private int stockSacks;
    private int packaged25kg;
    private int packaged20kg;
    private int packaged10kg;
    private int packaged5kg;
    private double stockKg;
    private double totalKg;
    private String status;

    public InventoryBatch() {}

    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public String getReceivedDate() { return receivedDate; }
    public void setReceivedDate(String receivedDate) { this.receivedDate = receivedDate; }

    public int getDaysOld() { return daysOld; }
    public void setDaysOld(int daysOld) { this.daysOld = daysOld; }

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

    public double getTotalKg() { return totalKg; }
    public void setTotalKg(double totalKg) { this.totalKg = totalKg; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
