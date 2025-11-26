package com.settlex.android.domain.usecase.user

import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.repository.UserRepositoryImpl
import jakarta.inject.Inject

class AuthPaymentPinUseCase @Inject constructor(private val userRepoImpl: UserRepositoryImpl) {
    suspend operator fun invoke(pin: String): Result<ApiResponse<Boolean>> {
        return userRepoImpl.authPaymentPin(pin)
    }
}