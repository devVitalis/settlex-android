package com.settlex.android.data.remote.dto;

import com.google.firebase.Timestamp;
import com.settlex.android.data.enums.TransactionOperation;
import com.settlex.android.data.enums.TransactionServiceType;
import com.settlex.android.data.enums.TransactionStatus;

/**
 * Production DTO representing a transaction in Firestore
 */

public class TransactionDto {
    public String transactionId, transactionReference, sender, recipient, description;
    public double amount;
    public com.google.firebase.Timestamp createdAt;
    public TransactionStatus status;
    public TransactionOperation operation;
    public TransactionServiceType serviceType;

    public TransactionDto() {
        // Firestore requires a no-arg constructor
    }

    public TransactionDto(String transactionId, String transactionReference, String sender, String recipient, String description, double amount, Timestamp createdAt, TransactionStatus status, TransactionOperation operation, TransactionServiceType serviceType) {
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.sender = sender;
        this.recipient = recipient;
        this.description = description;
        this.amount = amount;
        this.createdAt = createdAt;
        this.status = status;
        this.operation = operation;
        this.serviceType = serviceType;
    }
}
