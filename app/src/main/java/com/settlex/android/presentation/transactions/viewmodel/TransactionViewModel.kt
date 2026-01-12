package com.settlex.android.presentation.transactions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.mapper.toRecipientUiModel
import com.settlex.android.data.mapper.toTransferToFriendUiModel
import com.settlex.android.data.session.UserSessionManager
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.domain.usecase.transaction.TransferToFriendUseCase
import com.settlex.android.domain.usecase.user.AuthPaymentPinUseCase
import com.settlex.android.domain.usecase.user.GetReceipientUseCase
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.transactions.model.RecipientUiModel
import com.settlex.android.presentation.transactions.model.TransferToFriendUiModel
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
class TransactionViewModel @Inject constructor(
    private val transferToFriendUseCase: TransferToFriendUseCase,
    private val getRecipientUseCase: GetReceipientUseCase,
    private val authPaymentPinUseCase: AuthPaymentPinUseCase,
    sessionManager: UserSessionManager
) :
    ViewModel() {
    val userSessionState: StateFlow<UserSessionState<TransferToFriendUiModel>> =
        sessionManager.userSession.map { userSessionState ->
            when (userSessionState) {
                is UserSessionState.Loading -> UserSessionState.Loading
                is UserSessionState.UnAuthenticated -> UserSessionState.UnAuthenticated
                is UserSessionState.Error -> UserSessionState.Error(userSessionState.exception)
                is UserSessionState.Authenticated -> UserSessionState.Authenticated(userSessionState.user.toTransferToFriendUiModel())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserSessionState.Loading
        )

    private val _transferToFriendEvent = Channel<UiState<String>>(Channel.BUFFERED)
    val transferToFriendEvent = _transferToFriendEvent.receiveAsFlow()

    fun transferToFriend(
        toRecipientPaymentId: String,
        transferAmount: Long,
        description: String?
    ) {
        viewModelScope.launch {
            _transferToFriendEvent.send(UiState.Loading)
            transferToFriendUseCase(toRecipientPaymentId, transferAmount, description)
                .fold(
                    onSuccess = { _transferToFriendEvent.send(UiState.Success(it.data)) },
                    onFailure = { _transferToFriendEvent.send(UiState.Failure(it as AppException)) }
                )
        }
    }

    private val _getRecipientEvent = Channel<UiState<List<RecipientUiModel>>>(Channel.BUFFERED)
    val getRecipientEvent = _getRecipientEvent.receiveAsFlow()

    fun getRecipientByPaymentId(paymentId: String) {
        viewModelScope.launch {
            _getRecipientEvent.send(UiState.Loading)

            getRecipientUseCase(paymentId).fold(
                onSuccess = {
                    val recipientList = mutableListOf<RecipientUiModel>()
                    for (rcpt in it.data) {
                        recipientList.add(rcpt.toRecipientUiModel())
                    }
                    _getRecipientEvent.send(UiState.Success(recipientList))
                },
                onFailure = { _getRecipientEvent.send(UiState.Failure(it as AppException)) }
            )
        }
    }

    private val _authPaymentPinEvent = Channel<UiState<Boolean>>(Channel.BUFFERED)
    val authPaymentPinEvent = _authPaymentPinEvent.receiveAsFlow()

    fun authPaymentPin(pin: String) {
        viewModelScope.launch {
            _authPaymentPinEvent.send(UiState.Loading)

            authPaymentPinUseCase(pin).fold(
                onSuccess = { _authPaymentPinEvent.send(UiState.Success(it.data)) },
                onFailure = { _authPaymentPinEvent.send(UiState.Failure(it as AppException)) }
            )
        }
    }
}