package com.settlex.android.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.settlex.android.domain.model.UserModel;

/**
 * Temporary storage for user data when database operations fail.
 * Acts as a fallback for retrying failed saves (e.g., network issues).
 * Data is persisted locally via SharedPreferences until successfully synced.
 */
public class TempUserStoragePrefs {
    private static final String PREFS_NAME = "temp_user_data";
    private static final String KEY_USER_JSON = "user_json";

    private SharedPreferences prefs;

    public TempUserStoragePrefs(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves user data as JSON for later retry. Overwrites any existing temp data.
     */
    public void saveUser(UserModel user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        prefs.edit().putString(KEY_USER_JSON, json).apply();
    }

    /**
     * Retrieves unsaved user data (if any). Returns null if no pending data exists.
     */
    public UserModel getUser() {
        String json = prefs.getString(KEY_USER_JSON, null);
        if (json == null) return null;
        return new Gson().fromJson(json, UserModel.class);
    }

    public void clearUser() {
        prefs.edit().remove(KEY_USER_JSON).apply();
    }
}