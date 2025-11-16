package com.settlex.android.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.remote.dto.BackendResponseDto
import com.settlex.android.domain.model.UserModel
import com.settlex.android.util.event.UiState

interface AuthRepository {

    fun signOut()
    fun getCurrentUser(): FirebaseUser?
    suspend fun login(email: String, password: String): UiState<Unit>
    suspend fun register(user: UserModel, password: String): UiState<Unit>
    suspend fun sendOtp(email: String, type: OtpType): UiState<BackendResponseDto>
    suspend fun verifyEmail(email: String, otp: String): UiState<BackendResponseDto>
    suspend fun verifyPasswordReset(email: String, otp: String): UiState<BackendResponseDto>
    suspend fun setNewPassword(email: String, oldPassword: String, newPassword: String): UiState<BackendResponseDto>
    suspend fun getFcmToken(): UiState<String>
    suspend fun storeFcmToken(token: String): UiState<Unit>
}
