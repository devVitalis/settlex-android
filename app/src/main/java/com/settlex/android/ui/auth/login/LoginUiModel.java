package com.settlex.android.ui.auth.login;

public class LoginUiModel {
    private final String uid;
    private final String email;
    private final String displayName;
    private final String photoUrl;

    public LoginUiModel(String uid, String email, String displayName, String photoUrl) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }
    public String getDisplayName() {
        return displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
