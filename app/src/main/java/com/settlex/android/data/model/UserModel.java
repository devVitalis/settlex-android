package com.settlex.android.data.model;

public class UserModel {

    private String createdAt;
    private String uid;
    private String role;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private double balance;
    private String referralCode;
    private String passcode;
    private String passcodeSalt;
    private boolean hasPasscode;

    public UserModel() { /* Required no-arg constructor for Firestore deserialization*/ }

    /*------------------------------------
    Public Constructor for initialization
    ------------------------------------*/
    public UserModel(String createdAt, String role, String firstName, String lastName, String email, String phone, double balance, String referralCode, String passcode, String passcodeSalt, boolean hasPasscode) {
        this.createdAt = createdAt;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.balance = balance;
        this.referralCode = referralCode;
        this.passcode = passcode;
        this.passcodeSalt = passcodeSalt;
        this.hasPasscode = hasPasscode;
    }

    /*--------------------
    Getters and Setters
    --------------------*/

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public String getPasscodeSalt() {
        return passcodeSalt;
    }

    public void setPasscodeSalt(String passcodeSalt) {
        this.passcodeSalt = passcodeSalt;
    }

    public boolean getHasPasscode() {
        return hasPasscode;
    }

    public void setHasPasscode(boolean hasPasscode) {
        this.hasPasscode = hasPasscode;
    }
}