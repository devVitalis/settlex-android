package com.settlex.android.ui.auth.common

/**
 * An object that provides utility functions to validate password strings based on a set of rules.
 *
 * The validation rules include checks for minimum length, presence of uppercase and lowercase letters,
 * and the inclusion of special characters. It also provides a method to check if a password
 * matches its confirmation string.
 */
object PasswordValidator {

    fun validate(password: String, confirm: String): Boolean {
        val hasLength = password.length >= 8
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasSpecial = password.any { "!@#$%^&*()_+-=[]{};:,.?".contains(it) }
        val matches = password == confirm

        return hasLength && hasUpper && hasLower && hasSpecial && matches
    }

    fun validate(password: String): Boolean {
        val hasLength = password.length >= 8
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasSpecial = password.any { "!@#$%^&*()_+-=[]{};:,.?".contains(it) }

        return hasLength && hasUpper && hasLower && hasSpecial
    }
}
