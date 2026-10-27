package com.roberice.pos.model;

import java.util.Date;

/**
 * Represents an audit trail adjustment record (restock, sack opening, repack, correction, spoilage, depletion).
 */
public class InventoryAdjustment {
    private int adjustmentId;
    private int productId;
    private String productName;
    private String batchCode;
    private Date adjustmentDate;
    private String adjustmentType; // "restock", "open_sack", "repack", "correction", "spoilage", "batch_depleted"
    private int sacks;
    private double kg;
    private String note;
    private String performedBy;

    public InventoryAdjustment() {}

    public int getAdjustmentId() { return adjustmentId; }
    public void setAdjustmentId(int adjustmentId) { this.adjustmentId = adjustmentId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public Date getAdjustmentDate() { return adjustmentDate; }
    public void setAdjustmentDate(Date adjustmentDate) { this.adjustmentDate = adjustmentDate; }

    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }

    public int getSacks() { return sacks; }
    public void setSacks(int sacks) { this.sacks = sacks; }

    public double getKg() { return kg; }
    public void setKg(double kg) { this.kg = kg; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
}
