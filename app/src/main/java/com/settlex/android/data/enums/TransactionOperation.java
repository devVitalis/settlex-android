package com.settlex.android.data.enums;

import com.settlex.android.R;

/**
 * Enum representing transaction types with symbols and color for UI display
 */
public enum TransactionOperation {
    CREDIT("+", R.color.green),
    DEBIT("-", R.color.red);

    private final String symbol;
    private final int symbolColorRes;

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
}