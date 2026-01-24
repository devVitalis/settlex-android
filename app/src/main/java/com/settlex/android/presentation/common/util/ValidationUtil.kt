package com.settlex.android.presentation.common.util

/**
 * An object that provides utility functions for various validation tasks.
 *
 * This utility object offers methods to validate different types of input, such as passwords
 * and payment IDs, based on a predefined set of rules.
 */
object ValidationUtil {
    private const val LENGTH = 8
    const val ALLOWED_SPECIAL_CHARS = "!@#$%^&*()_+-=[]{};:,.?"
    const val ERROR_PASSWORD_MISMATCH = "Passwords do not match!"
    const val PAYMENT_PIN_REGEX = "^\\d+$.*"
    private const val PHONE_NUMBER_REGEX = "^(0)?[7-9][0-1]\\d{8}$"
    private const val NAME_VALIDATION_REGEX = "^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$"


    fun isPasswordAndConfirmationValid(password: String, confirmationPassword: String): Boolean {
        val hasLength = password.length >= LENGTH
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasSpecial = password.any { ALLOWED_SPECIAL_CHARS.contains(it) }
        val passwordMatches = password == confirmationPassword

        return hasLength && hasUpper && hasLower && hasSpecial && passwordMatches
    }

    fun isPasswordValid(password: String): Boolean {
        val hasLength = password.length >= LENGTH
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasSpecial = password.any { ALLOWED_SPECIAL_CHARS.contains(it) }

        return hasLength && hasUpper && hasLower && hasSpecial
    }

    fun isPasswordsMatch(password: String, confirmationPassword: String): Boolean {
        return password == confirmationPassword
    }

    fun isPaymentIdValid(id: String): Boolean {
        val paymentId = if (id.startsWith("@")) id.substring(1) else id
        return paymentId.matches("^[a-z][a-z0-9]{4,19}$".toRegex())
    }

    fun isPaymentPinValid(pin: String): Boolean {
        return pin.matches(PAYMENT_PIN_REGEX.toRegex())
    }

    fun isPaymentPinValidAndMatch(pin: String, confirmationPin: String): Boolean {
        return pin.matches(PAYMENT_PIN_REGEX.toRegex()) && pin == confirmationPin
    }

    fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return phoneNumber.matches(PHONE_NUMBER_REGEX.toRegex())
    }

    fun isNamesValid(name1: String, name2: String): Boolean {
        return name1.matches(NAME_VALIDATION_REGEX.toRegex()) && name2.matches(NAME_VALIDATION_REGEX.toRegex())
    }
}
