package com.settlex.android.presentation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.mapper.toHomeUiModel
import com.settlex.android.data.mapper.toWalletUiModel
import com.settlex.android.data.session.UserSessionManager
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.presentation.dashboard.home.model.HomeUiModel
import com.settlex.android.presentation.wallet.model.WalletUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class WalletViewModel @Inject constructor(
    sessionManager: UserSessionManager,
) : ViewModel() {

    val userSessionState: StateFlow<UserSessionState<WalletUiModel>> =
        sessionManager.userSession.map { userSessionState ->
            when (userSessionState) {
                is UserSessionState.Loading -> UserSessionState.Loading
                is UserSessionState.UnAuthenticated -> UserSessionState.UnAuthenticated
                is UserSessionState.Error -> UserSessionState.Error(userSessionState.exception)
                is UserSessionState.Authenticated -> UserSessionState.Authenticated(userSessionState.user.toWalletUiModel())

            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserSessionState.Loading
        )
}