package com.settlex.android;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.firebase.FirebaseApp;
import com.settlex.android.data.enums.TransactionOperation;
import com.settlex.android.data.enums.TransactionStatus;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.network.NetworkMonitor;

/**
 * Core application class managing global dependencies and context.
 * Initializes essential services (Firebase, NetworkMonitor) at app launch.
 */
public class SettleXApp extends Application implements ViewModelStoreOwner {
    private static SettleXApp instance;
    private static Context appContext;
    private Aead aead;
    // The ViewModelStore that will hold our ViewModels for the application scope.
    private final ViewModelStore appViewModelStore = new ViewModelStore();

    public static SettleXApp getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public Aead getAead() {
        return aead;
    }

    // A getter for a shared instance of the ViewModel.
    public UserViewModel getSharedUserViewModel() {
        return new ViewModelProvider(this).get(UserViewModel.class);
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return appViewModelStore;
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

        // Transaction enums colors
        TransactionStatus.init(this);
        TransactionOperation.init(this);

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