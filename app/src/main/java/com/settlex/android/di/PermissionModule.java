package com.settlex.android.di;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.settlex.android.data.local.PermissionPrefs;
import com.settlex.android.util.permission.NotificationPermission;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.scopes.ActivityScoped;

@Module
@InstallIn(ActivityComponent.class)
public class PermissionModule {

    @Provides
    @ActivityScoped
    public static NotificationPermission provideNotificationPermissionUtil(@ActivityContext Context context, PermissionPrefs prefs) {
        return new NotificationPermission((AppCompatActivity) context, prefs);
    }
}
