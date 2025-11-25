package com.settlex.android.presentation.transactions.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A data class representing a transaction, formatted for display in the user interface.
 * This model is parcelable to allow it to be passed between Android components, such as activities or fragments.
 */
@Parcelize
data class TransactionUiModel(
    val transactionId: String,
    val description: String,
    val sender: String,
    val senderName: String,
    val recipient: String,
    val recipientName: String,
    val displayName: String,
    val serviceTypeName: String,
    val serviceTypeIcon: Int,
    val operationSymbol: String,
    val operationColor: Int,
    val amount: String,
    val timestamp: String,
    val status: String,
    val statusColor: Int,
    val statusBgColor: Int
) : Parcelable