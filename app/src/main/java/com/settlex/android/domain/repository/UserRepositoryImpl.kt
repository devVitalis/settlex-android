package com.settlex.android.domain.repository

import android.content.Context
import android.net.Uri
import com.settlex.android.data.datasource.UserRemoteDataSource
import com.settlex.android.data.exception.ApiException
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.repository.UserRepository
import jakarta.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
    private val exception: ApiException
) : UserRepository {

    override suspend fun isPaymentIdTaken(id: String): Result<Boolean> {
        return try {
            val available = remoteDataSource.isPaymentIdTaken(id)
            Result.success(available)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }
    }

    override suspend fun assignPaymentId(id: String, uid: String): Result<Unit> {
        return try {
            remoteDataSource.assignPaymentId(id, uid)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }
    }

    override suspend fun setPaymentPin(pin: String): Result<ApiResponse<String>> {
        return try {
            val response = remoteDataSource.setPaymentPin(pin)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }
    }

    override suspend fun authPaymentPin(pin: String): Result<ApiResponse<Boolean>> {
        return try {
            val authenticated = remoteDataSource.authPaymentPin(pin)
            Result.success(authenticated)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }
    }

    override suspend fun resetPaymentPin(
        oldPin: String,
        newPin: String
    ): Result<ApiResponse<String>> {
        return try {
            val response = remoteDataSource.resetPaymentPin(oldPin, newPin)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }
    }

    override suspend fun resetPassword(
        oldPwd: String,
        newPwd: String
    ): Result<Unit> {
        return runCatching {
            remoteDataSource.resetPassword(oldPwd, newPwd)
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(exception.map(it as Exception)) }
        )
    }

    override suspend fun setProfilePicture(
        context: Context,
        uri: Uri
    ): Result<ApiResponse<String>> {
        return runCatching { remoteDataSource.setProfilePicture(context, uri) }
            .fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(exception.map(it as Exception)) }
            )
    }

    override suspend fun refreshUser() {
        runCatching { remoteDataSource.refreshUser() }
    }
}