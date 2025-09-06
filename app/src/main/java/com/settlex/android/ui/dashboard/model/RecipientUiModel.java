package com.settlex.android.ui.dashboard.model;

import java.util.Objects;

public class RecipientUiModel {
    private final String username;
    private final String fullName;
    private final String profileUrl;

    public RecipientUiModel(String username, String fullName, String profileUrl) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipientUiModel that = (RecipientUiModel) o;
        return Objects.equals(getUsername(), that.getUsername()) && Objects.equals(getFullName(), that.getFullName()) && Objects.equals(getProfileUrl(), that.getProfileUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getFullName(), getProfileUrl());
    }
}
