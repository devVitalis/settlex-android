package com.settlex.android.ui.dashboard.model;

import com.settlex.android.data.enums.TransactionServiceType;

/**
 * Data model representing a service item with display name and icon resource
 */
public class ServiceUiModel {
    private final String name;
    private final int iconResId;
    private final TransactionServiceType type;

    public ServiceUiModel(String name, int iconResId, TransactionServiceType type) {
        this.name = name;
        this.iconResId = iconResId;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public TransactionServiceType getType() {
        return type;
    }
}