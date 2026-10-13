package com.roberice.dsa.model;

public class AdjustmentLog {
    private String id;
    private String rawDate;
    private String formattedDate;
    private String type;
    private String label;
    private String category;
    private String badgeClass;
    private String productName;
    private String origin;
    private String batchCode;
    private String qtyImpact;
    private String note;
    private String user;

    public AdjustmentLog() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRawDate() { return rawDate; }
    public void setRawDate(String rawDate) { this.rawDate = rawDate; }

    public String getFormattedDate() { return formattedDate; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBadgeClass() { return badgeClass; }
    public void setBadgeClass(String badgeClass) { this.badgeClass = badgeClass; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public String getQtyImpact() { return qtyImpact; }
    public void setQtyImpact(String qtyImpact) { this.qtyImpact = qtyImpact; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
}
