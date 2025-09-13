package com.settlex.android.di;

import com.settlex.android.data.local.SessionManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    public static SessionManager provideSessionManager() {
        return SessionManager.getInstance();
    }

}
