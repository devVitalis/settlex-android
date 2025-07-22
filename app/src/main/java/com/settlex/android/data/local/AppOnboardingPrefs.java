package com.settlex.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class AppOnboardingPrefs {

    public static final String PREF_NAME = "app_onboarding";
    public static final String KEY_NAME_INTRO_VIEWED = "intro_viewed";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    /*-----------------------------------------
    Constructor for app-wide (non-user) flags
    ------------------------------------------*/
    public AppOnboardingPrefs(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /*---------------------------------------
    Intro screen viewed flag (device-wide)
    ---------------------------------------*/
    public void setIntroViewed(boolean viewed) {
        editor.putBoolean(KEY_NAME_INTRO_VIEWED, viewed).apply();
    }

    public boolean isIntroViewed() {
        return prefs.getBoolean(KEY_NAME_INTRO_VIEWED, false);
    }
}
