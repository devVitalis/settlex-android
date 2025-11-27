package com.settlex.android.data.datasource


import android.content.Context
import com.settlex.android.data.local.UserPrefs
import jakarta.inject.Singleton


@Singleton
class UserLocalDataSource(
    context: Context,
    uid: String
) {
    private val userPrefs = UserPrefs(context, uid)

    // Pay Biometrics
    var isPayBiometricsEnabled: Boolean
        get() = userPrefs.isPayBiometricsEnabled
        set(value) {
            userPrefs.isPayBiometricsEnabled = value
        }

    // Login Biometrics
    var isLoginBiometricsEnabled: Boolean
        get() = userPrefs.isLoginBiometricsEnabled
        set(value) {
            userPrefs.isLoginBiometricsEnabled = value
        }

    // Balance visibility
    var isBalanceHidden: Boolean
        get() = userPrefs.isBalanceHidden
        set(value) {
            userPrefs.isBalanceHidden = value
        }
}
