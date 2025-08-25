package com.settlex.android.data.remote.dto;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Represents a user transaction in Firestore
 */
public class TransactionDto {

    public String tnxId;
    public String description;
    public String status;
    public String type;
    public double amount;

    @ServerTimestamp
    public Date timestamp;

    public TransactionDto() {
        // Firestore requires a no-arg constructor
    }

    public TransactionDto(String id, String type, String description, Double amount, String status, Date timestamp) {
        this.tnxId = id;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Optional: add getters/setters if you prefer
}
