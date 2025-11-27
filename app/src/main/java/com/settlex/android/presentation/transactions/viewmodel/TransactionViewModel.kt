package com.settlex.android.presentation.transactions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.session.UserSessionManager
import com.settlex.android.domain.usecase.transaction.TransferToFriendUseCase
import com.settlex.android.domain.usecase.user.AuthPaymentPinUseCase
import com.settlex.android.domain.usecase.user.GetReceipientUseCase
import com.settlex.android.data.mapper.toRecipientUiModel
import com.settlex.android.data.mapper.toTransferToFriendUiModel
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.UserState
import com.settlex.android.presentation.transactions.model.RecipientUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transferToFriendUseCase: TransferToFriendUseCase,
    private val getRecipientUseCase: GetReceipientUseCase,
    private val authPaymentPinUseCase: AuthPaymentPinUseCase,
    userSession: UserSessionManager
) :
    ViewModel() {
    val userState = userSession.authState
        .combine(userSession.userState) { auth, dto ->
            UiState.Success(
                UserState(
                    authUid = auth?.uid,
                    user = dto?.toTransferToFriendUiModel()
                )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val _transferToFriendEvent = MutableSharedFlow<UiState<String>>()
    val transferToFriendEvent = _transferToFriendEvent.asSharedFlow()

    fun transferToFriend(
        fromUid: String,
        toPaymentId: String,
        txnId: String,
        amount: Long,
        desc: String?
    ) {
        viewModelScope.launch {
            _transferToFriendEvent.emit(UiState.Loading)
            transferToFriendUseCase(fromUid, toPaymentId, txnId, amount, desc)
                .fold(
                    onSuccess = { _transferToFriendEvent.emit(UiState.Success(it.data)) },
                    onFailure = { _transferToFriendEvent.emit(UiState.Failure(it as AppException)) }
                )
        }
    }

    private val _getRecipientEvent = MutableSharedFlow<UiState<List<RecipientUiModel>>>()
    val getRecipientEvent = _getRecipientEvent.asSharedFlow()

    fun getRecipientByPaymentId(paymentId: String) {
        viewModelScope.launch {
            _getRecipientEvent.emit(UiState.Loading)

            getRecipientUseCase(paymentId).fold(
                onSuccess = {

                    val recipientList = mutableListOf<RecipientUiModel>()
                    for (rcpt in it.data) {
                        recipientList.add(
                            rcpt.toRecipientUiModel()
                        )
                    }

                    _getRecipientEvent.emit(
                        UiState.Success(
                            recipientList
                        )
                    )
                },
                onFailure = { _getRecipientEvent.emit(UiState.Failure(it as AppException)) }
            )
        }
    }

    private val _authPaymentPinEvent = MutableSharedFlow<UiState<Boolean>>()
    val authPaymentPinEvent = _authPaymentPinEvent.asSharedFlow()

    fun authPaymentPin(pin: String) {
        viewModelScope.launch {
            _authPaymentPinEvent.emit(UiState.Loading)

            authPaymentPinUseCase(pin).fold(
                onSuccess = { _authPaymentPinEvent.emit(UiState.Success(it.data)) },
                onFailure = { _authPaymentPinEvent.emit(UiState.Failure(it as AppException)) }
            )
        }
    }
}