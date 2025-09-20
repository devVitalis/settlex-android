package com.settlex.android.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class FirebaseModule {

    @Provides
    public static FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    public static FirebaseFirestore provideFirebaseFirestore() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    public static FirebaseFunctions provideFirebaseFunctions() {
        return FirebaseFunctions.getInstance("europe-west2");
    }

    @Provides
    public static FirebaseRemoteConfig provideFirebaseRemoteConfig() {
        return FirebaseRemoteConfig.getInstance();
    }
}
