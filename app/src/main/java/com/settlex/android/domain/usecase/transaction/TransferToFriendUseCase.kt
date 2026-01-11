package com.settlex.android.domain.usecase.transaction

import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.repository.TransactionRepositoryImpl
import jakarta.inject.Inject

class TransferToFriendUseCase @Inject constructor(
    private val transactionRepo: TransactionRepositoryImpl
) {
    suspend operator fun invoke(
        fromSenderUid: String,
        toRecipientPaymentId: String,
        transferAmount: Long,
        description: String?
    ): Result<ApiResponse<String>> {
        return transactionRepo.transferToFriend(
            fromSenderUid,
            toRecipientPaymentId,
            transferAmount,
            description
        )
    }
}