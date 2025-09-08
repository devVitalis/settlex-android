package com.settlex.android;

import android.app.Application;
import android.content.Context;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;
import com.google.firebase.FirebaseApp;
import com.settlex.android.data.enums.TransactionOperation;
import com.settlex.android.data.enums.TransactionStatus;
import com.settlex.android.util.network.NetworkMonitor;

/**
 * Core application class managing global dependencies and context.
 * Initializes essential services (Firebase, NetworkMonitor) at app launch.
 */
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

    // LIFECYCLE
    @Override
    public void onCreate() {
        super.onCreate();
        initializeGlobals();
        initializeServices();
    }

    // INITIALIZATION

    /**
     * Sets up global application references
     */
    private void initializeGlobals() {
        instance = this;
        appContext = getApplicationContext();
    }

    /**
     * Starts essential background services
     */
    private void initializeServices() {
        FirebaseApp.initializeApp(this);
        NetworkMonitor.startNetworkCallback();
        TransactionStatus.init(this);
        TransactionOperation.init(this);

        try {
            AeadConfig.register(); // Initialize Tink
            KeysetHandle keysetHandle = KeysetHandle.generateNew(
                    com.google.crypto.tink.aead.AeadKeyTemplates.AES256_GCM
            );
            aead = AeadFactory.getPrimitive(keysetHandle);
        } catch (Exception e) {
            throw new RuntimeException("Tink init failed", e);
        }
    }
}