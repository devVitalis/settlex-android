package com.settlex.android.presentation.dashboard.rewards

import androidx.lifecycle.ViewModel
import com.settlex.android.data.session.UserSessionManager
import jakarta.inject.Inject

class RewardsViewModel @Inject constructor(
    userSession: UserSessionManager
) : ViewModel() {

//    val userSessionState = userSession.authState
//        .combine(userSession.userState) { auth, dto ->
//            UiState.Success(
//                UserSessionState(
//                    authUid = auth?.uid,
//                    user = dto?.toRewardsUiModel()
//                )
//            )
//        }.stateIn(
//            scope = viewModelScope,
//            SharingStarted.WhileSubscribed(5000),
//            UiState.Loading
//        )
}