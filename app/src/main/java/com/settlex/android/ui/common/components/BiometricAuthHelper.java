package com.settlex.android.ui.common.components;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class BiometricAuthHelper {

    private final Context context;
    private final BiometricAuthCallback callback;
    private final BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public BiometricAuthHelper(Context context, FragmentActivity activity, BiometricAuthCallback callback) {
        this.context = context;
        this.callback = callback;

        // Executor to run UI actions on main thread
        var executor = ContextCompat.getMainExecutor(context);

        biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                callback.onError(errString.toString());
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                callback.onFailed();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onAuthenticated();
            }
        });
    }

    public void authenticate(String setNegativeButtonText) {
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirm your identity")
                .setNegativeButtonText(setNegativeButtonText)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Check device biometric capability
     * call this method before triggering auth
     */
    public static boolean isBiometricAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Returns feedback on why Device can't authenticate
     */
    public static String getBiometricFeedback(Context context) {
        int status = BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        switch (status) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return "";
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "No biometric hardware available";
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Biometric hardware unavailable";
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "No biometric enrolled";
            default:
                return "Biometric authentication not supported";
        }
    }

    public interface BiometricAuthCallback {
        void onAuthenticated();

        void onError(String message);

        void onFailed();
    }
}
