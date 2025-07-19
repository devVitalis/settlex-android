package com.settlex.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.settlex.android.SettleXApp;

public class SessionManager {
    private static final String PREF_NAME = "settle_x_prefs";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_FIRSTNAME = "user_firstname";
    private static final String KEY_USER_LASTNAME = "user_lastname";
    private static final String KEY_HAS_PASSCODE = "has_passcode";
    private static SessionManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            Context appContext = SettleXApp.getInstance().getApplicationContext();
            instance = new SessionManager(appContext);
        }
        return instance;
    }

    public void cacheUserInfo(String uid, String email, String firstName, String lastName, boolean hasPasscode) {
        editor.putString(KEY_USER_UID, uid);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_FIRSTNAME, firstName);
        editor.putString(KEY_USER_LASTNAME, lastName);
        editor.putBoolean(KEY_HAS_PASSCODE, hasPasscode);
        editor.apply();
    }

    public String getUserUid() {
        return prefs.getString(KEY_USER_UID, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserFirstName() {
        return prefs.getString(KEY_USER_FIRSTNAME, null);
    }

    public String getUserLastName() {
        return prefs.getString(KEY_USER_LASTNAME, null);
    }

    public void setHasPasscode(boolean hasPasscode) {
        editor.putBoolean(KEY_HAS_PASSCODE, hasPasscode);
        editor.apply();
    }


    public boolean hasPasscode() {
        return prefs.getBoolean(KEY_HAS_PASSCODE, false);
    }

    public boolean isUserLoggedIn() {
        return getUserUid() != null;
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}