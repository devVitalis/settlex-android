package com.settlex.android.ui.dashboard.model;

public class UserUiModel {
    private final String uid, firstName, lastName, username;
    private final double balance, commissionBalance;

    public UserUiModel(String uid, String firstName, String lastName, String username, double balance, double commissionBalance) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.balance = balance;
        this.commissionBalance = commissionBalance;
    }

    // GETTERS
    public String getUid() {
        return uid;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getUsername() {
        return username;
    }
    public double getBalance() {
        return balance;
    }
    public double getCommissionBalance() {
        return commissionBalance;
    }
    public String getUserFullName() {
        return firstName + " " + lastName;
    }
}
