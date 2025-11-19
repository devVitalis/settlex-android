package com.settlex.android.domain.usecase

import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.domain.repository.AuthRepositoryImpl
import jakarta.inject.Inject

class SetNewPasswordUseCase @Inject constructor(private val authRepositoryImpl: AuthRepositoryImpl) {
    suspend operator fun invoke(
        email: String,
        oldPassword: String,
        newPassword: String
    ): Result<ApiResponse<String>> {
        return authRepositoryImpl.setNewPassword(email, oldPassword, newPassword)
    }
}