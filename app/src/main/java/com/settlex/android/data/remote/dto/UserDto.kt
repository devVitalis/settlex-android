package com.settlex.android.data.remote.dto

import com.google.firebase.Timestamp

/**
 * Represents a user in firestore database
 */
data class UserDto(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val createdAt: Timestamp,
    val paymentId: String?,
    val photoUrl: String?,
    val hasPin: Boolean,
    val email: String,
    val phone: String,
    val balance: Long,
    val commissionBalance: Long,
    val referralBalance: Long
)
