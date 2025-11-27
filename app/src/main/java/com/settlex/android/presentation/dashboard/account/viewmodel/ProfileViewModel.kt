package com.settlex.android.presentation.dashboard.account.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.session.UserSessionManager
import com.settlex.android.domain.usecase.user.SetPaymentPinUseCase
import com.settlex.android.domain.usecase.user.SetProfilePictureUseCase
import com.settlex.android.data.mapper.toProfileUiModel
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val setPaymentPinUseCase: SetPaymentPinUseCase,
    private val setProfilePictureUseCase: SetProfilePictureUseCase,
    userSession: UserSessionManager
) : ViewModel() {

    val userState = userSession.authState
        .combine(userSession.userState) { auth, dto ->
            UiState.Success(
                UserState(
                    authUid = auth?.uid,
                    user = dto?.toProfileUiModel()
                )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val _setProfilePictureEvent = MutableSharedFlow<UiState<String>>()
    val setProfilePictureEvent = _setProfilePictureEvent.asSharedFlow()

    fun setProfilePicture(context: Context, uri: Uri) {
        viewModelScope.launch {
            _setProfilePictureEvent.emit(UiState.Loading)

            setProfilePictureUseCase(context, uri).fold(
                onSuccess = { _setProfilePictureEvent.emit(UiState.Success(it.data)) },
                onFailure = { _setProfilePictureEvent.emit(UiState.Failure(it as AppException)) }
            )
        }
    }

    private val _setPaymentPinEvent = MutableSharedFlow<UiState<String>>()
    val setPaymentPinEvent = _setPaymentPinEvent.asSharedFlow()

    fun setPaymentPin(pin: String) {
        viewModelScope.launch {
            setPaymentPinUseCase(pin).fold(
                onSuccess = { _setPaymentPinEvent.emit(UiState.Success(it.data)) },
                onFailure = { _setPaymentPinEvent.emit(UiState.Failure(it as AppException)) }
            )
        }
    }
}