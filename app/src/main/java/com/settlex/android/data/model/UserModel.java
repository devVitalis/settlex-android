package com.settlex.android.data.model;

/**
 * Represents a user entity with personal and security data.
 * Used for both Firestore operations and local storage.
 */
public class UserModel {
    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String fcmToken;
    private String referralCode;
    public boolean hasPin;

    public UserModel() {
    }

    public UserModel(String firstName, String lastName, String email, String phone, String fcmToken, String referralCode, boolean hasPin) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.fcmToken = fcmToken;
        this.referralCode = referralCode;
        this.hasPin = hasPin;
    }

    // Getters

    public String getUid() {
        return uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public boolean isHasPin() {
        return hasPin;
    }

    // Setters

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public void setHasPin(boolean hasPin) {
        this.hasPin = hasPin;
    }
}