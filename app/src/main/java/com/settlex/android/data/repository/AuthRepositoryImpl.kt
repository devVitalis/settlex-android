package com.settlex.android.data.repository

import com.google.firebase.auth.FirebaseUser
import com.settlex.android.data.datasource.AuthRemoteDataSource
import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.exception.ExceptionMapper
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.domain.model.UserModel
import com.settlex.android.domain.repository.AuthRepository
import jakarta.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthRemoteDataSource,
    private val exception: ExceptionMapper
) : AuthRepository {

    override fun signOut() = authApi.signOut()
    override fun getCurrentUser(): FirebaseUser? = authApi.getCurrentUser()

    override suspend fun login(email: String, password: String): Result<Unit> =
        try {
            authApi.login(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }

    override suspend fun register(user: UserModel, password: String): Result<Unit> =
        try {
            authApi.register(user, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }

    override suspend fun sendOtp(email: String, type: OtpType): Result<ApiResponse<String>> =
        try {
            val response = authApi.sendOtp(email, type)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }

    override suspend fun verifyEmail(email: String, otp: String): Result<ApiResponse<String>> =
        try {
            val response = authApi.verifyEmail(email, otp)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }

    override suspend fun verifyPasswordReset(
        email: String,
        otp: String
    ): Result<ApiResponse<String>> =
        try {
            val response = authApi.verifyPasswordReset(email, otp)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }

    override suspend fun setNewPassword(
        email: String,
        oldPassword: String,
        newPassword: String
    ): Result<ApiResponse<String>> =
        try {
            val response = authApi.setNewPassword(email, oldPassword, newPassword)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }

    override suspend fun getFcmToken(): Result<String> =
        try {
            val token = authApi.getFcmToken()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }

    override suspend fun setFcmToken(token: String): Result<Unit> =
        try {
            authApi.setFcmToken(token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(exception.map(e))
        }
}
