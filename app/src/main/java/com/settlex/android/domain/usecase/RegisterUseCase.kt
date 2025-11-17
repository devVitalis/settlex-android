package com.settlex.android.domain.usecase

import com.settlex.android.data.repository.AuthRepositoryImpl
import com.settlex.android.domain.model.UserModel
import jakarta.inject.Inject

class RegisterUseCase @Inject constructor(private val authRepositoryImpl: AuthRepositoryImpl) {
    suspend operator fun invoke(user: UserModel, password: String): Result<Unit> {
        return authRepositoryImpl.register(user, password)
    }
}