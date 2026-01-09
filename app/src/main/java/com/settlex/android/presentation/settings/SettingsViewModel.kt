package com.settlex.android.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.exception.AppException
import com.settlex.android.domain.usecase.user.AssignPaymentIdUseCase
import com.settlex.android.domain.usecase.user.IsPaymentIdTakenUseCase
import com.settlex.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val isPaymentIdTakenUseCase: IsPaymentIdTakenUseCase,
    private val assignPaymentIdUseCase: AssignPaymentIdUseCase,
) : ViewModel() {

    private val _isPaymentIdTakenEvent = MutableSharedFlow<UiState<Boolean>>()
    val isPaymentIdTakenEvent = _isPaymentIdTakenEvent.asSharedFlow()

    fun isPaymentIdTaken(paymentId: String) {
        viewModelScope.launch {
            _isPaymentIdTakenEvent.emit(UiState.Loading)

            isPaymentIdTakenUseCase(paymentId).fold(
                onSuccess = { _isPaymentIdTakenEvent.emit(UiState.Success(it)) },
                onFailure = { _isPaymentIdTakenEvent.emit(UiState.Failure(it as AppException)) }
            )
        }
    }

    private val _assignPaymentIdEven = Channel<UiState<Unit>>(Channel.BUFFERED)
    val assignPaymentIdEvent = _assignPaymentIdEven.receiveAsFlow()

    fun assignPaymentId(paymentId: String) {
        viewModelScope.launch {
            _assignPaymentIdEven.send(UiState.Loading)

            assignPaymentIdUseCase(paymentId).fold(
                onSuccess = { _assignPaymentIdEven.send(UiState.Success(Unit)) },
                onFailure = { _assignPaymentIdEven.send(UiState.Failure(it as AppException)) }
            )
        }
    }
}