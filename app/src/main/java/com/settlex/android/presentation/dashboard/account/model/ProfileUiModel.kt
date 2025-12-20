package com.settlex.android.presentation.dashboard.account.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class ProfileUiModel(
    val email: String,
    val firstName: String,
    val lastName: String,
    @PropertyName("createdAt")
    val joinedDate: Timestamp,
    val phone: String,
    val paymentId: String?,
    val photoUrl: String?
) {
    val fullName: String get() = "$firstName $lastName"
}