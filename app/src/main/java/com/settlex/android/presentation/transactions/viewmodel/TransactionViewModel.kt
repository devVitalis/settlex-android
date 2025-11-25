package com.settlex.android.presentation.transactions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.exception.AppException
import com.settlex.android.domain.usecase.txn.TransferToFriendUseCase
import com.settlex.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transferToFriendUseCase: TransferToFriendUseCase,
) :
    ViewModel() {

    private val _transferToFriendEvent = MutableSharedFlow<UiState<String>>()
    val transferToFriendEvent = _transferToFriendEvent.asSharedFlow()

    fun transferToFriend(
        fromUid: String,
        toPaymentId: String,
        txnId: String,
        amount: Long,
        desc: String
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
}