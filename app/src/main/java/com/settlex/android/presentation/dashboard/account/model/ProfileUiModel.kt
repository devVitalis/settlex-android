package com.settlex.android.presentation.dashboard.account.model

import com.google.firebase.Timestamp

data class ProfileUiModel(
    val email: String,
    val firstName: String,
    val lastName: String,
    val createdAt: Timestamp?,
    val phone: String,
    val paymentId: String?,
    val photoUrl: String?
) {
    val fullName: String get() = "$firstName $lastName"
}