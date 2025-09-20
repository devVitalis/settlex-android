package com.settlex.android.domain.model;

/**
 * Represents a user entity with personal and security data.
 * Used for both Firestore operations and local storage.
 */
public class UserModel {

    // Fields ordered by logical grouping: identity -> contact -> security
    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String referralCode;
    private String pin;
    private String pinSalt;
    public boolean hasPin;  // Public flag for quick PIN status checks

    // ====================== CONSTRUCTORS ======================

    /**
     * Required for Firestore deserialization
     */
    public UserModel() {
    }

    /**
     * Full constructor for manual user creation.
     * Includes all required fields for account setup.
     */
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

    // ====================== GETTERS/SETTERS ======================
    // --- Identity ---
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

    // --- Contact ---
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

    // --- Security ---
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