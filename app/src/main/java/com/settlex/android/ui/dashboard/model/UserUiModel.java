package com.settlex.android.ui.dashboard.model;

public class UserUiModel {
    private final String firstName, lastName, balance, commissionBalance;

    public UserUiModel(String firstName, String lastName, String balance, String commissionBalance) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.balance = balance;
        this.commissionBalance = commissionBalance;
    }

    // GETTERS
    public String getFirstName() {
        return firstName;
    }

    public String getBalance() {
        return balance;
    }

    public String getCommissionBalance() {
        return commissionBalance;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUserFullName() {
        return firstName + " " + lastName;
    }
}
