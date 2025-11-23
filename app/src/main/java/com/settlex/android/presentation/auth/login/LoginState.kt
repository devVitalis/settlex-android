package com.settlex.android.presentation.auth.login

/**
 * Represents the different states of user authentication for the login screen.
 */
sealed class LoginState {

    object NoUser : LoginState()

    data class CurrentUser(
        val uid: String,
        val email: String,
        val displayName: String,
        val photoUrl: String?
    ) : LoginState()
}