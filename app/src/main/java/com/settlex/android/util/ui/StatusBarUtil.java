package com.settlex.android.util.ui;

import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowInsetsController;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

/**
 * Utility class for managing the status bar appearance.
 */
public class StatusBarUtil {

    private StatusBarUtil() {
        // Prevent instantiation
    }

    /**
     * Sets the status bar color and adjusts icon contrast accordingly.
     *
     * @param activity The target activity.
     * @param colorRes The color resource to apply to the status bar.
     */
    public static void setStatusBarColor(Activity activity, @ColorRes int colorRes) {
        if (activity == null) return;

        Window window = activity.getWindow();
        int color = ContextCompat.getColor(activity, colorRes);

        // Still required for backward compatibility
        window.setStatusBarColor(color);

        // Handle icon contrast (light vs dark icons)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                if (isColorLight(color)) {
                    // Light background → dark icons
                    insetsController.setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                } else {
                    // Dark background → light icons
                    insetsController.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            }
        }
    }

    /**
     * Simple luminance check to guess if a color is "light".
     */
    private static boolean isColorLight(int color) {
        double darkness = 1 - (0.299 * ((color >> 16) & 0xFF)
                + 0.587 * ((color >> 8) & 0xFF)
                + 0.114 * (color & 0xFF)) / 255;
        return darkness < 0.5; // lighter colors have lower darkness
    }
}
