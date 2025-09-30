package com.settlex.android.data.local;

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

    public static synchronized UserPrefs getInstance() {
        if (instance == null) {
            instance = new UserPrefs(SettleXApp.getAppContext());
        }
        return instance;
    }

    // Balance state
    public boolean isBalanceHidden() {
        return prefs.getBoolean(KEY_HIDE_BALANCE, false);
    }

    public void setBalanceHidden(boolean hidden) {
        prefs.edit().putBoolean(KEY_HIDE_BALANCE, hidden).apply();
    }
}

