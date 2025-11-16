package com.settlex.android.domain.usecase

import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.remote.dto.BackendResponseDto
import com.settlex.android.data.repository.AuthRepositoryImpl
import com.settlex.android.util.event.UiState
import jakarta.inject.Inject

class SendOtpUseCase @Inject constructor(private val authRepositoryImpl: AuthRepositoryImpl) {
    suspend operator fun invoke(email: String, type: OtpType): UiState<BackendResponseDto> {
        return authRepositoryImpl.sendOtp(email, type)
    }
}