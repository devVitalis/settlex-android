package com.settlex.android.domain.usecase

import com.settlex.android.data.remote.dto.BackendResponseDto
import com.settlex.android.data.repository.AuthRepositoryImpl
import com.settlex.android.util.event.UiState
import jakarta.inject.Inject

class SetNewPasswordUseCase @Inject constructor(private val authRepositoryImpl: AuthRepositoryImpl) {
    suspend operator fun invoke(
        email: String,
        oldPassword: String,
        newPassword: String
    ): UiState<BackendResponseDto> {
        return authRepositoryImpl.setNewPassword(email, oldPassword, newPassword)
    }
}