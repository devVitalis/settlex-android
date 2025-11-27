package com.settlex.android.data.remote.dto

import com.google.firebase.Timestamp

/**
 * Represents a user in firestore database
 */
data class UserDto(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val createdAt: Timestamp? = null,
    val paymentId: String? = null,
    val photoUrl: String? = null,
    val hasPin: Boolean = false,
    val email: String = "",
    val phone: String = "",
    val balance: Long = 0L,
    val commissionBalance: Long = 0L,
    val referralBalance: Long = 0L
)
