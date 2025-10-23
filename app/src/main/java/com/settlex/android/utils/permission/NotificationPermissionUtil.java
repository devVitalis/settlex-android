package com.settlex.android.utils.permission;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.settlex.android.data.local.PermissionPrefs;

import jakarta.inject.Inject;

/**
 * Handles runtime permission for notifications (Android 13+).
 */
public class NotificationPermissionUtil {
    private final PermissionPrefs permissionPrefs;
    private final AppCompatActivity activity;
    private ActivityResultLauncher<String> notificationLauncher;

    @Inject
    public NotificationPermissionUtil(@NonNull AppCompatActivity activity, @NonNull PermissionPrefs permissionPrefs) {
        this.activity = activity;
        this.permissionPrefs = permissionPrefs;
    }

    /**
     * Must be called in onCreate() before requesting permission.
     */
    public void initNotificationLauncher(@NonNull Runnable onGranted, @NonNull Runnable onDenied) {
        notificationLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    permissionPrefs.setNotificationPromptShown(true);
                    if (isGranted) {
                        onGranted.run();
                        return;
                    }
                    onDenied.run();
                }
        );
    }

    public boolean shouldRequestPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
                && !permissionPrefs.isNotificationPromptShown();
    }

    /**
     * Request POST_NOTIFICATIONS permission (Android 13+ only).
     */
    public void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}
