package com.settlex.android.presentation.dashboard.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.session.UserSessionManager
import com.settlex.android.presentation.dashboard.UserState
import com.settlex.android.data.mapper.toRewardsUiModel
import com.settlex.android.presentation.common.state.UiState
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class RewardsViewModel @Inject constructor(
    userSession: UserSessionManager
) : ViewModel() {

    val userState = userSession.authState
        .combine(userSession.userState) { auth, dto ->
            UiState.Success(
                UserState(
                    authUid = auth?.uid,
                    user = dto?.toRewardsUiModel()
                )
            )
        }.stateIn(
            scope = viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UiState.Loading
        )
}