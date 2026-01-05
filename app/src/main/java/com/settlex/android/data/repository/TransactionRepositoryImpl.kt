package com.settlex.android.data.repository

import com.settlex.android.data.datasource.UserRemoteDataSource
import com.settlex.android.data.exception.ExceptionMapper
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.remote.dto.TransactionDto
import com.settlex.android.domain.repository.TransactionsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class TransactionRepositoryImpl @Inject constructor(
    private val remote: UserRemoteDataSource,
    private val exception: ExceptionMapper
) : TransactionsRepository {

    override suspend fun getRecentTransactions(uid: String): Flow<Result<List<TransactionDto>>> {
        return remote.getRecentTransactions(uid)
            .catch { exception.map(it as Exception) }
    }

    override suspend fun transferToFriend(
        fromUid: String,
        toPaymentId: String,
        txnId: String,
        amount: Long,
        desc: String?
    ): Result<ApiResponse<String>> {
        runCatching {
            remote.transferToFriend(fromUid, toPaymentId, txnId, amount, desc)
        }.fold(
            onSuccess = { return Result.success(it) },
            onFailure = { return Result.failure(exception.map(it as Exception)) }
        )
    }
}