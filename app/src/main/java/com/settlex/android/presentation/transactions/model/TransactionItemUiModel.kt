package com.settlex.android.presentation.transactions.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A data class representing a transaction, formatted for display in the user interface.
 * This model is parcelable to allow it to be passed between Android components, such as activities or fragments.
 */
@Parcelize
data class TransactionItemUiModel(
    val transactionId: String,
    val description: String?,
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val recipientName: String,
    val recipientOrSenderName: String,
    val serviceTypeName: String,
    val serviceTypeIcon: Int,
    val operationSymbol: String,
    val operationColor: Int,
    val amount: String,
    val timestamp: String,
    val status: String,
    val statusColor: Int,
    val statusBackgroundColor: Int
) : Parcelable