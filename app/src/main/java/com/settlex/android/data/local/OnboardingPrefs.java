package com.settlex.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages device-wide onboarding state.
 * Tracks if the app intro was ever viewed on this device.
 */
public class OnboardingPrefs {
    public static final String PREF_NAME = "onboarding";
    public static final String KEY_NAME_INTRO_VIEWED = "intro_viewed";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public OnboardingPrefs(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // (Device-global) ---
    public void setIntroViewed(boolean viewed) {
        editor.putBoolean(KEY_NAME_INTRO_VIEWED, viewed).apply();
    }

    public boolean isIntroViewed() {
        return prefs.getBoolean(KEY_NAME_INTRO_VIEWED, false);
    }
}