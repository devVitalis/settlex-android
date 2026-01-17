package com.settlex.android.presentation.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents the different user flows for managing a payment PIN.
 * This sealed class is used to determine which specific sequence of screens
 * or actions should be presented to the user when they interact with PIN settings.
 */
@Parcelize
sealed class PaymentPinFlow : Parcelable {
    object CreatePin : PaymentPinFlow()
    object ChangePin : PaymentPinFlow()
    object ResetPin : PaymentPinFlow()
}