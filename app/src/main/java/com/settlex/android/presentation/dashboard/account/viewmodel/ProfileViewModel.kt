package com.settlex.android.presentation.dashboard.account.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.mapper.toProfileUiModel
import com.settlex.android.data.session.UserSessionManager
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.domain.usecase.user.SetPaymentPinUseCase
import com.settlex.android.domain.usecase.user.SetProfilePictureUseCase
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.account.model.ProfileUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val setPaymentPinUseCase: SetPaymentPinUseCase,
    private val setProfilePhotoUseCase: SetProfilePictureUseCase,
    sessionManager: UserSessionManager
) : ViewModel() {
    val userSessionState: StateFlow<UserSessionState<ProfileUiModel>> =
        sessionManager.userSession.map { userSessionState ->
            when (userSessionState) {
                is UserSessionState.Loading -> UserSessionState.Loading
                is UserSessionState.UnAuthenticated -> UserSessionState.UnAuthenticated
                is UserSessionState.Error -> UserSessionState.Error(userSessionState.exception)
                is UserSessionState.Authenticated -> UserSessionState.Authenticated(userSessionState.user.toProfileUiModel())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserSessionState.Loading
        )

    private val _setProfilePictureEvent = Channel<UiState<String>>(Channel.BUFFERED)
    val setProfilePictureEvent = _setProfilePictureEvent.receiveAsFlow()

    fun setProfilePhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _setProfilePictureEvent.send(UiState.Loading)

            setProfilePhotoUseCase(context, uri).fold(
                onSuccess = { _setProfilePictureEvent.send(UiState.Success(it.data)) },
                onFailure = { _setProfilePictureEvent.send(UiState.Failure(it as AppException)) }
            )
        }
    }

    private val _setPaymentPinEvent = Channel<UiState<String>>(Channel.BUFFERED)
    val setPaymentPinEvent = _setPaymentPinEvent.receiveAsFlow()

    fun setPaymentPin(pin: String) {
        viewModelScope.launch {
            setPaymentPinUseCase(pin).fold(
                onSuccess = { _setPaymentPinEvent.send(UiState.Success(it.data)) },
                onFailure = { _setPaymentPinEvent.send(UiState.Failure(it as AppException)) }
            )
        }
    }
}
