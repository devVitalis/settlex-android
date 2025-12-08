package com.settlex.android.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import com.settlex.android.data.datasource.UserRemoteDataSource
import com.settlex.android.data.exception.ApiException
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.remote.dto.RecipientDto
import com.settlex.android.domain.repository.UserRepository
import jakarta.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val remote: UserRemoteDataSource,
    private val exception: ApiException
) : UserRepository {

    override fun getCurrentUser(): FirebaseUser? {
        return remote.getCurrentUser()
    }

    override suspend fun isPaymentIdTaken(id: String): Result<Boolean> {
        return runCatching {
            remote.isPaymentIdTaken(id)
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(exception.map(it as Exception)) }
        )
    }

    override suspend fun assignPaymentId(id: String): Result<Unit> {
        return runCatching {
            remote.assignPaymentId(id)
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(exception.map(it as Exception)) }
        )
    }

    override suspend fun setPaymentPin(pin: String): Result<ApiResponse<String>> {
        return runCatching {
            remote.setPaymentPin(pin)
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(exception.map(it as Exception)) }
        )
    }

    override suspend fun authPaymentPin(pin: String): Result<ApiResponse<Boolean>> {
        return runCatching {
            remote.authPaymentPin(pin)
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(exception.map(it as Exception)) }
        )
    }

    override suspend fun resetPaymentPin(
        oldPin: String,
        newPin: String
    ): Result<ApiResponse<String>> {
        return runCatching {
            remote.resetPaymentPin(oldPin, newPin)
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(exception.map(it as Exception)) }
        )
    }

    override suspend fun resetPassword(
        oldPwd: String,
        newPwd: String
    ): Result<Unit> {
        return runCatching {
            remote.resetPassword(oldPwd, newPwd)
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(exception.map(it as Exception)) }
        )
    }

    override suspend fun setProfilePicture(
        context: Context,
        uri: Uri
    ): Result<ApiResponse<String>> {
        return runCatching { remote.setProfilePicture(context, uri) }
            .fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(exception.map(it as Exception)) }
            )
    }

    override suspend fun refreshUser() {
        runCatching { remote.refreshUser() }
    }

    override suspend fun getRecipientByPaymentId(paymentId: String): Result<ApiResponse<List<RecipientDto>>> {
        runCatching {
            remote.getRecipientByPaymentId(paymentId)
        }.fold(
            onSuccess = { return Result.success(it) },
            onFailure = { return Result.failure(exception.map(it as Exception)) }
        )
    }
}