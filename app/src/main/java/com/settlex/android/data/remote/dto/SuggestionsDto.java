package com.settlex.android.data.remote.dto;

public class SuggestionsDto {
    public String username;
    public String firstName;
    public String lastName;
    public String profileUrl;

    public SuggestionsDto(String username, String firstName, String lastName, String profileUrl) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileUrl = profileUrl;
    }
}
