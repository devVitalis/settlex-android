package com.settlex.android.presentation.dashboard.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.R
import com.settlex.android.data.enums.TransactionOperation
import com.settlex.android.data.enums.TransactionStatus
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.exception.ExceptionMapper
import com.settlex.android.data.mapper.toHomeUiModel
import com.settlex.android.data.remote.dto.TransactionDto
import com.settlex.android.data.repository.TransactionRepositoryImpl
import com.settlex.android.data.session.UserSessionManager
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.presentation.common.extensions.toNairaString
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.home.model.HomeUiModel
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel
import com.settlex.android.util.network.NetworkMonitor
import com.settlex.android.util.string.CurrencyFormatter
import com.settlex.android.util.string.DateFormatter
import com.settlex.android.util.string.StringFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepoImpl: TransactionRepositoryImpl,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    val userSessionState: StateFlow<UserSessionState<HomeUiModel>> =
        sessionManager.userSession.map { userSessionState ->
            when (userSessionState) {
                is UserSessionState.Loading -> UserSessionState.Loading
                is UserSessionState.UnAuthenticated -> UserSessionState.UnAuthenticated
                is UserSessionState.Error -> UserSessionState.Error(userSessionState.exception)
                is UserSessionState.Authenticated -> {
                    userSessionState.user.let {
                        _rawBalance.value = it.balance to it.commissionBalance
                    }
                    _isBalanceHidden.value = sessionManager.userLocalDataSource.isBalanceHidden
                    UserSessionState.Authenticated(userSessionState.user.toHomeUiModel())
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserSessionState.Loading
        )

    private val _isBalanceHidden = MutableStateFlow(false)
    val isBalanceHidden: StateFlow<Boolean> = _isBalanceHidden.asStateFlow()

    fun toggleBalanceVisibility() {
        _isBalanceHidden.value = !_isBalanceHidden.value
        sessionManager.userLocalDataSource.isBalanceHidden = _isBalanceHidden.value
    }

    private val _rawBalance = MutableStateFlow<Pair<Long, Long>?>(null)

    val userBalance: StateFlow<Pair<String, String>?> = combine(
        _rawBalance, _isBalanceHidden
    ) { rawBalance, isHidden ->
        if (rawBalance == null) return@combine null

        val (balance, commissionBalance) = rawBalance
        when {
            isHidden -> StringFormatter.setAsterisks() to StringFormatter.setAsterisks()
            else -> {
                val formattedBalance = when {
                    balance > MILLION_THRESHOLD_KOBO -> CurrencyFormatter.formatToNairaShort(balance)
                    else -> balance.toNairaString()
                }

                val formattedCommission = CurrencyFormatter.formatToNairaShort(commissionBalance)
                formattedBalance to formattedCommission
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val _recentTransactions =
        MutableStateFlow<UiState<List<TransactionItemUiModel>>>(UiState.Loading)
    val recentTransactions = _recentTransactions.asStateFlow()

    fun loadRecentTransactions(uid: String) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _recentTransactions.value = UiState.Failure(
                    AppException.NetworkException(ExceptionMapper.ERROR_NO_NETWORK)
                )
                return@launch
            }
            transactionRepoImpl.getRecentTransactions(uid).collect { result ->
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
                        _recentTransactions.value = UiState.Failure(it as AppException)
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
            amount = dto.amount.toNairaString(),
            timestamp = DateFormatter.toFormattedDateString(dto.createdAt),
            status = dto.status.displayName,
            statusColor = dto.status.colorRes,
            statusBackgroundColor = dto.status.bgColorRes
        )
    }

    private fun isNetworkConnected(): Boolean {
        return NetworkMonitor.networkStatus.value
    }

    companion object {
        private const val MILLION_THRESHOLD_KOBO = 999999999L * 100
    }
}