package com.settlex.android.domain.usecase.auth

import com.google.firebase.auth.FirebaseUser
import com.settlex.android.data.repository.AuthRepositoryImpl
import jakarta.inject.Inject

class GetCurrentUserUseCase @Inject constructor(private val authRepo: AuthRepositoryImpl) {

    operator fun invoke(): FirebaseUser? = authRepo.getCurrentUser()
}