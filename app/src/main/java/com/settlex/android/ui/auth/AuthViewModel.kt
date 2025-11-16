package com.settlex.android.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.settlex.android.util.event.Event
import com.settlex.android.util.event.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val verifyPasswordResetUseCase: VerifyPasswordResetUseCase,
    private val setNewPasswordUseCase: SetNewPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<Event<UiState<Any?>>>()
    val uiState: LiveData<Event<UiState<Any?>>> = _uiState

    private val _userLiveData = MutableLiveData<UserModel>()
    val userLiveData: LiveData<UserModel> = _userLiveData


    fun login(email: String, password: String) {
        _uiState.value = Event(UiState.Loading)
        viewModelScope.launch {
            _uiState.value = Event(mapResultToUiState(loginUseCase(email, password)))
        }
    }

    fun register(user: UserModel, password: String) {
        _uiState.value = Event(UiState.Loading)
        viewModelScope.launch {
            _uiState.value = Event(mapResultToUiState(registerUseCase(user, password)))
        }
    }

    fun sendVerificationCode(email: String, type: OtpType) {
        _uiState.value = Event(UiState.Loading)
        viewModelScope.launch {
            _uiState.value = Event(mapResultToUiState(sendOtpUseCase(email, type)))
        }
    }

    fun verifyEmail(email: String, otp: String) {
        _uiState.value = Event(UiState.Loading)
        viewModelScope.launch {
            _uiState.value = Event(mapResultToUiState(verifyEmailUseCase(email, otp)))
        }
    }

    fun verifyPasswordReset(email: String, otp: String) {
        _uiState.value = Event(UiState.Loading)
        viewModelScope.launch {
            _uiState.value = Event(mapResultToUiState(verifyPasswordResetUseCase(email, otp)))
        }
    }

    fun setNewPassword(email: String, oldPassword: String, newPassword: String) {
        _uiState.value = Event(UiState.Loading)
        viewModelScope.launch {
            val result = setNewPasswordUseCase(email, oldPassword, newPassword)
            _uiState.value = Event(mapResultToUiState(result))
        }
    }

    private fun <T> mapResultToUiState(result: UiState<T>): UiState<T?> {
        return when (result) {
            is UiState.Success -> UiState.Success(result.data)
            is UiState.Failure -> UiState.Failure(result.message)
            is UiState.Loading -> UiState.Loading
        }
    }

    fun updateUser(user: UserModel) {
        _userLiveData.value = user
    }
}
