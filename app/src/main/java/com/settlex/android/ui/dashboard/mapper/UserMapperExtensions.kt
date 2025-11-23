package com.settlex.android.ui.dashboard.mapper

import com.settlex.android.data.remote.dto.UserDto
import com.settlex.android.ui.dashboard.model.UserUiModel

fun UserDto.toUiModel(): UserUiModel {
    return UserUiModel(
        uid = this.uid,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        createdAt = this.createdAt,
        phone = this.phone,
        paymentId = paymentId,
        photoUrl = photoUrl,
        hasPin = hasPin,
        balance = balance,
        commissionBalance = commissionBalance,
        referralBalance = referralBalance
    )
}