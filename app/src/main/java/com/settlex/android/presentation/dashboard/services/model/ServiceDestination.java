package com.settlex.android.presentation.dashboard.services.model;

import android.app.Activity;

import androidx.annotation.IdRes;

public class ServiceDestination {
    private Class<? extends Activity> activity;
    private Integer navDestinationId;

    public ServiceDestination(Class<? extends Activity> activity) {
        this.activity = activity;
    }

    public ServiceDestination(@IdRes int navDestinationId) {
        this.navDestinationId = navDestinationId;
    }

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
