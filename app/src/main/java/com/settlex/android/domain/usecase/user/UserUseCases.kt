package com.settlex.android.domain.usecase.user

import jakarta.inject.Inject

/**
 * This class holds references to all user use cases
 */
class UserUseCases @Inject constructor(
    val assignPaymentId: AssignPaymentIdUseCase,
    val authPaymentPin: authPaymentPinUseCase,
    val isPaymentIdTaken: IsPaymentIdTakenUseCase,
    val resetPassword: ResetPasswordUseCase,
    val resetPaymentPin: ResetPaymentPinUseCase,
    val setPaymentPin: SetPaymentPinUseCase,
    val setProfilePicture: SetProfilePictureUseCase
)