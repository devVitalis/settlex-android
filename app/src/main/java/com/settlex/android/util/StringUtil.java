package com.settlex.android.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for string formatting and masking
 */
public class StringUtil {

    // ====================== CONSTRUCTOR ======================
    private StringUtil() {
        // Prevent instantiation
    }

    // ====================== EMAIL UTIL ======================
    // Example: b***k@gmail.com)
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "";

        String[] parts = email.split("@");
        String emailPrefix = parts[0];
        String domain = parts[1];

        if (emailPrefix.length() <= 2) {
            return emailPrefix.charAt(0) + "***@" + domain;
        }

        String masked = emailPrefix.charAt(0) + "***" + emailPrefix.charAt(emailPrefix.length() - 1);
        return masked + "@" + domain;
    }

    // ====================== TEXT UTIL ======================
    // Example: "settle x" → "Settle X"
    public static String capitalizeEachWord(String input) {
        if (input == null || input.isEmpty()) return input;

        String[] words = input.toLowerCase().trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }

    // ====================== PHONE UTIL ======================

    /**
     * Normalizes Nigerian phone number to +234XXXX format
     * Accepts: 0X0, X0
     */
    public static String formatPhoneNumber(String phone) {
        if (phone == null) return null;

        if (phone.startsWith("0")) {
            phone = phone.substring(1);
        }

        return "+234" + phone;
    }

    // ====================== CURRENCY UTIL ======================

    /**
     * Formats amount to ₦ with commas e.g. 1500 → ₦1,500.00
     */
    public static String formatToNaira(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "NG"));
        return formatter.format(amount);
    }

    /**
     * Formats amount into a short Naira string
     * Examples:
     * 950 → ₦950
     * 1_500 → ₦1.5K
     * 1_000_000 → ₦1M
     */
    public static String formatToNairaShort(double amount) {
        String symbol = "₦";

        if (amount < 1_000) {
            return symbol + ((int) amount);
        } else if (amount < 1_000_000) {
            return symbol + trimTrailingZeros(amount / 1_000.0) + "K";
        } else if (amount < 1_000_000_000) {
            return symbol + trimTrailingZeros(amount / 1_000_000.0) + "M";
        } else {
            return symbol + trimTrailingZeros(amount / 1_000_000_000.0) + "B";
        }
    }

    // ====================== PRIVATE HELPERS ======================

    /**
     * Removes unnecessary trailing .0 from doubles
     * Example: 2.0 → 2
     */
    private static String trimTrailingZeros(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        } else {
            return String.format(Locale.ENGLISH, "%.1f", value);
        }
    }
}
