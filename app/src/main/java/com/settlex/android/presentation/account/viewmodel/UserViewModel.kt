package com.settlex.android.presentation.account.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.exception.AppException
import com.settlex.android.domain.session.UserSessionManager
import com.settlex.android.domain.usecase.user.UserUseCases
import com.settlex.android.presentation.account.extension.toUiModel
import com.settlex.android.presentation.account.model.UserState
import com.settlex.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userUseCase: UserUseCases,
    userSession: UserSessionManager
) : ViewModel() {

    val userState = userSession.authState
        .combine(userSession.userState) { auth, dto ->
            UiState.Success(
                UserState(
                    authUid = auth?.uid,
                    user = dto?.toUiModel()
                )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val _isPaymentIdTakenEvent = MutableSharedFlow<UiState<Boolean>>()
    val isPaymentIdTakenEvent = _isPaymentIdTakenEvent.asSharedFlow()

    fun isPaymentIdTaken(paymentId: String) {
        viewModelScope.launch {
            _isPaymentIdTakenEvent.emit(UiState.Loading)

            userUseCase.isPaymentIdTaken(paymentId).fold(
                onSuccess = { _isPaymentIdTakenEvent.emit(UiState.Success(it)) },
                onFailure = { _isPaymentIdTakenEvent.emit(UiState.Failure(it as AppException)) }
            )
        }
    }

    private val _assignPaymentIdEven = MutableSharedFlow<UiState<Unit>>()
    val assignPaymentIdEvent = _assignPaymentIdEven.asSharedFlow()

    fun assignPaymentId(paymentId: String, uid: String) {
        viewModelScope.launch {
            userUseCase.assignPaymentId(paymentId, uid).fold(
                onSuccess = { _assignPaymentIdEven.emit(UiState.Success(Unit)) },
                onFailure = { _assignPaymentIdEven.emit(UiState.Failure(it as AppException)) }
            )
        }
    }

    private val _setPaymentPinEvent = MutableSharedFlow<UiState<String>>()
    val setPaymentPinEvent = _setPaymentPinEvent.asSharedFlow()

    fun setPaymentPin(pin: String) {
        viewModelScope.launch {
            userUseCase.setPaymentPin(pin).fold(
                onSuccess = { _setPaymentPinEvent.emit(UiState.Success(it.data)) },
                onFailure = { _setPaymentPinEvent.emit(UiState.Failure(it as AppException)) }
            )
        }
    }
}