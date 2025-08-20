package com.settlex.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user-specific onboarding preferences (biometrics, prompts, etc.).
 * Scoped per-user using UID in SharedPreferences name.
 */
public class UserOnboardPrefs {

    // Preference keys (all user-scoped)
    private static final String PREF_NAME = "user_onboard_prefs";
    private static final String KEY_NOTIFICATION_PROMPT_SHOWN = "onboard_notification_prompt_shown";
    private static final String KEY_PIN_PROMPT_SHOWN = "onboard_pin_prompt_shown";
    private static final String KEY_FINGERPRINT_ENABLED = "onboard_fingerprint_enabled";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    /**
     * Creates user-scoped preference file.
     * UID ensures isolation between accounts on the same device.
     */
    public UserOnboardPrefs(Context context, String uid) {
        prefs = context.getSharedPreferences(PREF_NAME + "_" + uid, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // --- User-specific onboarding states ---
    public boolean isNotificationPromptShown() {
        return prefs.getBoolean(KEY_NOTIFICATION_PROMPT_SHOWN, false);
    }

    public void setNotificationPromptShown(boolean shown) {
        editor.putBoolean(KEY_NOTIFICATION_PROMPT_SHOWN, shown).apply();
    }

    public boolean isPinPromptShown() {
        return prefs.getBoolean(KEY_PIN_PROMPT_SHOWN, false);
    }

    public void setPinPromptShown(boolean shown) {
        editor.putBoolean(KEY_PIN_PROMPT_SHOWN, shown).apply();
    }

    public boolean isFingerprintEnabled() {
        return prefs.getBoolean(KEY_FINGERPRINT_ENABLED, false);
    }

    public void setFingerprintEnabled(boolean enabled) {
        editor.putBoolean(KEY_FINGERPRINT_ENABLED, enabled).apply();
    }
}