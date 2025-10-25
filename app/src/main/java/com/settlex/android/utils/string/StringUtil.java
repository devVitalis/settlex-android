package com.settlex.android.utils.string;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.Timestamp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Utility class for string formatting and data transformations.
 * Central place for formatting money, usernames, timestamps, etc.
 */
public class StringUtil {
    private static final String TAG = StringUtil.class.getSimpleName();

    private StringUtil() {
        // Prevent instantiation
    }

    /**
     * Masks email for privacy, e.g. johndoe@gmail.com → j***e@gmail.com
     */
    public static String maskEmail(String email) {
        if (email == null) return "";

        String[] parts = email.split("@");
        String emailPrefix = parts[0];
        String domain = parts[1];

        if (emailPrefix.length() <= 2) {
            return emailPrefix.charAt(0) + "***@" + domain;
        }

        String masked = emailPrefix.charAt(0) + "***" + emailPrefix.charAt(emailPrefix.length() - 1);
        return masked + "@" + domain;
    }

    /**
     * Capitalizes each word: "john doe" → "John Doe"
     */
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

    /**
     * Removes leading '@' from Payment ID, if present
     */
    public static String removeAtInPaymentId(String paymentId) {
        if (paymentId == null || paymentId.isEmpty()) return paymentId;
        if (paymentId.startsWith("@")) return paymentId.substring(1);
        return paymentId;
    }

    /**
     * Ensures Payment ID starts with '@'
     */
    public static String addAtToPaymentId(String paymentId) {
        if (paymentId == null || paymentId.isEmpty()) return paymentId;
        if (paymentId.startsWith("@")) return paymentId;
        return "@" + paymentId;
    }

    public static String setAsterisks() {
        return "****";
    }

    /**
     * Copies text to clipboard, optionally showing a Toast (only for API < 33)
     */
    public static void copyToClipboard(Context context, String label, String text, boolean showToast) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, text);
        clipboardManager.setPrimaryClip(clipData);

        if (showToast && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Converts Nigerian phone numbers (starting with 0) to +234 format
     */
    public static String formatPhoneNumberWithCountryCode(String phone) {
        if (phone == null || phone.isEmpty()) return phone;
        if (phone.startsWith("0")) phone = phone.substring(1);
        return "+234" + phone;
    }

    /**
     * Converts Kobo (long) to a formatted Naira currency string, e.g. 123450 → ₦1,234.50
     */
    public static String formatToNaira(long amountInKobo) {
        BigDecimal kobo = BigDecimal.valueOf(amountInKobo);
        BigDecimal naira = kobo.divide(BigDecimal.valueOf(100), 2, RoundingMode.UNNECESSARY);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "NG"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        return formatter.format(naira);
    }

    /**
     * Formats a BigDecimal into local currency string without ₦ symbol
     */
    public static String formatToCurrency(BigDecimal number) {
        Locale NIG_LOCAL = Locale.forLanguageTag("en-NG");
        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(NIG_LOCAL);

        String formattedAmount = numberFormatter.format(number);
        String symbol = Objects.requireNonNull(numberFormatter.getCurrency()).getSymbol(NIG_LOCAL);

        return formattedAmount.replace(symbol, "").trim();
    }

    /**
     * Converts large amounts to readable short forms, e.g. ₦2.5K / ₦3.2M / ₦1.1B
     */
    public static String formatToNairaShort(long amountInKobo) {
        String symbol = "₦";
        DecimalFormat df = new DecimalFormat("#.##");
        BigDecimal naira = BigDecimal.valueOf(amountInKobo).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

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
     * Converts a Naira string like "120.50" to its equivalent Kobo value (12050L)
     */
    public static long convertNairaStringToKobo(String amountString) {
        if (amountString == null || amountString.trim().isEmpty()) {
            return 0L;
        }

        BigDecimal amount = new BigDecimal(amountString.trim());
        BigDecimal amountInCents = amount.multiply(new BigDecimal("100"));

        // Round to nearest whole Kobo
        BigDecimal roundedCents = amountInCents.setScale(0, RoundingMode.HALF_UP);

        return roundedCents.longValueExact();
    }

    /**
     * Formats a Firebase Timestamp into human-readable date, e.g. "12 Oct 2025, 09:45 AM"
     */
    public static String formatTimeStampToSimpleDateAndTime(Timestamp timestamp) {
        return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
                .format(timestamp.toDate());
    }

    public static String formatTimeStampToFullDateAndTime(Timestamp timestamp) {
        return new SimpleDateFormat("EEEE MMMM yyyy, hh:mm a", Locale.US)
                .format(timestamp.toDate());
    }

    public static String formatTimestampToRelative(Timestamp timestamp) {
        Date date = timestamp.toDate();
        long now = System.currentTimeMillis();

        return DateUtils.getRelativeTimeSpanString(
                date.getTime(),
                now,
                DateUtils.MINUTE_IN_MILLIS, // minimum resolution to display
                DateUtils.FORMAT_ABBREV_RELATIVE).toString(); // Flag to use shortened words
    }

    /**
     * Reads, scales, compresses, and encodes an image from a Uri into Base64 string.
     * Keeps aspect ratio and limits image dimensions to 512px max.
     */
    public static String compressAndConvertImageToBase64(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI");
                return null;
            }

            // Maintain aspect ratio, ensuring neither width nor height exceeds 512px
            int maxSize = 512;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float scale = Math.min((float) maxSize / width, (float) maxSize / height);

            int scaledWidth = Math.round(width * scale);
            int scaledHeight = Math.round(height * scale);

            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

            // Compress scaled bitmap to JPEG (70% quality)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

            // Convert bytes to Base64 string for upload or storage
            byte[] compressedBytes = outputStream.toByteArray();
            return Base64.encodeToString(compressedBytes, Base64.NO_WRAP);

        } catch (Exception e) {
            Log.e(TAG, "Error compressing image: " + e.getMessage(), e);
            return null;
        }
    }

    public static void showNotImplementedToast(Context context){
        Toast.makeText(context, "Feature not yet implemented", Toast.LENGTH_SHORT).show();
    }
}