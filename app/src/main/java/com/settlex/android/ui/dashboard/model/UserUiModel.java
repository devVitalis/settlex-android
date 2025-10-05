package com.settlex.android.ui.dashboard.model;

public class UserUiModel {
    private final String uid, firstName, lastName, username;
    private final long balance, commissionBalance, referralBalance;

    public UserUiModel(String uid, String firstName, String lastName, String username, long balance, long commissionBalance, long referralBalance) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.balance = balance;
        this.commissionBalance = commissionBalance;
        this.referralBalance = referralBalance;
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

    public long getBalance() {
        return balance;
    }

    public long getCommissionBalance() {
        return commissionBalance;
    }

    public long getReferralBalance() {
        return referralBalance;
    }

    public String getUserFullName() {
        return getFirstName() + " " + getLastName();
    }
}
