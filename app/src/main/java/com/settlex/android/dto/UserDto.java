package com.settlex.android.dto;

public class UserDto {
    private final String email;
    private final String displayName;

    public UserDto(String email, String displayName) {
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
