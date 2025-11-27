package com.settlex.android.data.mapper

import com.settlex.android.data.remote.dto.UserDto
import com.settlex.android.presentation.dashboard.account.model.ProfileUiModel
import com.settlex.android.presentation.dashboard.home.model.HomeUiModel
import com.settlex.android.presentation.dashboard.rewards.RewardsUiModel
import com.settlex.android.presentation.transactions.model.TransferToFriendUiModel

fun UserDto.toProfileUiModel(): ProfileUiModel {
    return ProfileUiModel(
        email = email,
        firstName = firstName,
        lastName = lastName,
        createdAt = createdAt,
        phone = phone,
        paymentId = paymentId,
        photoUrl = photoUrl,
    )
}

fun UserDto.toHomeUiModel(): HomeUiModel {
    return HomeUiModel(
        firstName = firstName,
        lastName = lastName,
        photoUrl = photoUrl,
        hasPin = hasPin,
        balance = balance,
        commissionBalance = commissionBalance,
        paymentId = paymentId
    )
}

fun UserDto.toRewardsUiModel(): RewardsUiModel {
    return RewardsUiModel(
        commissionBalance = commissionBalance,
        referralBalance = referralBalance,
        paymentId = paymentId
    )
}

fun UserDto.toTransferToFriendUiModel(): TransferToFriendUiModel {
    return TransferToFriendUiModel(
        uid = uid,
        firstName = firstName,
        lastName = lastName,
        createdAt = createdAt,
        paymentId = paymentId,
        hasPin = hasPin,
        balance = balance,
        commissionBalance = commissionBalance,
        totalBalance = balance + commissionBalance
    )
}