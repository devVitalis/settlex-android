package com.settlex.android.utils.network;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class ResponseHandler {

    /*-------------------------------------------------------
    Safely reads and parses error message from response.
    Appends error code for tracking.
    --------------------------------------------------------*/
    public static String getErrorMessage(@NonNull Response<?> response) {
        ResponseBody errorBody = response.errorBody();

        if (errorBody != null) {
            try {
                // Read raw error response
                String errorJson = errorBody.string();
                Log.e("SERVER_ERROR_BODY", errorJson);

                // Try to parse JSON to extract "error" field
                JSONObject json = new JSONObject(errorJson);
                if (json.has("error")) {
                    return json.getString("error");
                } else if (json.has("message")) {
                    return json.getString("message");
                } else {
                    return "Unknown server error. [RH1001]";
                }

            } catch (Exception e) {
                Log.e("ERROR_PARSING_FAILED", e.toString());
                return "Failed to parse server error. [RH1002]";
            }

        } else {
            return "Empty response from server. [RH1003]";
        }
    }

}
