package com.settlex.android.data.remote.dto;

public class PaymentRecipientDto {
    public String paymentId;
    public String firstName;
    public String lastName;
    public String photoUrl;

    public PaymentRecipientDto() {
        // for deserialization
    }

    public PaymentRecipientDto(String paymentId, String firstName, String lastName, String photoUrl) {
        this.paymentId = paymentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoUrl = photoUrl;
    }
}
