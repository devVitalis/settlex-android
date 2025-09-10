package com.settlex.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.crypto.tink.Aead;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.settlex.android.SettleXApp;
import com.settlex.android.data.local.prefs.UserOnboardPrefs;
import com.settlex.android.ui.dashboard.model.UserUiModel;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

/**
 * Manages the active user session.
 * Stores user data securely using Tink AEAD encryption and SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "session_prefs";
    private static final String KEY_USER = "user";
    private static final String KEY_HAS_PIN = "has_pin";

    private static SessionManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Aead aead;
    private final Gson gson;

    private UserOnboardPrefs onboardingPrefs;

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        aead = SettleXApp.getInstance().getAead(); // AEAD instance from Application class
        gson = new Gson();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager(SettleXApp.getInstance().getApplicationContext());
        }
        return instance;
    }

    /**
     * Save user securely in SharedPreferences
     */
    public void cacheUserData(UserUiModel user) {
        try {
            String json = gson.toJson(user);
            byte[] encrypted = aead.encrypt(json.getBytes(StandardCharsets.UTF_8), null);
            String encoded = Base64.getEncoder().encodeToString(encrypted);

            prefs.edit().putString(KEY_USER, encoded).apply();

            // Setup user-scoped onboarding prefs
            onboardingPrefs = new UserOnboardPrefs(SettleXApp.getInstance(), user.getUid());

        } catch (Exception ignored) {
        }
    }

    /**
     * Get the cached user, decrypting it
     */
    public UserUiModel getUser() {
        try {
            String encoded = prefs.getString(KEY_USER, null);
            if (encoded == null) return null;

            byte[] decrypted = aead.decrypt(Base64.getDecoder().decode(encoded), null);
            String json = new String(decrypted, StandardCharsets.UTF_8);

            return gson.fromJson(json, UserUiModel.class);

        } catch (GeneralSecurityException | JsonSyntaxException e) {
            Log.e("SessionManager", "Failed to decrypt or parse user data", e);
            return null;
        }
    }

    // --- Session state checks ---
    public boolean isUserLoggedIn() {
        return getUser() != null && getUser().getUid() != null;
    }

    public boolean hasPin() {
        // return getUser()!= null && getUser().getHasPin();
        return prefs.getBoolean(KEY_HAS_PIN, false);
    }

    /**
     * Clear session data
     */
    public void clearSession() {
        prefs.edit().clear().apply();
        onboardingPrefs = null;
    }
}
