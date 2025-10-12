package com.settlex.android.data.remote.dto;

/**
 * Represents a user in firestore database
 */
public class UserDto {
    public String uid, email, phone, firstName, lastName, username, profileUrl, profileDeleteUrl;
    public long balance, commissionBalance, referralBalance;

    public UserDto() {
    }

    public UserDto(String uid, String firstName, String lastName, String username, String profileUrl, String profileDeleteUrl, String email, String phone, long balance, long commissionBalance, long referralBalance) {
        this.uid = uid;
        this.email = email;
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.profileUrl = profileUrl;
        this.profileDeleteUrl = profileDeleteUrl;
        this.balance = balance;
        this.commissionBalance = commissionBalance;
        this.referralBalance = referralBalance;
    }
}
