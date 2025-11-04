package com.settlex.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user scoped preferences
 */
public class UserPrefs {

    private static final String PREFS_NAME = "user_prefs_";
    private static final String KEY_HIDE_BALANCE = "hide_balance";
    private static final String KEY_ENABLE_FINGERPRINT = "enable_fingerprint";

    private final SharedPreferences prefs;

    public UserPrefs(Context context, String uid) {
        prefs = context.getSharedPreferences(PREFS_NAME + uid, Context.MODE_PRIVATE);
    }

    // Fingerprint state
    public boolean isFingerPrintEnabled() {
        return prefs.getBoolean(KEY_ENABLE_FINGERPRINT, false);
    }

    public void setFingerprintEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLE_FINGERPRINT, enabled).apply();
    }

    // Balance state
    public boolean isBalanceHidden() {
        return prefs.getBoolean(KEY_HIDE_BALANCE, false);
    }

    public void setBalanceHidden(boolean hidden) {
        prefs.edit().putBoolean(KEY_HIDE_BALANCE, hidden).apply();
    }
}
