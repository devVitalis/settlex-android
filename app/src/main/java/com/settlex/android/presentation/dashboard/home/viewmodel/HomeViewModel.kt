package com.settlex.android.presentation.dashboard.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.enums.ServiceType
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.exception.ExceptionMapper
import com.settlex.android.data.mapper.toHomeUiModel
import com.settlex.android.data.mapper.toTransactionUiModel
import com.settlex.android.data.repository.TransactionRepositoryImpl
import com.settlex.android.data.session.UserSessionManager
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.presentation.common.extensions.toNairaString
import com.settlex.android.presentation.common.extensions.toNairaStringShort
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.home.model.HomeUiModel
import com.settlex.android.presentation.dashboard.services.model.ServiceUiModel
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel
import com.settlex.android.util.network.NetworkMonitor
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
            isHidden -> "****" to "****"
            else -> {
                val formattedBalance = when {
                    balance > MILLION_THRESHOLD_KOBO -> balance.toNairaStringShort()
                    else -> balance.toNairaString()
                }

                val formattedCommission = commissionBalance.toNairaStringShort()
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

    fun fetchRecentTransactions() {
        viewModelScope.launch {
            if (!isNetworkAvailable()) {
                _recentTransactions.value = sendNetworkErrorException()
                return@launch
            }

            val (uid, transactionsFlow) = transactionRepoImpl.fetchRecentTransactions()
            transactionsFlow.collect { result ->
                result.fold(
                    onSuccess = { dtoList ->
                        if (dtoList.isEmpty()) {
                            _recentTransactions.value = UiState.Success(emptyList())
                            return@fold
                        }

                        val mapped = dtoList.map { it.toTransactionUiModel(uid) }
                        _recentTransactions.value = UiState.Success(mapped)
                    },

                    onFailure = {
                        _recentTransactions.value = UiState.Failure(it as AppException)
                    }
                )
            }
        }
    }

    val homeServiceList by lazy {
        ServiceType.entries
            .take(8)
            .map { serviceType ->
                ServiceUiModel(
                    serviceType.displayName,
                    serviceType.iconRes,
                    serviceType.cashbackPercentage,
                    serviceType.label,
                    serviceType.transactionServiceType,
                    serviceType.destination
                )
            }
    }

    private fun <T> sendNetworkErrorException(): UiState<T> {
        return UiState.Failure(
            AppException.NetworkException(
                ExceptionMapper.ERROR_NO_NETWORK
            )
        )
    }

    private fun isNetworkAvailable(): Boolean {
        return NetworkMonitor.networkStatus.value
    }

    companion object {
        private const val MILLION_THRESHOLD_KOBO = 999999999L * 100
    }
}