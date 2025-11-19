package com.settlex.android.domain.usecase

import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.domain.repository.AuthRepositoryImpl
import jakarta.inject.Inject

class SendOtpUseCase @Inject constructor(private val authRepositoryImpl: AuthRepositoryImpl) {
    suspend operator fun invoke(email: String, type: OtpType): Result<ApiResponse<String>> {
        return authRepositoryImpl.sendOtp(email, type)
    }
}