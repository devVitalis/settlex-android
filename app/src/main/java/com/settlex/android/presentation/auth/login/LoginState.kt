package com.settlex.android.presentation.auth.login

/**
 * Represents the different states of user authentication for the login screen.
 */
sealed class LoginState {
    object Unauthenticated : LoginState()
    data class Authenticated(val user: LoginUiModel) : LoginState()
}