package com.settlex.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import jakarta.inject.Singleton;

@Singleton
public class PermissionPrefs {
    private static final String PREF_NAME = "permission_prefs";
    private static final String KEY_NOTIFICATION_PROMPT_SHOWN = "notification_prompt_shown";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public PermissionPrefs(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // flags
    public boolean isNotificationPromptShown() {
        return prefs.getBoolean(KEY_NOTIFICATION_PROMPT_SHOWN, false);
    }

    public void setNotificationPromptShown(boolean shown) {
        editor.putBoolean(KEY_NOTIFICATION_PROMPT_SHOWN, shown).apply();
    }
}