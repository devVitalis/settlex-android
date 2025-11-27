package com.settlex.android.presentation.common.components

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthManager(
    context: Context,
    activity: FragmentActivity,
    private val callback: BiometricAuthCallback
) {
    private val biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo

    init {
        // Executor to run UI actions on main thread
        val executor = ContextCompat.getMainExecutor(context)

        biometricPrompt =
            BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    callback.onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback.onFailed()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    callback.onAuthenticated()
                }
            })
    }

    fun authenticate(title: String, setNegativeButtonText: String) {
        promptInfo = PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(setNegativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    interface BiometricAuthCallback {
        fun onAuthenticated()

        fun onError(message: String?)

        fun onFailed()
    }

    companion object {
        /**
         * Check device biometric capability
         * call this method before triggering auth
         */
        fun isBiometricAvailable(context: Context): Boolean {
            val biometricManager = BiometricManager.from(context)
            val canAuthenticate =
                biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

            return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
        }

        /**
         * Returns feedback on why Device can't authenticate
         */
        fun getBiometricFeedback(context: Context): String {
            val status = BiometricManager.from(context)
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

            return when (status) {
                BiometricManager.BIOMETRIC_SUCCESS -> ""
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware available"
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware unavailable"
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometric enrolled"
                else -> "Biometric authentication not supported"
            }
        }
    }
}
