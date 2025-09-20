package com.settlex.android.data.remote.dto;

/**
 * Represents a user in firestore database
 */
public class UserDto {
    public String uid, firstName, lastName, username, email, phone;
    public double balance, commissionBalance;

    public UserDto() {
    }

    public UserDto(String uid, String firstName, String lastName, String username, String email, String phone, double balance, double commissionBalance) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.balance = balance;
        this.commissionBalance = commissionBalance;
    }
}
