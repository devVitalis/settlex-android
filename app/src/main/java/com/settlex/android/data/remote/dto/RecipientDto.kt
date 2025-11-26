package com.settlex.android.data.remote.dto

data class RecipientDto(
    val paymentId: String,
    val firstName: String,
    val lastName: String,
    val photoUrl: String? = null
)
