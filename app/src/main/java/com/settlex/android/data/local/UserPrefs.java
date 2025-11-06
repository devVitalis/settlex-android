package com.settlex.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user scoped preferences
 */
public class UserPrefs {

    private static final String PREFS_NAME = "user_prefs_";
    private static final String KEY_HIDE_BALANCE = "hide_balance";
    private static final String KEY_PAY_BIOMETRIC_ENABLED = "pay_biometric_enabled";
    private static final String KEY_LOGIN_BIOMETRIC_ENABLED = "login_biometric_enabled";

    private final SharedPreferences prefs;

    public UserPrefs(Context context, String uid) {
        prefs = context.getSharedPreferences(PREFS_NAME + uid, Context.MODE_PRIVATE);
    }

    // Biometric state
    public boolean isPayBiometricsEnabled() {
        return prefs.getBoolean(KEY_PAY_BIOMETRIC_ENABLED, false);
    }

    public void setPayBiometricsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PAY_BIOMETRIC_ENABLED, enabled).apply();
    }

    public boolean isLoginBiometricsEnabled() {
        return prefs.getBoolean(KEY_LOGIN_BIOMETRIC_ENABLED, false);
    }

    public void setLoginBiometricsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_LOGIN_BIOMETRIC_ENABLED, enabled).apply();
    }

    // Balance state
    public boolean isBalanceHidden() {
        return prefs.getBoolean(KEY_HIDE_BALANCE, false);
    }

    public void setBalanceHidden(boolean hidden) {
        prefs.edit().putBoolean(KEY_HIDE_BALANCE, hidden).apply();
    }
}
