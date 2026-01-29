package com.settlex.android.data.repository

import com.settlex.android.data.datasource.UserRemoteDataSource
import com.settlex.android.data.exception.ExceptionMapper
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.remote.dto.TransactionDto
import com.settlex.android.domain.repository.TransactionsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl @Inject constructor(
    private val remote: UserRemoteDataSource,
    private val exceptionMapper: ExceptionMapper
) : TransactionsRepository {

    override suspend fun fetchRecentTransactions(): Pair<String, Flow<Result<List<TransactionDto>>>> {
        val (uid, transactions) = remote.fetchRecentTransactions()
        return uid to transactions.map { result ->
            result.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(exceptionMapper.map(it as Exception)) }
            )
        }
    }

    override suspend fun fetchTransactionsForTheMonth(): Flow<Result<Pair<String, List<TransactionDto>>>> {
        return remote.fetchTransactionsForTheMonth().map { result ->
            result.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(exceptionMapper.map(it as Exception)) }
            )
        }
    }

    override suspend fun transferToFriend(
        toRecipientPaymentId: String,
        transferAmount: Long,
        description: String?
    ): Result<ApiResponse<String>> {
        runCatching {
            remote.transferToFriend(
                toRecipientPaymentId,
                transferAmount,
                description
            )
        }.fold(
            onSuccess = { return Result.success(it) },
            onFailure = { return Result.failure(exceptionMapper.map(it as Exception)) }
        )
    }
}