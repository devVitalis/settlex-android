package com.settlex.android.ui.auth.register

import androidx.lifecycle.ViewModel
import com.settlex.android.domain.model.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    private var firstName: String = ""
    private var lastName: String = ""
    private var email: String = ""
    private var phone: String = ""
    private var referralCode: String? = null
    private var fcmToken: String? = null

    private val _registrationState = MutableStateFlow(RegistrationState())
    val registrationState = _registrationState.asStateFlow()

    fun updateName(first: String, last: String) {
        firstName = first
        lastName = last
        _registrationState.update {
            it.copy(firstName = first, lastName = last)
        }
    }

    fun storeUserContactInfo(emailValue: String, phoneValue: String) {
        email = emailValue
        phone = phoneValue
        _registrationState.update {
            it.copy(email = emailValue, phone = phoneValue)
        }
    }

    fun updateReferralCode(refCode: String?) {
        referralCode = refCode
        _registrationState.update {
            it.copy(referralCode = refCode)
        }
    }

    fun updateFcmToken(token: String) {
        fcmToken = token
        _registrationState.update { it.copy(fcmToken = token) }
    }

    fun buildUserModel(uid: String): UserModel {
        return UserModel(
            uid = uid,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            referralCode = referralCode,
            fcmToken = fcmToken,
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
