package com.settlex.android.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.settlex.android.SettleXApp
import com.settlex.android.data.datasource.UserLocalDataSource
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.exception.ExceptionMapper
import com.settlex.android.domain.usecase.auth.GetCurrentUserUseCase
import com.settlex.android.domain.usecase.auth.LoginUseCase
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.util.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private var _userLocalDataSource: UserLocalDataSource? = null
    private val userLocalDataSource: UserLocalDataSource
        get() = _userLocalDataSource ?: throw IllegalStateException(
            "UserLocalDataSource requested before login"
        )

    private val _isLoginBiometricsEnabled = MutableStateFlow(false)
    val isLoginBiometricsEnabled = _isLoginBiometricsEnabled.asStateFlow()

    private val _userState = MutableStateFlow<LoginState>(LoginState.Unauthenticated)
    val userState = _userState.asStateFlow()

    init {
        initUserState()
    }

    private fun initUserState() {
        viewModelScope.launch {
            val currentUser = getCurrentUserUseCase()
            if (currentUser == null) {
                _userState.emit(LoginState.Unauthenticated)
                _userLocalDataSource = null
                return@launch
            }

            _userState.emit(
                LoginState.Authenticated(
                    LoginUiModel(
                        currentUser.uid,
                        currentUser.email ?: "",
                        currentUser.displayName ?: "",
                        currentUser.photoUrl?.toString()
                    )
                )
            )

            _userLocalDataSource = UserLocalDataSource(
                SettleXApp.appContext,
                currentUser.uid
            )

            // Update the biometrics state
            _isLoginBiometricsEnabled.emit(
                userLocalDataSource.isLoginBiometricsEnabled
            )
        }
    }

    private val _loginEvent = MutableSharedFlow<UiState<Unit>>()
    val loginEvent = _loginEvent.asSharedFlow()

    /**
     * Attempts to log in a user with the provided credentials.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _loginEvent.emit(
                    UiState.Failure(
                        AppException.NetworkException(
                            ExceptionMapper.ERROR_NO_NETWORK
                        )
                    )
                )
                return@launch
            }

            _loginEvent.emit(UiState.Loading)

            loginUseCase(email, password)
                .onSuccess { _loginEvent.emit(UiState.Success(Unit)) }
                .onFailure { _loginEvent.emit(UiState.Failure(it as AppException)) }
        }
    }

    fun isNetworkConnected(): Boolean {
        return NetworkMonitor.networkStatus.value
    }

    fun logout() {
        // TODO: For testing, delete Later
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
        }
    }
}