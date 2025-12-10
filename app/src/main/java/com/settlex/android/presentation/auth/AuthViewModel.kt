package com.settlex.android.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.exception.ApiException
import com.settlex.android.data.exception.AppException
import com.settlex.android.domain.model.UserModel
import com.settlex.android.domain.usecase.auth.AuthUseCases
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.util.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling authentication-related business logic and state.
 *
 * This ViewModel orchestrates authentication flows such as login, registration,
 * email verification, and password reset. It interacts with various use cases
 * from the domain layer to perform these actions and exposes the results to the UI
 * through `SharedFlow`s representing different UI states.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _registrationEvent = Channel<UiState<Unit>>(capacity = Channel.BUFFERED)
    val registrationEvent = _registrationEvent.receiveAsFlow()

    fun register(user: UserModel, password: String) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _registrationEvent.send(
                    UiState.Failure(
                        AppException.NetworkException(
                            ApiException.ERROR_NO_NETWORK
                        )
                    )
                )
                return@launch
            }
            _registrationEvent.send(UiState.Loading)

            authUseCases.register(user, password)
                .onSuccess { _registrationEvent.send(UiState.Success(Unit)) }
                .onFailure { _registrationEvent.send(UiState.Failure(it as AppException)) }
        }
    }

    private val _otpEvent = Channel<UiState<String>>(capacity = Channel.BUFFERED)
    val otpEvent = _otpEvent.receiveAsFlow()

    fun sendVerificationCode(email: String, type: OtpType) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _otpEvent.send(
                    UiState.Failure(
                        AppException.NetworkException(
                            ApiException.ERROR_NO_NETWORK
                        )
                    )
                )
                return@launch
            }
            _otpEvent.send(UiState.Loading)

            authUseCases.sendOtp(email, type)
                .onSuccess { _otpEvent.send(UiState.Success(it.data)) }
                .onFailure { _otpEvent.send(UiState.Failure(it as AppException)) }
        }
    }

    private val _verifyEmailEvent = Channel<UiState<String>>(capacity = Channel.BUFFERED)
    val verifyEmailEvent = _verifyEmailEvent.receiveAsFlow()

    fun verifyEmail(email: String, otp: String) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _verifyEmailEvent.send(
                    UiState.Failure(
                        AppException.NetworkException(
                            ApiException.ERROR_NO_NETWORK
                        )
                    )
                )
                return@launch
            }
            _verifyEmailEvent.send(UiState.Loading)

            authUseCases.verifyEmail(email, otp)
                .onSuccess { _verifyEmailEvent.send(UiState.Success(it.data)) }
                .onFailure { _verifyEmailEvent.send(UiState.Failure(it as AppException)) }
        }
    }

    private val _verifyPasswordResetEvent = Channel<UiState<String>>(capacity = Channel.BUFFERED)
    val verifyPasswordResetEvent = _verifyPasswordResetEvent.receiveAsFlow()

    fun verifyPasswordReset(email: String, otp: String) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _verifyPasswordResetEvent.send(
                    UiState.Failure(
                        AppException.NetworkException(
                            ApiException.ERROR_NO_NETWORK
                        )
                    )
                )
                return@launch
            }
            _verifyPasswordResetEvent.send(UiState.Loading)

            authUseCases.verifyPasswordReset(email, otp)
                .onSuccess { _verifyPasswordResetEvent.send(UiState.Success(it.data)) }
                .onFailure { _verifyPasswordResetEvent.send(UiState.Failure(it as AppException)) }
        }
    }

    private val _setNewPasswordEvent = Channel<UiState<String>>(capacity = Channel.BUFFERED)
    val setNewPasswordEvent = _setNewPasswordEvent.receiveAsFlow()

    fun setNewPassword(email: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _setNewPasswordEvent.send(
                    UiState.Failure(
                        AppException.NetworkException(
                            ApiException.ERROR_NO_NETWORK
                        )
                    )
                )
                return@launch
            }
            _setNewPasswordEvent.send(UiState.Loading)

            authUseCases.setNewPassword(email, oldPassword, newPassword)
                .onSuccess { _setNewPasswordEvent.send(UiState.Success(it.data)) }
                .onFailure { _setNewPasswordEvent.send(UiState.Failure(it as AppException)) }
        }
    }

    /**
     * Checks if the device has an active network connection.
     *
     * This function is intended to be implemented to verify network availability
     * before making network requests, ensuring the app can gracefully handle
     * offline scenarios.
     *
     * @return `true` if the device is connected to a network, `false` otherwise.
     */
    fun isNetworkConnected(): Boolean {
        return NetworkMonitor.networkStatus.value
    }
}
