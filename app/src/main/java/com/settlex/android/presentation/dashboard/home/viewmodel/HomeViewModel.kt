package com.settlex.android.presentation.dashboard.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.R
import com.settlex.android.data.enums.TransactionOperation
import com.settlex.android.data.enums.TransactionStatus
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.remote.dto.TransactionDto
import com.settlex.android.data.repository.TxnRepositoryImpl
import com.settlex.android.domain.session.UserSessionManager
import com.settlex.android.presentation.account.extension.toUiModel
import com.settlex.android.presentation.account.model.UserState
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.transactions.model.TransactionUiModel
import com.settlex.android.util.string.CurrencyFormatter
import com.settlex.android.util.string.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val txnRepoImpl: TxnRepositoryImpl,
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

    private val _recentTransactions = MutableStateFlow<UiState<List<TransactionUiModel>>>(UiState.Loading)
    val recentTransactions = _recentTransactions.asStateFlow()

    fun loadRecentTransactions(uid: String) {
        viewModelScope.launch {

            _recentTransactions.value = UiState.Loading

            txnRepoImpl.getRecentTransactions(uid)
                .collect { result ->

                    result.fold(
                        onSuccess = { dtoList ->
                            if (dtoList.isEmpty()) {
                                _recentTransactions.value = UiState.Success(emptyList())
                                return@fold
                            }

                            val mapped = dtoList.map { dto -> toUiModel(uid, dto) }
                            _recentTransactions.value = UiState.Success(mapped)
                        },

                        onFailure = {
                            _recentTransactions.value =
                                UiState.Failure(it as AppException)
                        }
                    )
                }
        }
    }

    private fun toUiModel(uid: String, dto: TransactionDto): TransactionUiModel {

        val isSender = uid == dto.senderUid

        val operation = when (dto.status) {
            TransactionStatus.REVERSED -> if (isSender) TransactionOperation.CREDIT else TransactionOperation.DEBIT
            else -> if (isSender) TransactionOperation.DEBIT else TransactionOperation.CREDIT
        }

        return TransactionUiModel(
            transactionId = dto.transactionId,
            description = dto.description,
            sender = dto.sender,
            senderName = dto.senderName.uppercase(),
            recipient = dto.recipient,
            recipientName = dto.recipientName.uppercase(),
            displayName = if (isSender) dto.recipientName.uppercase() else dto.senderName.uppercase(),
            serviceTypeName = if (isSender) dto.serviceType.displayName else "Payment Received",
            serviceTypeIcon = if (isSender) dto.serviceType.iconRes else R.drawable.ic_service_payment_received,
            operationSymbol = operation.symbol,
            operationColor = operation.colorRes,
            amount = CurrencyFormatter.formatToNaira(dto.amount),
            timestamp = DateFormatter.formatTimeStampToDateAndTime(dto.createdAt),
            status = dto.status.displayName,
            statusColor = dto.status.colorRes,
            statusBgColor = dto.status.bgColorRes
        )
    }
}