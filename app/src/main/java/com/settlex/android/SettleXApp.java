package com.settlex.android;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.settlex.android.util.NetworkMonitor;

/**
 * Core application class managing global dependencies and context.
 * Initializes essential services (Firebase, NetworkMonitor) at app launch.
 */
public class SettleXApp extends Application {
    // SINGLETON PATTERN
    private static SettleXApp instance;
    private static Context appContext;

    public static SettleXApp getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    // LIFECYCLE
    @Override
    public void onCreate() {
        super.onCreate();
        initializeGlobals();
        initializeServices();
        NetworkMonitor.startNetworkCallback();
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
    }
}