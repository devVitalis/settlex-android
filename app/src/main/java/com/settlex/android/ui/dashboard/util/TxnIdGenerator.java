package com.settlex.android.ui.dashboard.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;

/**
 * Transaction ID Generator
 * Combines: username hash + timestamp + UUID
 */
public class TxnIdGenerator {

    // Generate unique transaction ID
    public static String generate(String username) {
        String userHash = hashUsername(username);
        long timestamp = System.currentTimeMillis();

        // UUID without dashes to keep ID compact
        String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT);

        return userHash + timestamp + uuid;
    }

    // Hash the username into lowercase hex (first 8 chars for compactness)
    private static String hashUsername(String username) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(username.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));

            // Convert first 8 bytes to hex string
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }
}
