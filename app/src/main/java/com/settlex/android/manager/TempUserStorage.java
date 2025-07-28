//package com.settlex.android.manager;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import com.google.gson.Gson;
//
//
//import com.settlex.android.data.model.UserModel;
//
//public class TempUserStorage {
//    private static final String PREFS_NAME = "temp_user_data";
//    private static final String KEY_USER_JSON = "user_json";
//
//    private SharedPreferences prefs;
//
//    public TempUserStorage(Context context) {
//        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//    }
//
//    public void saveUser(UserModel user) {
//        Gson gson = new Gson();
//        String json = gson.toJson(user);
//        prefs.edit().putString(KEY_USER_JSON, json).apply();
//    }
//
//    public UserModel getUser() {
//        String json = prefs.getString(KEY_USER_JSON, null);
//        if (json == null) return null;
//        return new Gson().fromJson(json, UserModel.class);
//    }
//
//    public void clearUser() {
//        prefs.edit().remove(KEY_USER_JSON).apply();
//    }
//}
//
