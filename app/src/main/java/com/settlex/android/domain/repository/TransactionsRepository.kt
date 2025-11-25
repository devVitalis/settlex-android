package com.settlex.android.domain.repository

import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.remote.dto.TransactionDto
import kotlinx.coroutines.flow.Flow

interface TransactionsRepository {

    suspend fun getRecentTransactions(uid: String): Flow<Result<List<TransactionDto>>>

    suspend fun transferToFriend(
        fromUid: String,
        toPaymentId: String,
        txnId: String,
        amount: Long,
        desc: String
    ): Result<ApiResponse<String>>
}