package com.settlex.android.domain.repository

import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.remote.dto.TransactionDto
import kotlinx.coroutines.flow.Flow

interface TransactionsRepository {
    suspend fun fetchRecentTransactions(): Pair<String, Flow<Result<List<TransactionDto>>>>
    suspend fun fetchTransactionsForTheMonth(): Flow<Result<Pair<String, List<TransactionDto>>>>
    suspend fun transferToFriend(
        toRecipientPaymentId: String,
        transferAmount: Long,
        description: String?
    ): Result<ApiResponse<String>>
}