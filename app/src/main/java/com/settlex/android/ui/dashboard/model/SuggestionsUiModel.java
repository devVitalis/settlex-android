package com.settlex.android.ui.dashboard.model;

public class SuggestionsUiModel {
    private final String username;
    private final String fullName;
    private final String profileUrl;

    public SuggestionsUiModel(String username, String fullName, String profileUrl) {
        this.username = username;
        this.fullName = fullName;
        this.profileUrl = profileUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }
}
