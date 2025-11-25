package com.settlex.android.domain.usecase.user

import com.settlex.android.data.repository.UserRepositoryImpl
import jakarta.inject.Inject

class IsPaymentIdTakenUseCase @Inject constructor(private val userRepoImpl: UserRepositoryImpl) {
    suspend operator fun invoke(id: String): Result<Boolean> {
        return userRepoImpl.isPaymentIdTaken(id)
    }
}