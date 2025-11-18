package com.settlex.android.util.string;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;


public final class StringFormatter {
    private StringFormatter() {    }

    public static String setAsterisks() {
        return "****";
    }

    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) return email;

        String[] parts = email.split("@");
        String emailPrefix = parts[0];
        String domain = parts[1];

        if (emailPrefix.length() <= 2) {
            return emailPrefix.charAt(0) + "***@" + domain;
        }

        String masked = emailPrefix.charAt(0) + "***" + emailPrefix.charAt(emailPrefix.length() - 1);
        return masked + "@" + domain;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) return phone;

        final int prefixLength = 7;
        int suffixLength = 3;

        // Extract visible parts
        String prefix = phone.substring(0, prefixLength);
        String suffix = phone.substring(phone.length() - suffixLength);

        // Build masked section with asterisks
        int maskLength = phone.length() - (prefixLength + suffixLength);
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < maskLength; i++) {
            mask.append("*");
        }

        return prefix + mask + suffix;
    }

    public static String formatPhoneWithCode(String phone) {
        if (phone == null || phone.isEmpty()) return phone;
        if (phone.startsWith("0")) phone = phone.substring(1);
        return "+234" + phone;
    }

    public static String capitalizeEachWord(String input) {
        if (input == null || input.isEmpty()) return input;

        String[] words = input.toLowerCase().trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
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

    public static void copyToClipboard(Context context, String label, String text, boolean showToast) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, text);
        clipboardManager.setPrimaryClip(clipData);

        if (showToast && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show();
        }
    }

    public static void showNotImplementedToast(Context context) {
        Toast.makeText(context, "Feature not yet implemented", Toast.LENGTH_SHORT).show();
    }
}