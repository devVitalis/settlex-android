package com.settlex.android.ui.dashboard.model;

/**
 * UI model representing a transaction for display purposes
 */
public class TransactionUiModel {
    private String txnId;
    private String txnReference;
    private final String senderName;
    private final String receiverName;
    private String description;
    private final String amount;
    private final String timestamp;
    private final String serviceTypeName;
    private final int serviceTypeIcon;
    private final String status;
    private final int statusColor;
    private final String operationSymbol;
    private final int operationColor;

    /**
     * Constructor for basic transaction display (minimal required fields)
     */
    public TransactionUiModel(String senderName, String receiverName, String serviceTypeName, int serviceTypeIcon, String operationSymbol, int operationColor, String amount, String timestamp, String status, int statusColor) {
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.serviceTypeName = serviceTypeName;
        this.serviceTypeIcon = serviceTypeIcon;
        this.operationSymbol = operationSymbol;
        this.operationColor = operationColor;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
        this.statusColor = statusColor;
    }

    // GETTERS
    public String getSenderName() {
        return senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getServiceTypeName() {
        return serviceTypeName;
    }

    public int getServiceTypeIcon() {
        return serviceTypeIcon;
    }

    public String getOperationSymbol() {
        return operationSymbol;
    }

    public int getOperationColor() {
        return operationColor;
    }

    public String getAmount() {
        return amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public int getStatusColor() {
        return statusColor;
    }
}