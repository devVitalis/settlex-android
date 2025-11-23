package com.settlex.android.ui.dashboard

import com.settlex.android.ui.dashboard.model.UserUiModel

data class UserUiState(
    val authUid: String? = null,
    val user: UserUiModel? = null
)