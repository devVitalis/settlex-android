package com.settlex.android.presentation.wallet.model;

import java.util.Objects;

public class RecipientUiModel {
    private final String paymentId;
    private final String fullName;
    private final String photoUrl;

    public RecipientUiModel(String paymentId, String fullName, String photoUrl) {
        this.paymentId = paymentId;
        this.fullName = fullName;
        this.photoUrl = photoUrl;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipientUiModel that = (RecipientUiModel) o;
        return Objects.equals(getPaymentId(), that.getPaymentId()) && Objects.equals(getFullName(), that.getFullName()) && Objects.equals(getPhotoUrl(), that.getPhotoUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPaymentId(), getFullName(), getPhotoUrl());
    }
}
