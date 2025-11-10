package com.settlex.android.di;

import android.content.Context;

import com.settlex.android.data.local.PermissionPrefs;
import com.settlex.android.utils.network.NetworkMonitor;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import jakarta.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public static PermissionPrefs providePermissionPrefs(@ApplicationContext Context context) {
        return new PermissionPrefs(context);
    }
}
