package com.settlex.android;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.settlex.android.utils.network.NetworkMonitor;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Core application class managing global dependencies and context.
 */
@HiltAndroidApp
public class SettleXApp extends Application {
    private static SettleXApp instance;
    private static Context appContext;

    public static SettleXApp getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initializeGlobals();
        initializeServices();
    }

    private void initializeGlobals() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        instance = this;
        appContext = getApplicationContext();
    }

    private void initializeServices() {
        // starts essential background services
        FirebaseApp.initializeApp(this);
        NetworkMonitor.startNetworkCallback();
    }
}