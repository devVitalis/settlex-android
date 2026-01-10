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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val isPaymentIdTakenUseCase: IsPaymentIdTakenUseCase,
    private val assignPaymentIdUseCase: AssignPaymentIdUseCase,
) : ViewModel() {

    private val _isPaymentIdTakenEvent = Channel<UiState<Boolean>>(Channel.BUFFERED)
    val isPaymentIdTakenEvent = _isPaymentIdTakenEvent.receiveAsFlow()

    fun isPaymentIdTaken(paymentId: String) {
        viewModelScope.launch {
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
            _assignPaymentIdEvent.send(UiState.Loading)

            assignPaymentIdUseCase(paymentId).fold(
                onSuccess = { _assignPaymentIdEvent.send(UiState.Success(Unit)) },
                onFailure = { _assignPaymentIdEvent.send(UiState.Failure(it as AppException)) }
            )
        }
    }
}