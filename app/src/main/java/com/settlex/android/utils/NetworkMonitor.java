package com.settlex.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.settlex.android.SettleXApp;

public class NetworkMonitor {

    /*------------------------------------
    MutableLiveData holding network state
    -------------------------------------*/
    private static final MutableLiveData<Boolean> networkStatus = new MutableLiveData<>();

    /*------------------------------------
    Start observing network changes
    -------------------------------------*/
    public static void startNetworkCallback() {
        Context context = SettleXApp.getAppContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        /*------------------------------------
        Register default network callback
        -------------------------------------*/
        ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                networkStatus.postValue(true);
            }

            @Override
            public void onLost(Network network) {
                networkStatus.postValue(false);
            }

            @Override
            public void onUnavailable() {
                networkStatus.postValue(false);
            }
        };
        cm.registerDefaultNetworkCallback(callback);

        /*------------------------------------
        Emit initial network state immediately
        -------------------------------------*/
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        boolean isConnected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        networkStatus.setValue(isConnected);
    }

    /*------------------------------------
    Get LiveData to observe status
    -------------------------------------*/
    public static LiveData<Boolean> getNetworkStatus() {
        return networkStatus;
    }
}