package com.settlex.android.presentation.dashboard

data class UserState<T>(
    val authUid: String? = null,
    val user: T
)