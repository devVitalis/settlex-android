package com.settlex.android.util.string;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.google.firebase.Timestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Utility class for string formatting and data transformations
 */
public class StringUtil {

    private StringUtil() {
        // Prevent instantiation
    }

    // EMAIL MASKING ===========
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

    // TEXT FORMATTING ===========
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

    public static String setAsterisks() {
        return "****";
    }

    public static void copyToClipboard(Context context, String label, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, text);
        clipboardManager.setPrimaryClip(clipData);
    }

    // PHONE NUMBER FORMATTING ===========
    public static String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) return phone;

        if (phone.startsWith("0")) phone = phone.substring(1);

        return "+234" + phone;
    }

    // CURRENCY FORMATTING =============
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

    /**
     * Converts a long amount (in cents/kobo) to a safely formatted Naira currency string
     * using BigDecimal for precision and NumberFormat for locale-specific display.
     *
     * @param amountInCents The monetary value stored as a long (e.g., 1299L for N12.99).
     * @return A formatted currency string (e.g., "₦12.99").
     */
    public static String formatLongCentsToNaira(long amountInCents) {
        // 1. Convert long cents to a precise BigDecimal
        BigDecimal bdCents = new BigDecimal(amountInCents);

        // 2. Divide by 100 to get the Naira value, maintaining 2 decimal places.
        //    We use RoundingMode.UNNECESSARY because dividing an integer by 100
        //    will not introduce new rounding issues.
        BigDecimal finalAmount = bdCents.divide(
                new BigDecimal("100"),
                2,
                RoundingMode.UNNECESSARY
        );

        // 3. Format the precise BigDecimal into the Naira currency format
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "NG"));

        return formatter.format(finalAmount);
    }

    /**
     * Converts a user-provided string (e.g., "100.60") into a long value (in cents/kobo)
     * using BigDecimal for accurate parsing and rounding.
     *
     * @param amountString The user's input string (e.g., "100.60").
     * @return The monetary value as a long in cents (e.g., 10060L).
     * @throws NumberFormatException If the string cannot be parsed as a valid number.
     */
    public static long convertNairaStringToLongCents(String amountString) throws NumberFormatException {
        if (amountString == null || amountString.trim().isEmpty()) {
            throw new NumberFormatException("Input string cannot be empty or null.");
        }

        try {
            // 1. Safely parse the user's string using the BigDecimal(String) constructor.
            BigDecimal amountBD = new BigDecimal(amountString.trim());

            // 2. Multiply by 100 to shift the decimal point (get cents)
            BigDecimal amountInCentsBD = amountBD.multiply(new BigDecimal("100"));

            // 3. Round to 0 decimal places (the nearest whole cent/kobo) using HALF_UP
            BigDecimal roundedCents = amountInCentsBD.setScale(0, RoundingMode.HALF_UP);

            // 4. Safely extract the long value. longValueExact() throws an exception
            //    if the value is too large for a long, adding an extra layer of safety.
            return roundedCents.longValueExact();

        } catch (ArithmeticException e) {
            // Handle issues like too many decimal places after rounding or value overflow
            throw new NumberFormatException("Invalid currency value: " + amountString);
        } catch (NumberFormatException e) {
            // Re-throw if the initial BigDecimal parsing failed
            throw new NumberFormatException("Invalid number format for currency: " + amountString);
        }
    }


    // DATE FORMATTING ==========
    public static String formatTimeStampToSimpleDate(Timestamp timestamp) {
        return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
                .format(timestamp.toDate());
    }
}