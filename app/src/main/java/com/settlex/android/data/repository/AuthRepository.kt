package com.settlex.android.data.repository

import com.google.firebase.auth.FirebaseUser
import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.domain.model.UserModel

interface AuthRepository {

    fun signOut()
    fun getCurrentUser(): FirebaseUser?
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(user: UserModel, password: String): Result<Unit>
    suspend fun sendOtp(email: String, type: OtpType): Result<ApiResponse<String>>
    suspend fun verifyEmail(email: String, otp: String): Result<ApiResponse<String>>
    suspend fun verifyPasswordReset(email: String, otp: String): Result<ApiResponse<String>>
    suspend fun setNewPassword(email: String, oldPassword: String, newPassword: String): Result<ApiResponse<String>>
    suspend fun getFcmToken(): Result<String>
    suspend fun storeFcmToken(token: String): Result<Unit>
}