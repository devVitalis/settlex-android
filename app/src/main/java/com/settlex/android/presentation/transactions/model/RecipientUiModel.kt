package com.settlex.android.presentation.transactions.model

data class RecipientUiModel(
    @JvmField val paymentId: String,
    @JvmField val fullName: String,
    @JvmField val photoUrl: String?
)
