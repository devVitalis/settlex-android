package com.settlex.android.ui.dashboard.model;

import java.util.Objects;

public class RecipientUiModel {
    private final String paymentId;
    private final String fullName;
    private final String profileUrl;

    public RecipientUiModel(String paymentId, String fullName, String profileUrl) {
        this.paymentId = paymentId;
        this.fullName = fullName;
        this.profileUrl = profileUrl;
    }

    public String getPaymentId() {
        return paymentId;
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
        return Objects.equals(getPaymentId(), that.getPaymentId()) && Objects.equals(getFullName(), that.getFullName()) && Objects.equals(getProfileUrl(), that.getProfileUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPaymentId(), getFullName(), getProfileUrl());
    }
}
