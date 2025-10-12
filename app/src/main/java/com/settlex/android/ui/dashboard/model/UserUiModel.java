package com.settlex.android.ui.dashboard.model;

public class UserUiModel {
    private final String uid, email, firstName, lastName, phone, username, profileUrl;
    private final long balance, commissionBalance, referralBalance;

    public UserUiModel(String uid, String email, String firstName, String lastName, String phone, String username, String profileUrl, long balance, long commissionBalance, long referralBalance) {
        this.uid = uid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.profileUrl = profileUrl;
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

    public String getUsername() {
        return username;
    }

    public String getProfileUrl() {
        return profileUrl;
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
