package com.settlex.android.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;

/**
 * Transaction ID Generator
 * Compact + unique:
 * - 8-char username hash
 * - Base36 timestamp (~8 chars)
 * - 16-char UUID slice
 */
public class TransactionIdGenerator {

    private TransactionIdGenerator(){
        // prevent instantiation
    }

    // Generate compact unique transaction ID
    public static String generate(String uid) {
        String userHash = hashUID(uid); // 8 chars
        String tsBase36 = Long.toString(System.currentTimeMillis(), 36); // ~8 chars
        String uuidShort = UUID.randomUUID().toString().replace("-", "").substring(0, 16); // 16 chars

        return userHash + tsBase36 + uuidShort; // ~32 chars total
    }

    // Hash the UID into lowercase hex (first 4 bytes -> 8 chars)
    private static String hashUID(String uid) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(uid.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 4; i++) { // 4 bytes = 8 hex chars
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