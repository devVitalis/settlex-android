package com.settlex.android.data.repository

import com.google.firebase.auth.FirebaseUser
import com.settlex.android.data.datasource.AuthRemoteDataSource
import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.remote.dto.BackendResponseDto
import com.settlex.android.domain.model.UserModel
import com.settlex.android.domain.repository.AuthRepository
import com.settlex.android.util.event.UiState
import jakarta.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val remote: AuthRemoteDataSource) :
    AuthRepository {

    override fun signOut() = remote.signOut()

    override fun getCurrentUser(): FirebaseUser? = remote.getCurrentUser()

    override suspend fun login(email: String, password: String): UiState<Unit> =
        try {
            remote.login(email, password)
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Failure(e.message)
        }

    override suspend fun register(user: UserModel, password: String): UiState<Unit> =
        try {
            remote.register(user, password)
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Failure(e.message)
        }

    override suspend fun sendOtp(email: String, type: OtpType): UiState<BackendResponseDto> =
        try {
            val response = remote.sendOtp(email, type)
            UiState.Success(response)
        } catch (e: Exception) {
            UiState.Failure(e.message)
        }

    override suspend fun verifyEmail(email: String, otp: String): UiState<BackendResponseDto> =
        try {
            val response = remote.verifyEmail(email, otp)
            UiState.Success(response)
        } catch (e: Exception) {
            UiState.Failure(e.message)
        }

    override suspend fun verifyPasswordReset(
        email: String,
        otp: String
    ): UiState<BackendResponseDto> =
        try {
            val response = remote.verifyPasswordReset(email, otp)
            UiState.Success(response)
        } catch (e: Exception) {
            UiState.Failure(e.message)
        }

    override suspend fun setNewPassword(
        email: String,
        oldPassword: String,
        newPassword: String
    ): UiState<BackendResponseDto> =
        try {
            val response = remote.setNewPassword(email, oldPassword, newPassword)
            UiState.Success(response)
        } catch (e: Exception) {
            UiState.Failure(e.message)
        }

    override suspend fun getFcmToken(): UiState<String> =
        try {
            val token = remote.getFcmToken()
            UiState.Success(token)
        } catch (e: Exception) {
            UiState.Failure(e.message)
        }

    override suspend fun storeFcmToken(token: String): UiState<Unit> =
        try {
            remote.storeFcmToken(token)
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Failure(e.message)
        }
}
