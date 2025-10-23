package com.settlex.android.data.remote.dto;

public class RecipientDto {
    public String paymentId;
    public String firstName;
    public String lastName;
    public String profileUrl;

    public RecipientDto() {
        // for deserialization
    }

    public RecipientDto(String paymentId, String firstName, String lastName, String profileUrl) {
        this.paymentId = paymentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileUrl = profileUrl;
    }
}
