package com.settlex.android.domain.usecase.transaction

import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.repository.TransactionRepositoryImpl
import jakarta.inject.Inject

class TransferToFriendUseCase @Inject constructor(
    private val transactionRepo: TransactionRepositoryImpl
) {
    suspend operator fun invoke(
        fromUid: String,
        toPaymentId: String,
        txnId: String,
        amount: Long,
        desc: String?
    ): Result<ApiResponse<String>> {
        return transactionRepo.transferToFriend(fromUid, toPaymentId, txnId, amount, desc)
    }
}