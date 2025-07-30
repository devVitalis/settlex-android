package com.settlex.android.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class StringUtil {

    /*-----------------------------------------------------
    Mask user's email for display (e.g., b***k@gmail.com)
    -----------------------------------------------------*/
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

    /*------------------------------------------------
    Capitalize each word(e.g., settle x -> Settle X)
    -------------------------------------------------*/
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


    /*----------------------------------------
     Format amount to ₦ with commas
     e.g. 1500 → ₦1,500.00
    ----------------------------------------*/
    public static String formatToNaira(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "NG"));
        return formatter.format(amount);
    }

    /*------------------------------------------------
    Format amount into short Naira string:
    e.g. 950 → ₦950, 1500 → ₦1.5K, 1_000_000 → ₦1M
    ------------------------------------------------*/
    public static String formatToNairaShort(double amount) {
        String symbol = "₦";

        if (amount < 1_000) {
            return symbol + ((int) amount);
        } else if (amount < 1_000_000) {
            double value = amount / 1_000.0;
            return symbol + trimTrailingZeros(value) + "K";
        } else if (amount < 1_000_000_000) {
            double value = amount / 1_000_000.0;
            return symbol + trimTrailingZeros(value) + "M";
        } else {
            double value = amount / 1_000_000_000.0;
            return symbol + trimTrailingZeros(value) + "B";
        }
    }

    /*--------------------------------------------
    Strip trailing .0 from doubles like 2.0 → 2
    --------------------------------------------*/
    private static String trimTrailingZeros(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        } else {
            return String.format(Locale.ENGLISH, "%.1f", value);
        }
    }
}
