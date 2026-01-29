package com.settlex.android.presentation.transactions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.exception.ExceptionMapper
import com.settlex.android.data.mapper.toRecipientUiModel
import com.settlex.android.data.mapper.toTransactionUiModel
import com.settlex.android.data.mapper.toTransferToFriendUiModel
import com.settlex.android.data.repository.TransactionRepositoryImpl
import com.settlex.android.data.session.UserSessionManager
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.domain.usecase.transaction.TransferToFriendUseCase
import com.settlex.android.domain.usecase.user.AuthPaymentPinUseCase
import com.settlex.android.domain.usecase.user.GetReceipientUseCase
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.transactions.model.RecipientUiModel
import com.settlex.android.presentation.transactions.model.TransactionUiModel
import com.settlex.android.presentation.transactions.model.TransferToFriendUiModel
import com.settlex.android.util.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transferToFriendUseCase: TransferToFriendUseCase,
    private val getRecipientUseCase: GetReceipientUseCase,
    private val authPaymentPinUseCase: AuthPaymentPinUseCase,
    sessionManager: UserSessionManager,
    private val transactionRepoImpl: TransactionRepositoryImpl
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
            if (!isInternetConnected()) {
                _transferToFriendEvent.send(sendNetworkException())
                return@launch
            }

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
            if (!isInternetConnected()) {
                _getRecipientEvent.send(sendNetworkException())
                return@launch
            }

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
            if (!isInternetConnected()) {
                _authPaymentPinEvent.send(sendNetworkException())
                return@launch
            }

            _authPaymentPinEvent.send(UiState.Loading)

            authPaymentPinUseCase(pin).fold(
                onSuccess = { _authPaymentPinEvent.send(UiState.Success(it.data)) },
                onFailure = { _authPaymentPinEvent.send(UiState.Failure(it as AppException)) }
            )
        }
    }

    private val _fetchTransactionsForTheMonth =
        MutableStateFlow<UiState<List<TransactionUiModel>>>(UiState.Loading)
    val fetchTransactionsForTheMonth = _fetchTransactionsForTheMonth.asStateFlow()

    fun fetchTransactionsForTheMonth() {
        viewModelScope.launch {
            if (!isInternetConnected()) {
                _fetchTransactionsForTheMonth.emit(sendNetworkException())
                return@launch
            }

            _fetchTransactionsForTheMonth.emit(UiState.Loading)

            transactionRepoImpl.fetchTransactionsForTheMonth().collect { result ->
                result.fold(
                    onSuccess = { response ->
                        val (uid, transactionList) = response
                        if (transactionList.isEmpty()) {
                            _fetchTransactionsForTheMonth.emit(UiState.Success(emptyList()))
                            return@collect
                        }

                        val mapped = transactionList.map { it.toTransactionUiModel(uid) }
                        _fetchTransactionsForTheMonth.emit(UiState.Success(mapped))
                    },
                    onFailure = { _fetchTransactionsForTheMonth.emit(UiState.Failure(it as AppException)) }
                )
            }
        }
    }

    private fun <T> sendNetworkException(): UiState<T> {
        return UiState.Failure(
            AppException.NetworkException(
                ExceptionMapper.ERROR_NO_NETWORK
            )
        )
    }

    private fun isInternetConnected(): Boolean {
        return NetworkMonitor.networkStatus.value
    }
}