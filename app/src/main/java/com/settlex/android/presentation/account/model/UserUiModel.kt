package com.settlex.android.presentation.account.model

import com.google.firebase.Timestamp

data class UserUiModel(
    @JvmField val uid: String,
    @JvmField val email: String,
    @JvmField val firstName: String,
    @JvmField val lastName: String,
    @JvmField val createdAt: Timestamp,
    @JvmField val phone: String,
    @JvmField val paymentId: String?,
    @JvmField val photoUrl: String?,
    @JvmField val hasPin: Boolean,
    @JvmField val balance: Long,
    @JvmField val commissionBalance: Long,
    @JvmField val referralBalance: Long
) {

    val fullName: String get() = "$firstName $lastName"

    fun hasPin(): Boolean {
        return hasPin
    }
}