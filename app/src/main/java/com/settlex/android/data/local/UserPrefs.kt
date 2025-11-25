package com.settlex.android.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Manages user-specific preferences, such as biometrics settings and UI choices.
 *
 * This class provides an interface to read and write user preferences to `SharedPreferences`.
 * An instance of this class is scoped to a specific user, identified by their unique ID (UID),
 * ensuring that preferences are stored separately for each user.
 *
 * @property context The application context, used to access `SharedPreferences`.
 * @property uid The unique identifier for the user. This is appended to the preferences filename
 *           to create a user-specific storage.
 */
class UserPrefs(context: Context, uid: String) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME + uid, Context.MODE_PRIVATE)

    var isPayBiometricsEnabled: Boolean
        get() = prefs.getBoolean(KEY_PAYMENT_BIOMETRIC_ENABLED, false)
        set(enabled) = prefs.edit { putBoolean(KEY_PAYMENT_BIOMETRIC_ENABLED, enabled) }


    var isLoginBiometricsEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOGIN_BIOMETRIC_ENABLED, false)
        set(enabled) = prefs.edit { putBoolean(KEY_LOGIN_BIOMETRIC_ENABLED, enabled) }

    var isBalanceHidden: Boolean
        get() = prefs.getBoolean(KEY_HIDE_BALANCE, false)
        set(hidden) = prefs.edit { putBoolean(KEY_HIDE_BALANCE, hidden) }


    companion object {
        private const val PREFS_NAME = "user_prefs_"
        private const val KEY_HIDE_BALANCE = "hide_balance"
        private const val KEY_PAYMENT_BIOMETRIC_ENABLED = "payment_biometric_enabled"
        private const val KEY_LOGIN_BIOMETRIC_ENABLED = "login_biometric_enabled"
    }
}
