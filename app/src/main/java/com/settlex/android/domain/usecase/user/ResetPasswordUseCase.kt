package com.settlex.android.domain.usecase.user

import com.settlex.android.data.repository.UserRepositoryImpl
import jakarta.inject.Inject

class ResetPasswordUseCase @Inject constructor(private val userRepoImpl: UserRepositoryImpl) {
    suspend operator fun invoke(oldPwd: String, newPwd: String): Result<Unit> {
        return userRepoImpl.resetPassword(oldPwd, newPwd)
    }
}