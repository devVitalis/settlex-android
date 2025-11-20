package com.settlex.android.util.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import com.settlex.android.SettleXApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkMonitor {
    private val _networkStatus: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val networkStatus: StateFlow<Boolean> = _networkStatus.asStateFlow()

    fun startNetworkCallback() {
        val context = SettleXApp.appContext
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (cm == null) return

        val callback: NetworkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                _networkStatus.tryEmit(true)
            }

            override fun onLost(network: Network) {
                _networkStatus.tryEmit(false)
            }

            override fun onUnavailable() {
                _networkStatus.tryEmit(false)
            }
        }

        cm.registerDefaultNetworkCallback(callback)

        // Set initial network state
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        _networkStatus.value =
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}