package com.settlex.android.data.mapper

import com.settlex.android.R
import com.settlex.android.data.enums.TransactionOperation
import com.settlex.android.data.enums.TransactionStatus
import com.settlex.android.data.remote.dto.RecipientDto
import com.settlex.android.data.remote.dto.TransactionDto
import com.settlex.android.data.remote.dto.UserDto
import com.settlex.android.presentation.common.extensions.addAtPrefix
import com.settlex.android.presentation.common.extensions.toNairaString
import com.settlex.android.presentation.dashboard.account.model.ProfileUiModel
import com.settlex.android.presentation.dashboard.home.model.HomeUiModel
import com.settlex.android.presentation.dashboard.rewards.RewardsUiModel
import com.settlex.android.presentation.transactions.model.RecipientUiModel
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel
import com.settlex.android.presentation.transactions.model.TransferToFriendUiModel
import com.settlex.android.presentation.wallet.model.WalletUiModel

fun UserDto.toProfileUiModel(): ProfileUiModel {
    return ProfileUiModel(
        email = email,
        firstName = firstName,
        lastName = lastName,
        joinedDate = createdAt!!,
        phone = phone,
        paymentId = paymentId,
        photoUrl = photoUrl,
    )
}

fun UserDto.toHomeUiModel(): HomeUiModel {
    return HomeUiModel(
        uid = uid,
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

fun UserDto.toWalletUiModel(): WalletUiModel {
    return WalletUiModel(
        paymentId = paymentId
    )
}

fun RecipientDto.toRecipientUiModel(): RecipientUiModel {
    return RecipientUiModel(
        paymentId = paymentId,
        fullName = "$firstName $lastName",
        photoUrl = photoUrl
    )
}

fun TransactionDto.toTransactionUiModel(uid: String): TransactionItemUiModel {
    val isSender = uid == senderUid

    val operation = when (status) {
        TransactionStatus.REVERSED -> if (isSender) TransactionOperation.CREDIT else TransactionOperation.DEBIT
        else -> if (isSender) TransactionOperation.DEBIT else TransactionOperation.CREDIT
    }

    return TransactionItemUiModel(
        transactionId = transactionId,
        description = description,
        senderId = sender.addAtPrefix(),
        senderName = senderName.uppercase(),
        recipientId = recipient.addAtPrefix(),
        recipientName = recipientName.uppercase(),
        recipientOrSenderName = if (isSender) recipientName.uppercase() else senderName.uppercase(),
        serviceTypeName = if (isSender) serviceType.displayName else "Payment Received",
        serviceTypeIcon = if (isSender) serviceType.iconRes else R.drawable.ic_service_payment_received,
        operationSymbol = operation.symbol,
        operationColor = operation.colorRes,
        amount = amount.toNairaString(),
        timestamp = createdAt,
        status = status.displayName,
        statusColor = status.colorRes,
        statusBackgroundColor = status.bgColorRes
    )
}