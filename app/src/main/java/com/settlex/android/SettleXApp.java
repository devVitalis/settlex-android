package com.settlex.android;

import android.app.Application;
import android.content.Context;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.firebase.FirebaseApp;
import com.settlex.android.util.network.NetworkMonitor;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Core application class managing global dependencies and context.
 * Initializes essential services (Firebase, NetworkMonitor) at app launch.
 */
@HiltAndroidApp
public class SettleXApp extends Application {
    private static SettleXApp instance;
    private static Context appContext;
    private Aead aead;

    public static SettleXApp getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public Aead getAead() {
        return aead;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeGlobals();
        initializeServices();
    }

    // INITIALIZATION
    // global application references
    private void initializeGlobals() {
        instance = this;
        appContext = getApplicationContext();
    }

    // Starts essential background services
    private void initializeServices() {
        FirebaseApp.initializeApp(this);
        NetworkMonitor.startNetworkCallback();

        // Initialize Tink
        try {
            AeadConfig.register();

            // Persist keyset in SharedPreferences protected by Android Keystore
            AndroidKeysetManager keysetManager =
                    new AndroidKeysetManager.Builder()
                            .withSharedPref(this, "master_keyset", "master_key_preference")
                            .withKeyTemplate(com.google.crypto.tink.aead.AeadKeyTemplates.AES256_GCM)
                            .withMasterKeyUri("android-keystore://settlex_master_key")
                            .build();

            KeysetHandle keysetHandle = keysetManager.getKeysetHandle();
            aead = keysetHandle.getPrimitive(Aead.class);

        } catch (Exception e) {
            throw new RuntimeException("Tink init failed", e);
        }
    }
}