package com.settlex.android.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.settlex.android.di.AppPrefs
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Manages device-wide preferences, not tied to a specific user.
 */
@Singleton
class AppPrefs @Inject constructor(
    @param:AppPrefs private val prefs: SharedPreferences
) {

    var isIntroViewed: Boolean
        get() = prefs.getBoolean(KEY_NAME_INTRO_VIEWED, false)
        set(viewed) = prefs.edit { putBoolean(KEY_NAME_INTRO_VIEWED, viewed) }


    companion object {
        const val KEY_NAME_INTRO_VIEWED: String = "intro_viewed"
    }
}