package com.settlex.android.ui.dashboard.model;

import android.app.Activity;

import androidx.annotation.IdRes;

/**
 * Holds reference to each service destination Activity/Fragment
 */
public class ServiceDestination {
    private Class<? extends Activity> activity;
    private Integer navDestinationId;

    // constructors
    public ServiceDestination(Class<? extends Activity> activity) {
        this.activity = activity;
    }

    public ServiceDestination(@IdRes int navDestinationId) {
        this.navDestinationId = navDestinationId;
    }

    // Getters
    public Class<? extends Activity> getActivity() {
        return activity;
    }

    public Integer getNavDestinationId() {
        return navDestinationId;
    }

    public boolean isActivity() {
        return activity != null;
    }

    public boolean isFragment() {
        return navDestinationId != null;
    }
}
