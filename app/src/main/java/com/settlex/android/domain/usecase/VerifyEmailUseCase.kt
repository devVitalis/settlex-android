package com.settlex.android.domain.usecase

import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.repository.AuthRepositoryImpl
import jakarta.inject.Inject

class VerifyEmailUseCase @Inject constructor(private val authRepositoryImpl: AuthRepositoryImpl) {
    suspend operator fun invoke(email: String, otp: String): Result<ApiResponse<String>> {
        return authRepositoryImpl.verifyEmail(email, otp)
    }
}