package com.settlex.android.data.enums;

import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.settlex.android.R;

/**
 * Enum representing transaction status states with display names and color coding
 */
public enum TransactionStatus {
    PENDING("Pending", R.color.orange),
    SUCCESS("Successful", R.color.green),
    FAILED("Failed", R.color.red);

    private final String displayName;
    private int textColorRes;

    TransactionStatus(String displayName, @ColorRes int textColorRes) {
        this.displayName = displayName;
        this.textColorRes = textColorRes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColorRes() {
        return textColorRes;
    }

    public static void init(Context context) {
        for (TransactionStatus status : values()) {
            status.textColorRes = ContextCompat.getColor(context, status.textColorRes);
        }
    }
}