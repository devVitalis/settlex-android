package com.settlex.android.domain.usecase.txn

import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.domain.repository.TransactionsRepository
import jakarta.inject.Inject

class TransferToFriendUseCase @Inject constructor(private val txnRepo: TransactionsRepository) {
    suspend operator fun invoke(
        fromUid: String,
        toPaymentId: String,
        txnId: String,
        amount: Long,
        desc: String
    ): Result<ApiResponse<String>> {
        return txnRepo.transferToFriend(fromUid, toPaymentId, txnId, amount, desc)
    }
}