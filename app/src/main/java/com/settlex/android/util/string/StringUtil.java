package com.settlex.android.util.string;

import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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

    public static String removeAtInUsername(String username) {
        if (username == null || username.isEmpty()) return username;

        if (username.startsWith("@")) return username.substring(1).toLowerCase();

        return username;
    }

    public static String addAtToUsername(String username) {
        if (username == null || username.isEmpty()) return username;

        if (username.startsWith("@")) return username.toLowerCase();

        return "@" + username;
    }

    public static String setAsterisks(){
        return "****";
    }

    // ====================== PHONE NUMBER FORMATTING ======================
    public static String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) return phone;

        if (phone.startsWith("0")) phone = phone.substring(1);

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
    public static String formatTimeStamp(Timestamp timestamp) {
        return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
                .format(timestamp.toDate());
    }
}