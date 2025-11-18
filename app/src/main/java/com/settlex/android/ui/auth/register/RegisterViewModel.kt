package com.settlex.android.ui.auth.register

import androidx.lifecycle.ViewModel
import com.settlex.android.domain.model.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    private val _registrationState = MutableStateFlow(RegistrationState())

    fun updateContact(newEmail: String, newPhoneNumber: String) {
        _registrationState.update { it.copy(email = newEmail, phone = newPhoneNumber) }
    }

    val email: String get() = _registrationState.value.email

    fun updateName(newFirstName: String, newLastName: String) {
        _registrationState.update { it.copy(firstName = newFirstName, lastName = newLastName) }
    }

    fun updateReferralCode(newReferralCode: String?) {
        _registrationState.update { it.copy(referralCode = newReferralCode) }
    }

    fun updateFcmToken(newFcmToken: String) {
        _registrationState.update { it.copy(fcmToken = newFcmToken) }
    }

    fun buildUserModel(uid: String): UserModel {
        val user = _registrationState.value
        return UserModel(
            uid = uid,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            phone = user.phone,
            referralCode = user.referralCode,
            fcmToken = user.fcmToken,
        )
    }

    data class RegistrationState(
        val firstName: String = "",
        val lastName: String = "",
        val email: String = "",
        val phone: String = "",
        val referralCode: String? = null,
        val fcmToken: String? = null,
    )
}
