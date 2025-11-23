package com.settlex.android.presentation.auth.util

import android.content.Intent

/**
 * A utility object responsible for parsing the password flow type from an [Intent].
 * This helps determine whether the user is in a "forgot password" flow (unauthenticated)
 * or a "change password" flow (authenticated).
 */
object PasswordFlowParser {
    fun fromIntent(intent: Intent): PasswordFlow {
        return when (intent.getStringExtra("password_flow")) {
            "forgot" -> PasswordFlow.Forgot
            "change" -> PasswordFlow.Change
            "auth_reset" -> PasswordFlow.AuthenticatedReset
            else -> throw IllegalArgumentException("Invalid password flow type")
        }
    }
}
