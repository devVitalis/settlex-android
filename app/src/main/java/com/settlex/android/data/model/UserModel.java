package com.settlex.android.data.model;

/**
 * Represents a user entity with personal and security data.
 * Used for both Firestore operations and local storage.
 */
public class UserModel {
    private String uid;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phone;
    private String referralCode;
    private String pin;
    private String pinSalt;
    public boolean hasPin;

    public UserModel() {
    }

    public UserModel(String firstName, String lastName, String username, String email, String phone, String referralCode, String pin, String pinSalt, boolean hasPin) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.referralCode = referralCode;
        this.pin = pin;
        this.pinSalt = pinSalt;
        this.hasPin = hasPin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getPinSalt() {
        return pinSalt;
    }

    public void setPinSalt(String pinSalt) {
        this.pinSalt = pinSalt;
    }

    public boolean getHasPin() {
        return hasPin;
    }

    public void setHasPin(boolean hasPin) {
        this.hasPin = hasPin;
    }
}