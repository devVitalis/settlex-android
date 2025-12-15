package com.settlex.android.presentation.dashboard.home.model

data class HomeUiModel(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val photoUrl: String?,
    val hasPin: Boolean,
    val balance: Long,
    val commissionBalance: Long,
    val paymentId: String?
) {

    val fullName: String get() = "$firstName $lastName"

    fun hasPin(): Boolean {
        return hasPin
    }
}