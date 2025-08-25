package com.settlex.android.util.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.settlex.android.SettleXApp;

/**
 * Monitors network connectivity status and provides observable LiveData for network availability.
 * Call startNetworkCallback() once during application initialization to begin monitoring.
 */
public class NetworkMonitor {
    private static final MutableLiveData<Boolean> networkStatus = new MutableLiveData<>();

    private NetworkMonitor() {
        // Prevent instantiation
    }

    /**
     * Registers network callback to monitor connectivity changes
     * and provides initial status.
     */
    public static void startNetworkCallback() {
        Context context = SettleXApp.getAppContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                networkStatus.postValue(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                networkStatus.postValue(false);
            }

            @Override
            public void onUnavailable() {
                networkStatus.postValue(false);
            }
        };

        cm.registerDefaultNetworkCallback(callback);

        // Set initial network state
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        boolean isConnected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        networkStatus.setValue(isConnected);
    }

    /**
     * Returns LiveData observable for network connectivity status.
     * @return LiveData<Boolean> where true indicates internet connectivity is available
     */
    public static LiveData<Boolean> getNetworkStatus() {
        return networkStatus;
    }
}