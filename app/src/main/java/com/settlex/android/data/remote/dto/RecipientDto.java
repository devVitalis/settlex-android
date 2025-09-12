package com.settlex.android.data.remote.dto;

public class RecipientDto {
    public String username;
    public String firstName;
    public String lastName;
    public String profileUrl;

    public RecipientDto(String username, String firstName, String lastName, String profileUrl) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileUrl = profileUrl;
    }
}
