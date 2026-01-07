package com.settlex.android.presentation.auth.login

data class LoginUiModel(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?
)