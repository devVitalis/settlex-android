package com.settlex.android.util.ui

import android.app.Activity
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.settlex.android.presentation.common.extensions.getThemeColor

/**
 * Utility class for managing the status bar appearance.
 */
object StatusBar {

    @JvmStatic
    fun setColor(activity: Activity, @AttrRes colorRes: Int) {
        val window = activity.window
        val color = activity.getThemeColor(colorRes)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = color

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = isColorLight(color)
    }

    /**
     * Simple luminance check to guess if a color is "light".
     */
    private fun isColorLight(color: Int): Boolean {
        val darkness =
            1 - (0.299 * ((color shr 16) and 0xFF) + 0.587 * ((color shr 8) and 0xFF) + 0.114 * (color and 0xFF)) / 255
        return darkness < 0.5 // lighter colors have lower darkness
    }
}
