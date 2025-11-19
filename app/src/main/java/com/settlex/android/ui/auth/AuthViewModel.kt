package com.settlex.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.enums.OtpType
import com.settlex.android.domain.model.UserModel
import com.settlex.android.domain.usecase.LoginUseCase
import com.settlex.android.domain.usecase.RegisterUseCase
import com.settlex.android.domain.usecase.SendOtpUseCase
import com.settlex.android.domain.usecase.SetNewPasswordUseCase
import com.settlex.android.domain.usecase.VerifyEmailUseCase
import com.settlex.android.domain.usecase.VerifyPasswordResetUseCase
import com.settlex.android.ui.common.event.UiState
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
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val verifyPasswordResetUseCase: VerifyPasswordResetUseCase,
    private val setNewPasswordUseCase: SetNewPasswordUseCase
) : ViewModel() {

    private val _loginEvent = MutableSharedFlow<UiState<Unit>>()
    val loginEvent = _loginEvent.asSharedFlow()

    private val _registrationEvent = MutableSharedFlow<UiState<Unit>>()
    val registrationEvent = _registrationEvent.asSharedFlow()

    private val _otpEvent = MutableSharedFlow<UiState<String>>()
    val otpEvent = _otpEvent.asSharedFlow()

    private val _verifyEmailEvent = MutableSharedFlow<UiState<String>>()
    val verifyEmailEvent = _verifyEmailEvent.asSharedFlow()

    private val _verifyPasswordResetEvent = MutableSharedFlow<UiState<String>>()
    val verifyPasswordResetEvent = _verifyPasswordResetEvent.asSharedFlow()

    private val _setNewPasswordEvent = MutableSharedFlow<UiState<String>>()
    val setNewPasswordEvent = _setNewPasswordEvent.asSharedFlow()

    /**
     * Attempts to log in a user with the provided credentials.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginEvent.emit(UiState.Loading)

            loginUseCase(email, password)
                .onSuccess { _loginEvent.emit(UiState.Success(Unit)) }
                .onFailure { _loginEvent.emit(UiState.Failure(it.message)) }
        }
    }

    fun register(user: UserModel, password: String) {
        viewModelScope.launch {
            _registrationEvent.emit(UiState.Loading)

            registerUseCase(user, password)
                .onSuccess { _registrationEvent.emit(UiState.Success(Unit)) }
                .onFailure { _registrationEvent.emit(UiState.Failure(it.message)) }
        }
    }

    fun sendVerificationCode(email: String, type: OtpType) {
        viewModelScope.launch {
            _otpEvent.emit(UiState.Loading)

            sendOtpUseCase(email, type)
                .onSuccess { _otpEvent.emit(UiState.Success(it.data)) }
                .onFailure { _otpEvent.emit(UiState.Failure(it.message)) }
        }
    }

    fun verifyEmail(email: String, otp: String) {
        viewModelScope.launch {
            _verifyEmailEvent.emit(UiState.Loading)

            verifyEmailUseCase(email, otp)
                .onSuccess { _verifyEmailEvent.emit(UiState.Success(it.data)) }
                .onFailure { _verifyEmailEvent.emit(UiState.Failure(it.message)) }
        }
    }

    fun verifyPasswordReset(email: String, otp: String) {
        viewModelScope.launch {
            _verifyPasswordResetEvent.emit(UiState.Loading)

            verifyPasswordResetUseCase(email, otp)
                .onSuccess { _verifyPasswordResetEvent.emit(UiState.Success(it.data)) }
                .onFailure { _verifyPasswordResetEvent.emit(UiState.Failure(it.message)) }
        }
    }

    fun setNewPassword(email: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _setNewPasswordEvent.emit(UiState.Loading)

            setNewPasswordUseCase(email, oldPassword, newPassword)
                .onSuccess { _setNewPasswordEvent.emit(UiState.Success(it.data)) }
                .onFailure { _setNewPasswordEvent.emit(UiState.Failure(it.message)) }
        }
    }
}
