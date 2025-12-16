package com.settlex.android.presentation.dashboard.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.R
import com.settlex.android.data.enums.TransactionOperation
import com.settlex.android.data.enums.TransactionStatus
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.mapper.toHomeUiModel
import com.settlex.android.data.remote.dto.TransactionDto
import com.settlex.android.data.repository.TransactionRepositoryImpl
import com.settlex.android.data.session.UserSessionManager
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.home.model.HomeUiModel
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel
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
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    val userSessionState: StateFlow<UserSessionState<HomeUiModel>> =
        userSessionManager.userSession.map { userSessionState ->
            when (userSessionState) {
                is UserSessionState.Loading -> UserSessionState.Loading
                is UserSessionState.UnAuthenticated -> UserSessionState.UnAuthenticated
                is UserSessionState.Error -> UserSessionState.Error(userSessionState.exception)
                is UserSessionState.Authenticated -> {
                    userSessionState.user.let {
                        _rawBalance.value = it.balance to it.commissionBalance
                    }
                    UserSessionState.Authenticated(userSessionState.user.toHomeUiModel())
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserSessionState.Loading
        )

    private val _rawBalance = MutableStateFlow<Pair<Long, Long>?>(null)

    private val _isBalanceHidden = MutableStateFlow(false)
    val isBalanceHidden: StateFlow<Boolean> = _isBalanceHidden.asStateFlow()

    /**
     * A [StateFlow] that emits the user's formatted wallet balance and commission balance as a [Pair] of strings.
     * The format of the balances depends on the visibility state controlled by [_isBalanceHidden].
     *
     * - When the balance is visible:
     *   - The main balance is formatted to a short version (e.g., "₦1.2M") if it's over a certain threshold, otherwise it's fully formatted (e.g., "₦1,234.56").
     *   - The commission balance is always formatted to a short version.
     * - When the balance is hidden, both values in the pair are replaced with asterisks ("****").
     *
     * This flow combines the raw balance from [_rawBalance] and the visibility state from [_isBalanceHidden]
     * to produce the final display strings. It emits `null` initially until the raw balance is available.
     */
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
                    else -> CurrencyFormatter.formatToNaira(balance)
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

    fun toggleBalanceVisibility() {
    }

    private val _recentTransactions =
        MutableStateFlow<UiState<List<TransactionItemUiModel>>>(UiState.Loading)
    val recentTransactions = _recentTransactions.asStateFlow()

    fun loadRecentTransactions(uid: String) {
        viewModelScope.launch {
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

    companion object {
        private const val MILLION_THRESHOLD_KOBO = 999999999L * 100
    }
}