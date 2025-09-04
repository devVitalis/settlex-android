package com.settlex.android.data.remote.dto;

import com.google.firebase.Timestamp;
import com.settlex.android.data.enums.TransactionServiceType;
import com.settlex.android.data.enums.TransactionStatus;

/**
 * Production DTO representing a transaction in Firestore
 */

public class TransactionDto {
    public String transactionId, transactionReference, sender, senderUid, recipient, recipientUid, description;
    public double amount;
    public com.google.firebase.Timestamp createdAt;
    public TransactionStatus status;
    public TransactionServiceType serviceType;

    public TransactionDto() {
        // Firestore requires a no-arg constructor
    }

    public TransactionDto(String transactionId, String transactionReference, String sender, String senderUid, String recipient, String recipientUid, String description, double amount, Timestamp createdAt, TransactionStatus status, TransactionServiceType serviceType) {
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.sender = sender;
        this.senderUid = senderUid;
        this.recipientUid = recipient;
        this.recipient = recipientUid;
        this.description = description;
        this.amount = amount;
        this.createdAt = createdAt;
        this.status = status;
        this.serviceType = serviceType;
    }
}
