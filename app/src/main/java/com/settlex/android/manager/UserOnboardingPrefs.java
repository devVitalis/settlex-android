package com.settlex.android.manager;

import android.content.Context;
import android.content.SharedPreferences;

/*--------------------------------------------------
Handles onboarding-related preferences

- Uses UID to scope preferences per user
- Only `introViewed` is global (non-user scoped)
--------------------------------------------------*/
public class UserOnboardingPrefs {

    private static final String PREF_NAME = "user_onboarding";
    private static final String KEY_NOTIFICATION_PROMPT_SHOWN = "onboard_notification_prompt_shown";
    private static final String KEY_PASSCODE_PROMPT_SHOWN = "onboard_passcode_prompt_shown";
    private static final String KEY_FINGERPRINT_ENABLED = "onboard_fingerprint_enabled";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    /*--------------------------------------------------
    Constructor with UID for user-scoped preferences
    Used after account creation (e.g., biometrics, prompts)
    --------------------------------------------------*/
    public UserOnboardingPrefs(Context context, String uid) {
        prefs = context.getSharedPreferences(PREF_NAME + "_" + uid, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /*--------------------------------------------
    Notification prompt shown (user-scoped)
    --------------------------------------------*/
    public boolean isNotificationPromptShown() {
        return prefs.getBoolean(KEY_NOTIFICATION_PROMPT_SHOWN, false);
    }

    public void setNotificationPromptShown(boolean shown) {
        editor.putBoolean(KEY_NOTIFICATION_PROMPT_SHOWN, shown).apply();
    }

    /*--------------------------------------------
    Passcode prompt shown (user-scoped)
    --------------------------------------------*/
    public boolean isPasscodePromptShown() {
        return prefs.getBoolean(KEY_PASSCODE_PROMPT_SHOWN, false);
    }

    public void setPasscodePromptShown(boolean shown) {
        editor.putBoolean(KEY_PASSCODE_PROMPT_SHOWN, shown).apply();
    }

    /*--------------------------------------------
    Fingerprint enabled status (user-scoped)
    --------------------------------------------*/
    public boolean isFingerprintEnabled() {
        return prefs.getBoolean(KEY_FINGERPRINT_ENABLED, false);
    }

    public void setFingerprintEnabled(boolean enabled) {
        editor.putBoolean(KEY_FINGERPRINT_ENABLED, enabled).apply();
    }
}