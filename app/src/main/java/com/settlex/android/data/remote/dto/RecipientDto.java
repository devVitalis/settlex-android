package com.settlex.android.data.remote.dto;

public class RecipientDto {
    public String paymentId;
    public String firstName;
    public String lastName;
    public String photoUrl;

    public RecipientDto() {
        // for deserialization
    }

    public RecipientDto(String paymentId, String firstName, String lastName, String photoUrl) {
        this.paymentId = paymentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoUrl = photoUrl;
    }
}
