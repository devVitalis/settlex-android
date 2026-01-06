package com.settlex.android.util.string;

public final class StringFormatter {
    private StringFormatter() {
    }

    public static String removeAtInPaymentId(String paymentId) {
        if (paymentId == null || paymentId.isEmpty()) return paymentId;
        if (paymentId.startsWith("@")) return paymentId.substring(1);
        return paymentId;
    }

    public static String addAtToPaymentId(String paymentId) {
        if (paymentId == null || paymentId.isEmpty()) return paymentId;
        if (paymentId.startsWith("@")) return paymentId;
        return "@" + paymentId;
    }
}