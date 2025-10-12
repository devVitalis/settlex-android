package com.settlex.android.ui.dashboard.model;

import com.settlex.android.data.enums.TransactionServiceType;

/**
 * Data model representing a service item with display name and icon resource
 */
public class ServiceUiModel {
    private final String name;
    private final int iconResId;
    private final int cashbackPercentage;
    private final TransactionServiceType type;

    public ServiceUiModel(String name, int iconResId, int cashbackPercentage, TransactionServiceType type) {
        this.name = name;
        this.iconResId = iconResId;
        this.cashbackPercentage = cashbackPercentage;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getCashbackPercentage() {
        return cashbackPercentage;
    }

    public TransactionServiceType getType() {
        return type;
    }
}