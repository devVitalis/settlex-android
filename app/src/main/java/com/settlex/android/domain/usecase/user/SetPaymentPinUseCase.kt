package com.settlex.android.domain.usecase.user

import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.repository.UserRepositoryImpl
import jakarta.inject.Inject

class SetPaymentPinUseCase @Inject constructor(private val userRepoImpl: UserRepositoryImpl) {
    suspend operator fun invoke(pin: String): Result<ApiResponse<String>> {
        return userRepoImpl.setPaymentPin(pin)
    }
}