package com.roberice.pos.model;

/**
 * Line item in a sales order.
 */
public class TransactionItem {
    private int transactionItemId;
    private int transactionId;
    private int productId;
    private String productName;
    private String unit; // "sack", "25kg", "20kg", "10kg", "5kg", "tinatakal"
    private double quantity;
    private double unitPrice;
    private double subtotal;
    private String batchCode;

    public TransactionItem() {}

    public TransactionItem(int productId, String productName, String unit, double quantity, double unitPrice, double subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.unit = unit;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public int getTransactionItemId() { return transactionItemId; }
    public void setTransactionItemId(int transactionItemId) { this.transactionItemId = transactionItemId; }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public String getUnitDisplay() {
        if (unit == null) return "kg";
        switch (unit.toLowerCase()) {
            case "sack":
            case "50kg": return "50kg Whole Sack";
            case "25kg":
            case "half":
            case "half_sack": return "25kg Half Sack";
            case "20kg": return "20kg Bag";
            case "10kg": return "10kg Bag";
            case "5kg": return "5kg Bag";
            case "tinatakal":
            case "kg":
            default: return "Tinatakal (Kg)";
        }
    }
}
