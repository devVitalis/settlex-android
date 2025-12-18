package com.settlex.android.presentation.dashboard.services.model

import com.settlex.android.data.enums.TransactionServiceType

/**
 * Data model representing a service item with display name and icon resource
 */
data class ServiceUiModel(
    val name: String,
    val iconResId: Int,
    val cashbackPercentage: Int = 0,
    val label: String?,
    val transactionServiceType: TransactionServiceType,
    val destination: ServiceDestination?
)