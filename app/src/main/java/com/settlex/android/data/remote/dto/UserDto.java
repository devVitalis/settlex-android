package com.settlex.android.data.remote.dto;

import com.google.firebase.Timestamp;

/**
 * Represents a user in firestore database
 */
public class UserDto {
    public String uid, email, phone, firstName, lastName, paymentId, profileUrl, profileDeleteUrl;
    public long balance, commissionBalance, referralBalance;
    public Timestamp createdAt;

    public UserDto() {
    }

    public UserDto(String uid, String firstName, String lastName, Timestamp createdAt, String paymentId, String profileUrl, String profileDeleteUrl, String email, String phone, long balance, long commissionBalance, long referralBalance) {
        this.uid = uid;
        this.email = email;
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
        this.paymentId = paymentId;
        this.profileUrl = profileUrl;
        this.profileDeleteUrl = profileDeleteUrl;
        this.balance = balance;
        this.commissionBalance = commissionBalance;
        this.referralBalance = referralBalance;
    }
}
