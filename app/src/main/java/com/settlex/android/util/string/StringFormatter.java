package com.settlex.android.util.string;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Utility class for string formatting and data transformations.
 * Central place for formatting money, usernames, timestamps, etc.
 */
public class StringFormatter {
    private static final java.lang.String TAG = StringFormatter.class.getSimpleName();

    private StringFormatter() {
        // Prevent instantiation
    }

    /**
     * Masks email for privacy, e.g. johndoe@gmail.com → j***e@gmail.com
     */
    public static java.lang.String maskEmail(java.lang.String email) {
        if (email == null || email.isEmpty()) return email;

        java.lang.String[] parts = email.split("@");
        java.lang.String emailPrefix = parts[0];
        java.lang.String domain = parts[1];

        if (emailPrefix.length() <= 2) {
            return emailPrefix.charAt(0) + "***@" + domain;
        }

        java.lang.String masked = emailPrefix.charAt(0) + "***" + emailPrefix.charAt(emailPrefix.length() - 1);
        return masked + "@" + domain;
    }

    public static java.lang.String maskPhoneNumber(java.lang.String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return phoneNumber;

        int prefixLength = 7; // +234707
        int suffixLength = 3;

        // Extract visible parts
        java.lang.String prefix = phoneNumber.substring(0, prefixLength);
        java.lang.String suffix = phoneNumber.substring(phoneNumber.length() - suffixLength);

        // Build masked section with asterisks
        int maskLength = phoneNumber.length() - (prefixLength + suffixLength);
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < maskLength; i++) {
            mask.append("*");
        }

        return prefix + mask + suffix;
    }


    /**
     * Capitalizes each word: "john doe" → "John Doe"
     */
    public static java.lang.String capitalizeEachWord(java.lang.String input) {
        if (input == null || input.isEmpty()) return input;

        java.lang.String[] words = input.toLowerCase().trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (java.lang.String word : words) {
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
    public static java.lang.String removeAtInPaymentId(java.lang.String paymentId) {
        if (paymentId == null || paymentId.isEmpty()) return paymentId;
        if (paymentId.startsWith("@")) return paymentId.substring(1);
        return paymentId;
    }

    /**
     * Ensures Payment ID starts with '@'
     */
    public static java.lang.String addAtToPaymentId(java.lang.String paymentId) {
        if (paymentId == null || paymentId.isEmpty()) return paymentId;
        if (paymentId.startsWith("@")) return paymentId;
        return "@" + paymentId;
    }

    public static java.lang.String setAsterisks() {
        return "****";
    }

    /**
     * Copies text to clipboard, optionally showing a Toast (only for API < 33)
     */
    public static void copyToClipboard(Context context, java.lang.String label, java.lang.String text, boolean showToast) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, text);
        clipboardManager.setPrimaryClip(clipData);

        if (showToast && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show();
        }
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

            // Maintain aspect ratio,
            // Ensuring neither width nor height exceeds 512px
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

    public static void showNotImplementedToast(Context context) {
        Toast.makeText(context, "Feature not yet implemented", Toast.LENGTH_SHORT).show();
    }
}