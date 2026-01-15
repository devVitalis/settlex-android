package com.settlex.android.presentation.transactions.model

import android.os.Parcelable
import com.settlex.android.data.enums.TransactionStatus
import kotlinx.parcelize.Parcelize

/**
 * Immutable, type-safe representation of a transaction outcome.
 */
@Parcelize
data class TransactionResult(
    val status: TransactionStatus,
    val amount: Long, // in kobo
    val message: String,
    val errorMessage: String? = null
) : Parcelable