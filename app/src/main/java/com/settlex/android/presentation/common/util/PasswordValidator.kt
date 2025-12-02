package com.settlex.android.presentation.common.util

/**
 * An object that provides utility functions to validate password strings based on a set of rules.
 *
 * The validation rules include checks for minimum length, presence of uppercase and lowercase letters,
 * and the inclusion of special characters. It also provides a method to check if a password
 * matches its confirmation string.
 */
object PasswordValidator {
    private const val LENGTH = 8
    const val ALLOWED_SPECIAL_CHARS = "!@#$%^&*()_+-=[]{};:,.?"
    const val ERROR_PASSWORD_MISMATCH = "Passwords do not match!"

    fun validate(password: String, confirm: String): Boolean {
        val hasLength = password.length >= LENGTH
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasSpecial = password.any { ALLOWED_SPECIAL_CHARS.contains(it) }
        val matches = password == confirm

        return hasLength && hasUpper && hasLower && hasSpecial && matches
    }

    fun validate(password: String): Boolean {
        val hasLength = password.length >= LENGTH
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasSpecial = password.any { ALLOWED_SPECIAL_CHARS.contains(it) }

        return hasLength && hasUpper && hasLower && hasSpecial
    }
}
