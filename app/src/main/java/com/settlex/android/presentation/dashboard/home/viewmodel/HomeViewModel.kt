package com.settlex.android.presentation.dashboard.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.R
import com.settlex.android.data.enums.TransactionOperation
import com.settlex.android.data.enums.TransactionStatus
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.remote.dto.TransactionDto
import com.settlex.android.data.repository.TransactionRepositoryImpl
import com.settlex.android.domain.session.UserSessionManager
import com.settlex.android.presentation.common.mapper.toHomeUiModel
import com.settlex.android.presentation.dashboard.UserState
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel
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
    private val transactionRepoImpl: TransactionRepositoryImpl,
    userSession: UserSessionManager
) : ViewModel() {

    val userState = userSession.authState
        .combine(userSession.userState) { auth, dto ->
            UiState.Success(
                UserState(
                    authUid = auth?.uid,
                    user = dto?.toHomeUiModel()
                )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val _recentTransactions = MutableStateFlow<UiState<List<TransactionItemUiModel>>>(UiState.Loading)
    val recentTransactions = _recentTransactions.asStateFlow()

    fun loadRecentTransactions(uid: String) {
        viewModelScope.launch {
            _recentTransactions.value = UiState.Loading

            transactionRepoImpl.getRecentTransactions(uid)
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

    private fun toUiModel(uid: String, dto: TransactionDto): TransactionItemUiModel {

        val isSender = uid == dto.senderUid

        val operation = when (dto.status) {
            TransactionStatus.REVERSED -> if (isSender) TransactionOperation.CREDIT else TransactionOperation.DEBIT
            else -> if (isSender) TransactionOperation.DEBIT else TransactionOperation.CREDIT
        }

        return TransactionItemUiModel(
            transactionId = dto.transactionId,
            description = dto.description,
            senderId = dto.sender,
            senderName = dto.senderName.uppercase(),
            recipientId = dto.recipient,
            recipientName = dto.recipientName.uppercase(),
            recipientOrSenderName = if (isSender) dto.recipientName.uppercase() else dto.senderName.uppercase(),
            serviceTypeName = if (isSender) dto.serviceType.displayName else "Payment Received",
            serviceTypeIcon = if (isSender) dto.serviceType.iconRes else R.drawable.ic_service_payment_received,
            operationSymbol = operation.symbol,
            operationColor = operation.colorRes,
            amount = CurrencyFormatter.formatToNaira(dto.amount),
            timestamp = DateFormatter.formatTimeStampToDateAndTime(dto.createdAt),
            status = dto.status.displayName,
            statusColor = dto.status.colorRes,
            statusBackgroundColor = dto.status.bgColorRes
        )
    }
}