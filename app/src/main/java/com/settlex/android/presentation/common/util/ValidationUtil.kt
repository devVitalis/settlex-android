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
        return id.matches("^[a-z][a-z0-9]{4,19}$".toRegex())
    }
}
