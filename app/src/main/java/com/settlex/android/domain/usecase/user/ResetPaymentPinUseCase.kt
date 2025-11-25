package com.settlex.android.domain.usecase.user

import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.repository.UserRepositoryImpl
import jakarta.inject.Inject

class ResetPaymentPinUseCase @Inject constructor(private val userRepoImpl: UserRepositoryImpl) {
    suspend operator fun invoke(oldPin: String, newPin: String): Result<ApiResponse<String>> {
        return userRepoImpl.resetPaymentPin(oldPin, newPin)
    }
}