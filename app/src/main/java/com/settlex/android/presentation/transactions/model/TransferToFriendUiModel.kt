package com.settlex.android.presentation.transactions.model

import com.google.firebase.Timestamp

data class TransferToFriendUiModel (
    val uid: String,
    val firstName: String,
    val lastName: String,
    val createdAt: Timestamp?,
    val paymentId: String?,
    val hasPin: Boolean,
    val balance: Long,
    val commissionBalance: Long,
    val totalBalance: Long
)