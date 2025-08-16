package com.settlex.android.data.model;

import java.util.HashMap;
import java.util.Map;

public class UserModel {

    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String referralCode;
    private String pin;
    private String pinSalt;
    public boolean hasPin;

    public UserModel() { /* Required no-arg constructor for Firestore deserialization*/ }

    /*------------------------------------
    Public Constructor for initialization
    ------------------------------------*/
    public UserModel(String firstName, String lastName, String email, String phone, String referralCode, String pin, String pinSalt, boolean hasPin) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.referralCode = referralCode;
        this.pin = pin;
        this.pinSalt = pinSalt;
        this.hasPin = hasPin;
    }

    /*--------------------
    Getters and Setters
    --------------------*/
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

    /*---------------------------------------------
    Convert UserModel to Map for JSON-safe
    serialization in Firestore or Cloud Functions
    ----------------------------------------------*/
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("email", email);
        map.put("phone", phone);
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("referralCode", referralCode);
        map.put("pin", pin);
        map.put("pinSalt", pinSalt);
        map.put("hasPin", hasPin);
        return map;
    }

}