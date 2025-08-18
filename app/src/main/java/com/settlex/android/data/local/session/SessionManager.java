package com.settlex.android.data.local.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.settlex.android.SettleXApp;
import com.settlex.android.data.local.UserOnboardPrefs;

/**
 * Manages the active user session and user-scoped preferences.
 * Persists critical user data (UID, email, etc.) and handles onboarding state.
 */
public class SessionManager {

    // SharedPreferences keys for session data
    private static final String PREF_NAME = "session_prefs";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_FIRSTNAME = "user_firstname";
    private static final String KEY_USER_LASTNAME = "user_lastname";
    private static final String KEY_HAS_PIN = "has_pin";

    private static SessionManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private UserOnboardPrefs onboardingPrefs;

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();

        // Initialize user-scoped prefs if UID exists
        String uid = getUserUid();
        if (uid != null) {
            onboardingPrefs = new UserOnboardPrefs(context, uid);
        }
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            Context appContext = SettleXApp.getInstance().getApplicationContext();
            instance = new SessionManager(appContext);
        }
        return instance;
    }

    /**
     * Caches core user data and initializes user-specific preferences.
     * Called after successful login or profile update.
     */
    public void cacheUserInfo(String uid, String email, String firstName, String lastName, boolean hasPin) {
        editor.putString(KEY_USER_UID, uid);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_FIRSTNAME, firstName);
        editor.putString(KEY_USER_LASTNAME, lastName);
        editor.putBoolean(KEY_HAS_PIN, hasPin);
        editor.apply();

        onboardingPrefs = new UserOnboardPrefs(SettleXApp.getInstance(), uid);
    }

    // --- Session state checks ---
    public boolean isUserLoggedIn() {
        return getUserUid() != null;
    }

    public boolean hasPin() {
        return prefs.getBoolean(KEY_HAS_PIN, false);
    }

    // --- Getters ---
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

    public void setHasPin(boolean hasPin) {
        editor.putBoolean(KEY_HAS_PIN, hasPin);
        editor.apply();
    }

    /**
     * Provides access to user-specific onboarding preferences.
     */
    public UserOnboardPrefs getOnboardingPrefs() {
        return onboardingPrefs;
    }

    /**
     * Clears all session data and resets onboarding state.
     * Called during logout or session expiration.
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
        onboardingPrefs = null;
    }
}