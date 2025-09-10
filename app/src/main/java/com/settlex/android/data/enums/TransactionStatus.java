package com.settlex.android.data.enums;

import com.settlex.android.R;

/**
 * Enum representing transaction status states with display names and color coding
 */
public enum TransactionStatus {
    PENDING("Pending", R.color.orange, R.drawable.bg_3dp_orange_light),
    REVERSED("Reversed", R.color.blue, R.drawable.bg_3dp_blue_light),
    SUCCESS("Successful", R.color.green, R.drawable.bg_3dp_green_light),
    FAILED("Failed", R.color.red, R.drawable.bg_3dp_red_light);

    private final String displayName;
    private final int textColorRes;
    private final int bgColorRes;

    TransactionStatus(String displayName, int textColorRes, int bgColorRes) {
        this.displayName = displayName;
        this.textColorRes = textColorRes;
        this.bgColorRes = bgColorRes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColorRes() {
        return textColorRes;
    }

    public int getBgColorRes() {
        return bgColorRes;
    }
}