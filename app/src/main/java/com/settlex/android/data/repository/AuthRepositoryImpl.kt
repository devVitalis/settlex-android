package com.settlex.android.data.repository

import com.google.firebase.auth.FirebaseUser
import com.settlex.android.data.datasource.AuthRemoteDataSource
import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.domain.model.UserModel
import com.settlex.android.domain.repository.AuthRepository
import jakarta.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val remote: AuthRemoteDataSource) : AuthRepository {

    override fun signOut() = remote.signOut()

    override fun getCurrentUser(): FirebaseUser? = remote.getCurrentUser()

    override suspend fun login(email: String, password: String): Result<Unit> =
        try {
            remote.login(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun register(user: UserModel, password: String): Result<Unit> =
        try {
            remote.register(user, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun sendOtp(email: String, type: OtpType): Result<ApiResponse<String>> =
        try {
            val response = remote.sendOtp(email, type)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun verifyEmail(email: String, otp: String): Result<ApiResponse<String>> =
        try {
            val response = remote.verifyEmail(email, otp)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun verifyPasswordReset(
        email: String,
        otp: String
    ): Result<ApiResponse<String>> =
        try {
            val response = remote.verifyPasswordReset(email, otp)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun setNewPassword(
        email: String,
        oldPassword: String,
        newPassword: String
    ): Result<ApiResponse<String>> =
        try {
            val response = remote.setNewPassword(email, oldPassword, newPassword)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getFcmToken(): Result<String> =
        try {
            val token = remote.getFcmToken()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun storeFcmToken(token: String): Result<Unit> =
        try {
            remote.storeFcmToken(token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
