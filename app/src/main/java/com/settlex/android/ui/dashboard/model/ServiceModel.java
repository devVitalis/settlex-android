package com.settlex.android.ui.dashboard.model;

/**
 * Data model representing a service item with display name and icon resource
 */
public class ServiceModel {
    private final String name;
    private final int iconResId;

    public ServiceModel(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }
}