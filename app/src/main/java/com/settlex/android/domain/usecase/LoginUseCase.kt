package com.settlex.android.domain.usecase

import com.settlex.android.data.repository.AuthRepositoryImpl
import com.settlex.android.util.event.UiState
import jakarta.inject.Inject

class LoginUseCase @Inject constructor(private val authRepositoryImpl: AuthRepositoryImpl) {

    suspend operator fun invoke(email: String, password: String): UiState<Unit> {
        return authRepositoryImpl.login(email, password)
    }
}