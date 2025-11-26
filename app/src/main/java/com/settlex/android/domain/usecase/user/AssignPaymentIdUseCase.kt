package com.settlex.android.domain.usecase.user

import com.settlex.android.data.repository.UserRepositoryImpl
import jakarta.inject.Inject

class AssignPaymentIdUseCase @Inject constructor(private val userRepoImpl: UserRepositoryImpl) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return userRepoImpl.assignPaymentId(id)
    }
}