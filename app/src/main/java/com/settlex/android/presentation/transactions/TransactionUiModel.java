package com.settlex.android.presentation.transactions;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * UI model representing a transaction for display purposes
 */
public class TransactionUiModel implements Parcelable {
    private final String transactionId;
    private final String sender;
    private final String senderName;
    private final String recipient;
    private final String recipientName;
    private final String recipientOrSender;
    private final String description;
    private final String amount;
    private final String timestamp;
    private final String serviceTypeName;
    private final int serviceTypeIcon;
    private final String status;
    private final int statusColor;
    private final int statusBgColor;
    private final String operationSymbol;
    private final int operationColor;

    public TransactionUiModel(String transactionId, String description, String sender, String senderName, String recipient, String recipientName, String recipientOrSender, String serviceTypeName, int serviceTypeIcon, String operationSymbol, int operationColor, String amount, String timestamp, String status, int statusColor, int statusBgColor) {
        this.transactionId = transactionId;
        this.description = description;
        this.sender = sender;
        this.senderName = senderName;
        this.recipient = recipient;
        this.recipientName = recipientName;
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

    public String getDescription() {
        return description;
    }

    public String getSender() {
        return sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getRecipientName() {
        return recipientName;
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
        return getServiceTypeIcon() == that.getServiceTypeIcon()
                && getStatusColor() == that.getStatusColor()
                && getStatusBgColor() == that.getStatusBgColor()
                && getOperationColor() == that.getOperationColor()
                && Objects.equals(getTransactionId(), that.getTransactionId())
                && Objects.equals(getSender(), that.getSender())
                && Objects.equals(getSenderName(), that.getSenderName())
                && Objects.equals(getRecipient(), that.getRecipient())
                && Objects.equals(getRecipientName(), that.getRecipientName())
                && Objects.equals(getRecipientOrSender(), that.getRecipientOrSender())
                && Objects.equals(getDescription(), that.getDescription())
                && Objects.equals(getAmount(), that.getAmount())
                && Objects.equals(getTimestamp(), that.getTimestamp())
                && Objects.equals(getServiceTypeName(), that.getServiceTypeName())
                && Objects.equals(getStatus(), that.getStatus())
                && Objects.equals(getOperationSymbol(), that.getOperationSymbol());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTransactionId(), getSender(), getSenderName(), getRecipient(), getRecipientName(),
                getRecipientOrSender(), getDescription(), getAmount(), getTimestamp(),
                getServiceTypeName(), getServiceTypeIcon(), getStatus(), getStatusColor(),
                getStatusBgColor(), getOperationSymbol(), getOperationColor());
    }

    protected TransactionUiModel(Parcel in) {
        transactionId = in.readString();
        description = in.readString();
        sender = in.readString();
        senderName = in.readString();
        recipient = in.readString();
        recipientName = in.readString();
        recipientOrSender = in.readString();
        serviceTypeName = in.readString();
        serviceTypeIcon = in.readInt();
        operationSymbol = in.readString();
        operationColor = in.readInt();
        amount = in.readString();
        timestamp = in.readString();
        status = in.readString();
        statusColor = in.readInt();
        statusBgColor = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int i) {
        dest.writeString(transactionId);
        dest.writeString(description);
        dest.writeString(sender);
        dest.writeString(senderName);
        dest.writeString(recipient);
        dest.writeString(recipientName);
        dest.writeString(recipientOrSender);
        dest.writeString(serviceTypeName);
        dest.writeInt(serviceTypeIcon);
        dest.writeString(operationSymbol);
        dest.writeInt(operationColor);
        dest.writeString(amount);
        dest.writeString(timestamp);
        dest.writeString(status);
        dest.writeInt(statusColor);
        dest.writeInt(statusBgColor);
    }

    public static final Creator<TransactionUiModel> CREATOR = new Creator<>() {
        @Override
        public TransactionUiModel createFromParcel(Parcel in) {
            return new TransactionUiModel(in);
        }

        @Override
        public TransactionUiModel[] newArray(int size) {
            return new TransactionUiModel[size];
        }
    };
}