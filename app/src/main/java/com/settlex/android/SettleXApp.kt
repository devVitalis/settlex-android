package com.settlex.android

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.settlex.android.util.network.NetworkMonitor.startNetworkCallback
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class managing global dependencies and context.
 */
@HiltAndroidApp
class SettleXApp : Application() {

    companion object {
        lateinit var instance: SettleXApp
            private set

        val appContext: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initialize()
    }

    private fun initialize() {
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // starts essential background services
        FirebaseApp.initializeApp(this)
        startNetworkCallback()
    }
}