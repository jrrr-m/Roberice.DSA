package com.roberice.pos.model;

/**
 * Represents a Rice Variety product in the Roberice catalog.
 */
public class Product {
    private int productId;
    private String productName;
    private String category;
    private String origin; // "Local" or "Imported"
    private String riceType; // e.g. "Sinandomeng", "Dinorado", "Jasmine", etc.
    private String description;
    private double priceSack;
    private double price25kg;
    private double price20kg;
    private double price10kg;
    private double price5kg;
    private double priceKg;
    private double kgPerSack = 50.0;
    private String productStatus = "Active"; // "Active" or "Inactive"

    public Product() {}

    public Product(int productId, String productName, String category, String origin, String riceType,
                   String description, double priceSack, double price25kg, double price20kg,
                   double price10kg, double price5kg, double priceKg, double kgPerSack, String productStatus) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.origin = origin;
        this.riceType = riceType;
        this.description = description;
        this.priceSack = priceSack;
        this.price25kg = price25kg;
        this.price20kg = price20kg;
        this.price10kg = price10kg;
        this.price5kg = price5kg;
        this.priceKg = priceKg;
        this.kgPerSack = kgPerSack;
        this.productStatus = productStatus;
    }

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

    public double getPrice25kg() { return price25kg > 0 ? price25kg : (priceSack / 2.0); }
    public void setPrice25kg(double price25kg) { this.price25kg = price25kg; }

    public double getPrice20kg() { return price20kg > 0 ? price20kg : (priceKg * 20.0); }
    public void setPrice20kg(double price20kg) { this.price20kg = price20kg; }

    public double getPrice10kg() { return price10kg > 0 ? price10kg : (priceKg * 10.0); }
    public void setPrice10kg(double price10kg) { this.price10kg = price10kg; }

    public double getPrice5kg() { return price5kg > 0 ? price5kg : (priceKg * 5.0); }
    public void setPrice5kg(double price5kg) { this.price5kg = price5kg; }

    public double getPriceKg() { return priceKg; }
    public void setPriceKg(double priceKg) { this.priceKg = priceKg; }

    public double getKgPerSack() { return kgPerSack; }
    public void setKgPerSack(double kgPerSack) { this.kgPerSack = kgPerSack; }

    public String getProductStatus() { return productStatus; }
    public void setProductStatus(String productStatus) { this.productStatus = productStatus; }

    public double getUnitPriceForPackaging(String unit) {
        if (unit == null) return priceKg;
        String u = unit.trim().toLowerCase();
        switch (u) {
            case "sack":
            case "50kg":
                return getPriceSack();
            case "25kg":
            case "half":
            case "half_sack":
                return getPrice25kg();
            case "20kg":
                return getPrice20kg();
            case "10kg":
                return getPrice10kg();
            case "5kg":
                return getPrice5kg();
            case "tinatakal":
            case "kg":
            default:
                return getPriceKg();
        }
    }

    @Override
    public String toString() {
        return productName + " (" + origin + ")";
    }
}
