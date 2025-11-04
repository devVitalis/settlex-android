package com.settlex.android.ui.dashboard.model;

import com.google.firebase.Timestamp;

public class UserUiModel {
    private final String uid, email, firstName, lastName, phone, paymentId, photoUrl;
    boolean hasPin;
    private final long balance, commissionBalance, referralBalance;
    private final Timestamp createdAt;

    public UserUiModel(String uid, String email, String firstName, String lastName, Timestamp createdAt, String phone, String paymentId, String photoUrl, boolean hasPin, long balance, long commissionBalance, long referralBalance) {
        this.uid = uid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
        this.paymentId = paymentId;
        this.photoUrl = photoUrl;
        this.hasPin = hasPin;
        this.phone = phone;
        this.balance = balance;
        this.commissionBalance = commissionBalance;
        this.referralBalance = referralBalance;
    }

    // GETTERS
    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public boolean hasPin() {
        return hasPin;
    }

    public String getPhone() {
        return phone;
    }

    public long getBalance() {
        return balance;
    }

    public long getCommissionBalance() {
        return commissionBalance;
    }

    public long getReferralBalance() {
        return referralBalance;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }
}
