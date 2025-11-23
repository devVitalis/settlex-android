package com.settlex.android.presentation.auth.util

/**
 * Represents the purpose of the screen
 */
sealed class PasswordFlow {
    data object Forgot : PasswordFlow()
    data object Change : PasswordFlow()
    data object AuthenticatedReset : PasswordFlow()
}
