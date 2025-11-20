package com.settlex.android.domain.usecase.auth

import jakarta.inject.Inject

/**
 * This class holds references to all auth use cases
 */
data class AuthUseCases @Inject constructor(
    val login: LoginUseCase,
    val register: RegisterUseCase,
    val sendOtp: SendOtpUseCase,
    val verifyEmail: VerifyEmailUseCase,
    val verifyPasswordReset: VerifyPasswordResetUseCase,
    val setNewPassword: SetNewPasswordUseCase,
    val getCurrentUser: GetCurrentUserUseCase
)