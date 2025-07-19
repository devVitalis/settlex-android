package com.settlex.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/*-------------------------------------------------
Handles onboarding-related preferences
- These prefs are cached per user (scoped by UID)
--------------------------------------------------*/
public class UserOnboardingPrefs {

    private static final String PREFS_PREFIX = "user_onboarding_prefs";
    private static final String KEY_NOTIFICATION_PROMPT_SHOWN = "notification_prompt_shown";
    private static final String KEY_PASSCODE_PROMPT_SHOWN = "passcode_prompt_shown";
    private static final String KEY_FINGERPRINT_ENABLED = "fingerprint_enabled";
    private static final String KEY_INTRO_VIEWED = "intro_viewed";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    /*--------------------------------------------------
    Constructor with a UID to create user-scoped prefs
    --------------------------------------------------*/
    public UserOnboardingPrefs(Context context, String uid) {
        prefs = context.getSharedPreferences(PREFS_PREFIX + uid, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /*-------------------------------------------------
    Constructor without a UID to manage IntroViewed ?
    -------------------------------------------------*/
    public UserOnboardingPrefs(Context context) {
        prefs = context.getSharedPreferences(PREFS_PREFIX, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public boolean isNotificationPromptShown() {
        return prefs.getBoolean(KEY_NOTIFICATION_PROMPT_SHOWN, false);
    }

    public void setNotificationPromptShown() {
        editor.putBoolean(KEY_NOTIFICATION_PROMPT_SHOWN, true).apply();
    }

    public boolean isPasscodePromptShown() {
        return prefs.getBoolean(KEY_PASSCODE_PROMPT_SHOWN, false);
    }

    public void setPasscodePromptShown() {
        editor.putBoolean(KEY_PASSCODE_PROMPT_SHOWN, true).apply();
    }

    /*------------------------------
    Fingerprint auth setup status
    ------------------------------*/
    public boolean isFingerprintEnabled() {
        return prefs.getBoolean(KEY_FINGERPRINT_ENABLED, false);
    }

    public void setFingerprintEnabled(boolean enabled) {
        editor.putBoolean(KEY_FINGERPRINT_ENABLED, enabled).apply();
    }

    /*--------------------------------------------
    Intro Viewed? Status
    --------------------------------------------*/
    public boolean isIntroViewed() {
        return prefs.getBoolean(KEY_INTRO_VIEWED, false);
    }

    public void setIntroViewed() {
        editor.putBoolean(KEY_INTRO_VIEWED, true).apply();
    }
}