package com.roberice.pos.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a customer sales order in Roberice.
 */
public class SalesTransaction {
    private int transactionId;
    private int userId;
    private String cashierName;
    private Date transactionDate;
    private double totalAmount;
    private double paymentAmount;
    private double changeAmount;
    private String paymentMethod = "Cash"; // "Cash", "GCash", "Maya"
    private String transactionStatus = "Completed"; // "Completed", "Cancelled"
    private List<TransactionItem> items = new ArrayList<>();

    public SalesTransaction() {}

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public Date getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Date transactionDate) { this.transactionDate = transactionDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(double paymentAmount) { this.paymentAmount = paymentAmount; }

    public double getChangeAmount() { return changeAmount; }
    public void setChangeAmount(double changeAmount) { this.changeAmount = changeAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }

    public List<TransactionItem> getItems() { return items; }
    public void setItems(List<TransactionItem> items) { this.items = items; }

    public void addItem(TransactionItem item) {
        this.items.add(item);
    }
}
