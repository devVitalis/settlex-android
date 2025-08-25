package com.settlex.android.util.string;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for string formatting, masking, and data transformations
 */
public class StringUtil {

    private StringUtil() {
        // Prevent instantiation
    }

    // ====================== EMAIL MASKING ======================
    public static String maskEmail(String email) {
        if (email == null) return "";

        String[] parts = email.split("@");
        String emailPrefix = parts[0];
        String domain = parts[1];

        // Handle short prefixes differently
        if (emailPrefix.length() <= 2) {
            return emailPrefix.charAt(0) + "***@" + domain;
        }

        String masked = emailPrefix.charAt(0) + "***" + emailPrefix.charAt(emailPrefix.length() - 1);
        return masked + "@" + domain;
    }

    // ====================== TEXT FORMATTING ======================
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

    // ====================== PHONE NUMBER FORMATTING ======================
    public static String formatPhoneNumber(String phone) {
        if (phone == null) return null;

        // Remove leading zero from numbers
        if (phone.startsWith("0")) {
            phone = phone.substring(1);
        }

        return "+234" + phone;
    }

    // ====================== CURRENCY FORMATTING ======================
    public static String formatToNaira(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "NG"));
        return formatter.format(amount);
    }

    public static String formatToNairaShort(double amount) {
        String symbol = "₦";

        if (amount < 1_000) {
            // Show up to 2 decimals for small amounts
            return symbol + new DecimalFormat("#.##").format(amount);
        } else if (amount < 1_000_000) {
            return symbol + new DecimalFormat("#.##").format(amount / 1_000.0) + "K";
        } else if (amount < 1_000_000_000) {
            return symbol + new DecimalFormat("#.##").format(amount / 1_000_000.0) + "M";
        } else {
            return symbol + new DecimalFormat("#.##").format(amount / 1_000_000_000.0) + "B";
        }
    }

    // ====================== DATE FORMATTING ======================
    public static String formatTimeStamp(long dateTime) {
        return new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)
                .format(new Date(dateTime));
    }
}