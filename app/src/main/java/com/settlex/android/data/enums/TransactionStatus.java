package com.settlex.android.data.enums;

import com.settlex.android.R;

/**
 * Enum representing transaction status states with display names and color coding
 */
public enum TransactionStatus {
    PENDING("Pending", R.attr.colorOnWarningContainer, R.drawable.bg_label_status_pending),
    REVERSED("Reversed", com.google.android.material.R.attr.colorOnSecondary, R.drawable.bg_label_status_reversed),
    SUCCESS("Successful", R.attr.colorOnSuccessContainer, R.drawable.bg_label_status_success),
    FAILED("Failed", com.google.android.material.R.attr.colorOnErrorContainer, R.drawable.bg_label_status_failed);

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