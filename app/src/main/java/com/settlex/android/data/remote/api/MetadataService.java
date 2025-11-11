package com.settlex.android.data.remote.api;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.settlex.android.data.remote.dto.MetadataDto;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Service for collecting device and network metadata for API requests.
 * <p>
 * Collects device information, network details, and geolocation data to provide
 * context for server-side request processing and analytics.
 */
public class MetadataService {
    private static final String TAG = "RequestMetadataService";

    public interface RequestMetadataServiceCallback {
        void onResult(MetadataDto metadata);
    }

    /**
     * Asynchronously collects device and network metadata.
     * <p>
     * The callback will be executed on the main thread with either:
     * - Complete metadata (device + network info)
     * - Partial metadata (device info only if network fails)
     *
     * @param callback Receiver for the collected metadata
     */
    public static void collectMetadata(RequestMetadataServiceCallback callback) {
        new Thread(() -> {
            MetadataDto metadata = new MetadataDto();

            // Device information (always available)
            metadata.deviceBrand = safeValue(Build.BRAND);
            metadata.deviceModel = safeValue(Build.MODEL);
            metadata.osName = "Android";
            metadata.osVersion = safeValue(Build.VERSION.RELEASE);

            // Network information (default to unknown)
            metadata.publicIp = "unknown";
            metadata.city = "unknown";
            metadata.country = "unknown";
            metadata.isComplete = false;

            try {
                JSONObject json = fetchIpLocationData();
                metadata.publicIp = safeValue(json.optString("ip", "unknown"));
                metadata.city = safeValue(json.optString("city", "unknown"));
                metadata.country = safeValue(json.optString("country_name", "unknown"));

                metadata.isComplete = !metadata.publicIp.equals("unknown") && !metadata.city.equals("unknown") && !metadata.country.equals("unknown");
                if (!metadata.isComplete)  Log.w(TAG, "Metadata failed to complete");

            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch network metadata", e);
            }

            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(metadata));
        }).start();
    }

    /**
     * Fetches geolocation data from IP API service
     *
     * @return JSON response from the API
     * @throws Exception if network request fails
     */
    private static JSONObject fetchIpLocationData() throws Exception {
        URL url = new URL("https://ipapi.co/json/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return new JSONObject(response.toString());
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Safely converts nullable strings to default values
     *
     * @param value Input string that may be null
     * @return Original value if valid, "unknown" otherwise
     */
    private static String safeValue(String value) {
        return (value != null && !value.trim().isEmpty()) ? value : "unknown";
    }
}