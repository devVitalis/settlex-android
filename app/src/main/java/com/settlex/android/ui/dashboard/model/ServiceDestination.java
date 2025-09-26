package com.settlex.android.ui.dashboard.model;

/**
 * Holds reference to each service destination Activity/Fragment
 */
public class ServiceDestination {
    private Class<?> activity;
    private Integer navDestinationId;

    // Constructors
    public ServiceDestination(Class<?> activity) {
        this.activity = activity;
    }

    public ServiceDestination(Integer navDestinationId) {
        this.navDestinationId = navDestinationId;
    }

    // Getters
    public Class<?> getActivity() {
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
