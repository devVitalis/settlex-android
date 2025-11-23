package com.settlex.android.presentation.auth.register

import androidx.lifecycle.ViewModel
import com.settlex.android.domain.model.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    private val registrationState = MutableStateFlow(RegistrationState())

    fun updateContact(newEmail: String, newPhoneNumber: String) {
        registrationState.update { it.copy(email = newEmail, phone = newPhoneNumber) }
    }

    val email: String get() = registrationState.value.email

    fun updateName(newFirstName: String, newLastName: String) {
        registrationState.update { it.copy(firstName = newFirstName, lastName = newLastName) }
    }

    fun updateReferralCode(newReferralCode: String?) {
        registrationState.update { it.copy(referralCode = newReferralCode) }
    }

    fun updateFcmToken(newFcmToken: String) {
        registrationState.update { it.copy(fcmToken = newFcmToken) }
    }

    fun buildUserModel(uid: String): UserModel {
        val currentState = registrationState.value
        return UserModel(
            uid = uid,
            firstName = currentState.firstName,
            lastName = currentState.lastName,
            email = currentState.email,
            phone = currentState.phone,
            referralCode = currentState.referralCode,
            fcmToken = currentState.fcmToken,
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
