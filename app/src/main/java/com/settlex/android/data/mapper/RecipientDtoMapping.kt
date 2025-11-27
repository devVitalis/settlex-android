package com.settlex.android.data.mapper

import com.settlex.android.data.remote.dto.RecipientDto
import com.settlex.android.presentation.transactions.model.RecipientUiModel

fun RecipientDto.toRecipientUiModel(): RecipientUiModel {
    return RecipientUiModel(
        paymentId = paymentId,
        fullName = "$firstName + $lastName",
        photoUrl = photoUrl
    )
}