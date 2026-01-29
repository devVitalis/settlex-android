package com.settlex.android.data.remote.dto;

import com.google.firebase.Timestamp;
import com.settlex.android.data.enums.TransactionServiceType;
import com.settlex.android.data.enums.TransactionStatus;

/**
 * DTO representing a transaction in Firestore
 */
public class TransactionDto {
    public String transactionId;
    public String transactionReference;
    public String senderUid;
    public String sender;
    public String senderName;
    public String recipientUid;
    public String recipient;
    public String recipientName;
    public String description;

    public long amount;
    public com.google.firebase.Timestamp createdAt;

    public TransactionStatus status;
    public TransactionServiceType serviceType;

    public TransactionDto() {
        // Firestore requires a no-arg constructor
    }

    public TransactionDto(String transactionId, String transactionReference, String senderUid, String sender, String senderName, String recipient, String recipientName, String recipientUid, String description, long amount, Timestamp createdAt, TransactionStatus status, TransactionServiceType serviceType) {
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.senderUid = senderUid;
        this.sender = sender;
        this.senderName = senderName;
        this.recipientUid = recipient;
        this.recipient = recipientUid;
        this.recipientName = recipientName;
        this.description = description;
        this.amount = amount;
        this.createdAt = createdAt;
        this.status = status;
        this.serviceType = serviceType;
    }
}
