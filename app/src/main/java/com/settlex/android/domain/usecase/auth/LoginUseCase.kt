package com.settlex.android.domain.usecase.auth

import com.settlex.android.data.repository.AuthRepositoryImpl
import jakarta.inject.Inject

class LoginUseCase @Inject constructor(private val authRepositoryImpl: AuthRepositoryImpl) {

    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return authRepositoryImpl.login(email, password)
    }
}