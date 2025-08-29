package com.settlex.android.data.enums;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.settlex.android.R;

/**
 * Enum representing transaction types with symbols and color for UI display
 */
public enum TransactionOperation {
    CREDIT("+", R.color.green),
    DEBIT("-", R.color.red),
    REFUND("+", R.color.green);

    private final String symbol;
    private int symbolColorRes;

    TransactionOperation(String symbol, int symbolColorRes) {
        this.symbol = symbol;
        this.symbolColorRes = symbolColorRes;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getColorRes() {
        return symbolColorRes;
    }

    public static void init(Context context) {
        for (TransactionOperation type : values()) {
            type.symbolColorRes = ContextCompat.getColor(context, type.symbolColorRes);
        }
    }
}