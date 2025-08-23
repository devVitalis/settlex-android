package com.settlex.android.ui.auth.model;

public class UserUiModel {
    private final String email;
    private final String displayName;

    public UserUiModel(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }
}
