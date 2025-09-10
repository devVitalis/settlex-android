package com.settlex.android.ui.dashboard.model;

import java.util.Objects;

/**
 * UI model representing a transaction for display purposes
 */
public class TransactionUiModel {
    private final String transactionId;
    private String txnReference;
    private final String sender;
    private final String recipient;
    private final String recipientOrSender;
    private String description;
    private final String amount;
    private final String timestamp;
    private final String serviceTypeName;
    private final int serviceTypeIcon;
    private final String status;
    private final int statusColor;
    private final int statusBgColor;
    private final String operationSymbol;
    private final int operationColor;

    /**
     * Constructor for basic transaction display (minimal required fields)
     */
    public TransactionUiModel(String transactionId, String sender, String recipient, String recipientOrSender, String serviceTypeName, int serviceTypeIcon, String operationSymbol, int operationColor, String amount, String timestamp, String status, int statusColor, int statusBgColor) {
        this.transactionId = transactionId;
        this.sender = sender;
        this.recipient = recipient;
        this.recipientOrSender = recipientOrSender;
        this.serviceTypeName = serviceTypeName;
        this.serviceTypeIcon = serviceTypeIcon;
        this.operationSymbol = operationSymbol;
        this.operationColor = operationColor;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
        this.statusColor = statusColor;
        this.statusBgColor = statusBgColor;
    }

    // GETTERS

    public String getTransactionId() {
        return transactionId;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getRecipientOrSender() {
        return recipientOrSender;
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

    public int getStatusBgColor() {
        return statusBgColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionUiModel that = (TransactionUiModel) o;
        return getServiceTypeIcon() == that.getServiceTypeIcon() && getStatusColor() == that.getStatusColor() && getStatusBgColor() == that.getStatusBgColor() && getOperationColor() == that.getOperationColor() && Objects.equals(getTransactionId(), that.getTransactionId()) && Objects.equals(txnReference, that.txnReference) && Objects.equals(getSender(), that.getSender()) && Objects.equals(getRecipient(), that.getRecipient()) && Objects.equals(getRecipientOrSender(), that.getRecipientOrSender()) && Objects.equals(description, that.description) && Objects.equals(getAmount(), that.getAmount()) && Objects.equals(getTimestamp(), that.getTimestamp()) && Objects.equals(getServiceTypeName(), that.getServiceTypeName()) && Objects.equals(getStatus(), that.getStatus()) && Objects.equals(getOperationSymbol(), that.getOperationSymbol());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTransactionId(), txnReference, getSender(), getRecipient(), getRecipientOrSender(), description, getAmount(), getTimestamp(), getServiceTypeName(), getServiceTypeIcon(), getStatus(), getStatusColor(), getStatusBgColor(), getOperationSymbol(), getOperationColor());
    }
}