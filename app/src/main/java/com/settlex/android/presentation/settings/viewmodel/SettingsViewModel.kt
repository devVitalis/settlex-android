package com.settlex.android.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.exception.ExceptionMapper
import com.settlex.android.domain.usecase.user.AssignPaymentIdUseCase
import com.settlex.android.domain.usecase.user.IsPaymentIdTakenUseCase
import com.settlex.android.domain.usecase.user.SetPaymentPinUseCase
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.util.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val isPaymentIdTakenUseCase: IsPaymentIdTakenUseCase,
    private val assignPaymentIdUseCase: AssignPaymentIdUseCase,
    private val setPaymentPinUseCase: SetPaymentPinUseCase,
) : ViewModel() {

    private val _isPaymentIdTakenEvent = Channel<UiState<Boolean>>(Channel.BUFFERED)
    val isPaymentIdTakenEvent = _isPaymentIdTakenEvent.receiveAsFlow()

    fun isPaymentIdTaken(paymentId: String) {
        viewModelScope.launch {
            if (!isInternetConnected()) {
                _isPaymentIdTakenEvent.send(sendNetworkException())
                return@launch
            }

            _isPaymentIdTakenEvent.send(UiState.Loading)

            isPaymentIdTakenUseCase(paymentId).fold(
                onSuccess = { _isPaymentIdTakenEvent.send(UiState.Success(it)) },
                onFailure = { _isPaymentIdTakenEvent.send(UiState.Failure(it as AppException)) }
            )
        }
    }

    private val _assignPaymentIdEvent = Channel<UiState<Unit>>(Channel.BUFFERED)
    val assignPaymentIdEvent = _assignPaymentIdEvent.receiveAsFlow()

    fun assignPaymentId(paymentId: String) {
        viewModelScope.launch {
            if (!isInternetConnected()) {
                _assignPaymentIdEvent.send(sendNetworkException())
                return@launch
            }

            _assignPaymentIdEvent.send(UiState.Loading)

            assignPaymentIdUseCase(paymentId).fold(
                onSuccess = { _assignPaymentIdEvent.send(UiState.Success(Unit)) },
                onFailure = { _assignPaymentIdEvent.send(UiState.Failure(it as AppException)) }
            )
        }
    }

    private val _setPaymentPinEvent = Channel<UiState<String>>(Channel.BUFFERED)
    val setPaymentPinEvent = _setPaymentPinEvent.receiveAsFlow()

    fun setPaymentPin(pin: String) {
        viewModelScope.launch {
            if (!isInternetConnected()) {
                _setPaymentPinEvent.send(sendNetworkException())
                return@launch
            }

            _setPaymentPinEvent.send(UiState.Loading)

            setPaymentPinUseCase(pin).fold(
                onSuccess = { _setPaymentPinEvent.send(UiState.Success(it.data)) },
                onFailure = { _setPaymentPinEvent.send(UiState.Failure(it as AppException)) }
            )
        }
    }

    private fun <T> sendNetworkException(): UiState<T> {
        return UiState.Failure(
            AppException.NetworkException(
                message = ExceptionMapper.ERROR_NO_NETWORK
            )
        )
    }

    private fun isInternetConnected(): Boolean {
        return NetworkMonitor.networkStatus.value
    }
}