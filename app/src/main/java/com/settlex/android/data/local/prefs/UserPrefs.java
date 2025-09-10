package com.settlex.android.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.settlex.android.SettleXApp;

public class UserPrefs {

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_HIDE_BALANCE = "hide_balance";
    private final SharedPreferences prefs;
    private static UserPrefs instance;

    private UserPrefs(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Singleton accessor
    public static synchronized UserPrefs getInstance() {
        if (instance == null) {
            // Use app context to avoid leaking an Activity
            instance = new UserPrefs(SettleXApp.getAppContext());
        }
        return instance;
    }

    // --- Hide balance flag ---
    public boolean isBalanceHidden() {
        return prefs.getBoolean(KEY_HIDE_BALANCE, false);
    }

    public void setBalanceHidden(boolean hidden) {
        prefs.edit().putBoolean(KEY_HIDE_BALANCE, hidden).apply();
    }

}

