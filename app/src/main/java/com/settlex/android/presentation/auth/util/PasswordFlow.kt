package com.settlex.android.presentation.auth.util

/**
 * Represents the purpose of the screen during password reset
 */
sealed class PasswordFlow {
    data object ForgotPassword : PasswordFlow()
    data object ChangePassword : PasswordFlow()
    data object AuthenticatedPasswordReset : PasswordFlow()
}
