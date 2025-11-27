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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _registrationEvent = MutableSharedFlow<UiState<Unit>>()
    val registrationEvent = _registrationEvent.asSharedFlow()

    fun register(user: UserModel, password: String) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _registrationEvent.emit(
                    UiState.Failure(
                        AppException.NetworkException(
                            ApiException.ERROR_NO_NETWORK
                        )
                    )
                )
                return@launch
            }

            _registrationEvent.emit(UiState.Loading)

            authUseCases.register(user, password)
                .onSuccess { _registrationEvent.emit(UiState.Success(Unit)) }
                .onFailure { _registrationEvent.emit(UiState.Failure(it as AppException)) }
        }
    }

    private val _otpEvent = MutableSharedFlow<UiState<String>>()
    val otpEvent = _otpEvent.asSharedFlow()

    fun sendVerificationCode(email: String, type: OtpType) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _otpEvent.emit(
                    UiState.Failure(
                        AppException.NetworkException(
                            ApiException.ERROR_NO_NETWORK
                        )
                    )
                )
                return@launch
            }

            _otpEvent.emit(UiState.Loading)

            authUseCases.sendOtp(email, type)
                .onSuccess { _otpEvent.emit(UiState.Success(it.data)) }
                .onFailure { _otpEvent.emit(UiState.Failure(it as AppException)) }
        }
    }

    private val _verifyEmailEvent = MutableSharedFlow<UiState<String>>()
    val verifyEmailEvent = _verifyEmailEvent.asSharedFlow()

    fun verifyEmail(email: String, otp: String) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _verifyEmailEvent.emit(
                    UiState.Failure(
                        AppException.NetworkException(
                            ApiException.ERROR_NO_NETWORK
                        )
                    )
                )
                return@launch
            }

            _verifyEmailEvent.emit(UiState.Loading)

            authUseCases.verifyEmail(email, otp)
                .onSuccess { _verifyEmailEvent.emit(UiState.Success(it.data)) }
                .onFailure { _verifyEmailEvent.emit(UiState.Failure(it as AppException)) }
        }
    }

    private val _verifyPasswordResetEvent = MutableSharedFlow<UiState<String>>()
    val verifyPasswordResetEvent = _verifyPasswordResetEvent.asSharedFlow()

    fun verifyPasswordReset(email: String, otp: String) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _verifyPasswordResetEvent.emit(
                    UiState.Failure(
                        AppException.NetworkException(
                            ApiException.ERROR_NO_NETWORK
                        )
                    )
                )
                return@launch
            }

            _verifyPasswordResetEvent.emit(UiState.Loading)

            authUseCases.verifyPasswordReset(email, otp)
                .onSuccess { _verifyPasswordResetEvent.emit(UiState.Success(it.data)) }
                .onFailure { _verifyPasswordResetEvent.emit(UiState.Failure(it as AppException)) }
        }
    }

    private val _setNewPasswordEvent = MutableSharedFlow<UiState<String>>()
    val setNewPasswordEvent = _setNewPasswordEvent.asSharedFlow()

    fun setNewPassword(email: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            if (!isNetworkConnected()) {
                _setNewPasswordEvent.emit(
                    UiState.Failure(
                        AppException.NetworkException(
                            ApiException.ERROR_NO_NETWORK
                        )
                    )
                )
                return@launch
            }

            _setNewPasswordEvent.emit(UiState.Loading)

            authUseCases.setNewPassword(email, oldPassword, newPassword)
                .onSuccess { _setNewPasswordEvent.emit(UiState.Success(it.data)) }
                .onFailure { _setNewPasswordEvent.emit(UiState.Failure(it as AppException)) }
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
