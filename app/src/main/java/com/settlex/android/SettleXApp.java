package com.settlex.android;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.settlex.android.util.NetworkMonitor;

public class SettleXApp extends Application {

    private static SettleXApp instance;
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize global references
        instance = this;
        appContext = getApplicationContext();

        // Initialize Firebase Core SDK
        FirebaseApp.initializeApp(this);

        // Initialize network state monitor
        NetworkMonitor.startNetworkCallback();
    }

    /*-------------------------------
    Retrieve global app context
    -------------------------------*/
    public static Context getAppContext() {
        return appContext;
    }

    /*-------------------------------
    Retrieve application instance
    -------------------------------*/
    public static SettleXApp getInstance() {
        return instance;
    }
}