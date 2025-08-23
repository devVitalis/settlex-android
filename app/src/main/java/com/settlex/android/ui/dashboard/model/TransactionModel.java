package com.settlex.android.ui.dashboard.model;

/**
 * Record class model representing a transaction item
 */
public class TransactionModel {

    private String title, operation, status;
    double amount;
    long dateTime;

    public TransactionModel(String title, String operation, double amount, String status, long dateTime) {
        this.title = title;
        this.operation = operation;
        this.amount = amount;
        this.status = status;
        this.dateTime = dateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
}
