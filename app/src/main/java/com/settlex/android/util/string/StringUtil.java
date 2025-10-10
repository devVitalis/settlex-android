package com.settlex.android.util.string;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.Timestamp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Utility class for string formatting and data transformations
 */
public class StringUtil {
    private static final String TAG = StringUtil.class.getSimpleName();

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
    public static String formatPhoneNumberWithCountryCode(String phone) {
        if (phone == null || phone.isEmpty()) return phone;

        if (phone.startsWith("0")) phone = phone.substring(1);

        return "+234" + phone;
    }

    // CURRENCY FORMATTING =============

    /**
     * Formats kobo (long) into ₦X,XXX.XX
     */
    public static String formatToNaira(long amountInLongCent) {
        BigDecimal kobo = BigDecimal.valueOf(amountInLongCent);
        BigDecimal naira = kobo.divide(BigDecimal.valueOf(100), 2, RoundingMode.UNNECESSARY);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "NG"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        return formatter.format(naira);
    }

    public static String formatToCurrency(BigDecimal numericValue) {
        Locale nigerianLocal = Locale.forLanguageTag("en-NG");
        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(nigerianLocal);

        String formattedAmount = numberFormatter.format(numericValue);
        String symbol = Objects.requireNonNull(numberFormatter.getCurrency()).getSymbol(nigerianLocal);

        return formattedAmount.replace(symbol, "").trim();
    }

    /**
     * Formats kobo (long) into ₦X.XK / ₦X.XM / ₦X.XB
     */
    public static String formatToNairaShort(long amountInLongCents) {
        String symbol = "₦";
        DecimalFormat df = new DecimalFormat("#.##");
        BigDecimal naira = BigDecimal.valueOf(amountInLongCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (naira.compareTo(BigDecimal.valueOf(1_000)) < 0) {
            return symbol + naira.toPlainString(); // ₦999.99

        } else if (naira.compareTo(BigDecimal.valueOf(1_000_000)) < 0) {
            BigDecimal thousands = naira.divide(BigDecimal.valueOf(1_000), 1, RoundingMode.HALF_UP);
            return symbol + df.format(thousands) + "K";

        } else if (naira.compareTo(BigDecimal.valueOf(1_000_000_000)) < 0) {
            BigDecimal millions = naira.divide(BigDecimal.valueOf(1_000_000), 1, RoundingMode.HALF_UP);
            return symbol + df.format(millions) + "M";

        } else {
            BigDecimal billions = naira.divide(BigDecimal.valueOf(1_000_000_000), 1, RoundingMode.HALF_UP);
            return symbol + df.format(billions) + "B";
        }
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

    public static String compressAndConvertImageToBase64(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI");
                return null;
            }

            // Scale while keeping aspect ratio (max 512px for width/height)
            int maxSize = 512;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float scale = Math.min((float) maxSize / width, (float) maxSize / height);

            int scaledWidth = Math.round(width * scale);
            int scaledHeight = Math.round(height * scale);

            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

            // Compress Bitmap to JPEG with ~70% quality
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

            // Convert compressed bytes to Base64 string
            byte[] compressedBytes = outputStream.toByteArray();
            return Base64.encodeToString(compressedBytes, Base64.NO_WRAP);

        } catch (Exception e) {
            Log.e(TAG, "Error compressing image: " + e.getMessage(), e);
            return null;
        }
    }
}