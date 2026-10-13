package com.roberice.dsa.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private int productId;
    private String productName;
    private String category;
    private String origin;
    private String riceType;
    private String description;
    private double priceSack;
    private double price25kg;
    private double priceKg;
    private double price5kg;
    private double price10kg;
    private double price20kg;
    private double kgPerSack = 50.0;
    private String productStatus;

    // Inventory metrics
    private int stockSacks;
    private int packaged25kg;
    private double stockKg;
    private int packaged5kg;
    private int packaged10kg;
    private int packaged20kg;
    private String receivedDate;
    private String batchNumber;
    private int daysOld;
    private double reorderLevelKg;

    private List<InventoryBatch> batches = new ArrayList<>();

    public Product() {}

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getRiceType() { return riceType; }
    public void setRiceType(String riceType) { this.riceType = riceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPriceSack() { return priceSack; }
    public void setPriceSack(double priceSack) { this.priceSack = priceSack; }

    public double getPrice25kg() { return price25kg; }
    public void setPrice25kg(double price25kg) { this.price25kg = price25kg; }

    public double getPriceKg() { return priceKg; }
    public void setPriceKg(double priceKg) { this.priceKg = priceKg; }

    public double getPrice5kg() { return price5kg; }
    public void setPrice5kg(double price5kg) { this.price5kg = price5kg; }

    public double getPrice10kg() { return price10kg; }
    public void setPrice10kg(double price10kg) { this.price10kg = price10kg; }

    public double getPrice20kg() { return price20kg; }
    public void setPrice20kg(double price20kg) { this.price20kg = price20kg; }

    public double getKgPerSack() { return kgPerSack; }
    public void setKgPerSack(double kgPerSack) { this.kgPerSack = kgPerSack; }

    public String getProductStatus() { return productStatus; }
    public void setProductStatus(String productStatus) { this.productStatus = productStatus; }

    public int getStockSacks() { return stockSacks; }
    public void setStockSacks(int stockSacks) { this.stockSacks = stockSacks; }

    public int getPackaged25kg() { return packaged25kg; }
    public void setPackaged25kg(int packaged25kg) { this.packaged25kg = packaged25kg; }

    public double getStockKg() { return stockKg; }
    public void setStockKg(double stockKg) { this.stockKg = stockKg; }

    public int getPackaged5kg() { return packaged5kg; }
    public void setPackaged5kg(int packaged5kg) { this.packaged5kg = packaged5kg; }

    public int getPackaged10kg() { return packaged10kg; }
    public void setPackaged10kg(int packaged10kg) { this.packaged10kg = packaged10kg; }

    public int getPackaged20kg() { return packaged20kg; }
    public void setPackaged20kg(int packaged20kg) { this.packaged20kg = packaged20kg; }

    public String getReceivedDate() { return receivedDate; }
    public void setReceivedDate(String receivedDate) { this.receivedDate = receivedDate; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public int getDaysOld() { return daysOld; }
    public void setDaysOld(int daysOld) { this.daysOld = daysOld; }

    public double getReorderLevelKg() { return reorderLevelKg; }
    public void setReorderLevelKg(double reorderLevelKg) { this.reorderLevelKg = reorderLevelKg; }

    public List<InventoryBatch> getBatches() { return batches; }
    public void setBatches(List<InventoryBatch> batches) { this.batches = batches; }

    public double getTotalKg() {
        return (stockSacks * 50.0) + (packaged25kg * 25.0) + (packaged20kg * 20.0) + (packaged10kg * 10.0) + (packaged5kg * 5.0) + stockKg;
    }

    public boolean isLowStock() {
        return getTotalKg() <= reorderLevelKg;
    }

    @Override
    public String toString() {
        return productName;
    }
}
