package com.settlex.android.domain.usecase.user

import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.remote.dto.RecipientDto
import com.settlex.android.data.repository.UserRepositoryImpl
import jakarta.inject.Inject

class GetReceipientUseCase @Inject constructor(
    private val userRepositoryImpl: UserRepositoryImpl
) {
    suspend operator fun invoke(paymentId: String): Result<ApiResponse<List<RecipientDto>>> {
        return userRepositoryImpl.getRecipientByPaymentId(paymentId)
    }
}