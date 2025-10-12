package com.settlex.android.di;

import com.settlex.android.data.local.UserPrefs;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import jakarta.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public static UserPrefs provideUserPrefs() {
        return UserPrefs.getInstance();
    }

}
