package com.settlex.android.utils;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class ResponseHandler {

    /*-------------------------------------------------------
    Extract error message from response body safely.
    Falls back to default messages with error codes.
    --------------------------------------------------------*/
    public static String getErrorMessage(@NonNull Response<?> response) {
        ResponseBody errorBody = response.errorBody();

        if (errorBody == null) {
            return "Empty response from server. [RH1003]";
        }

        try {
            String errorJson = errorBody.string();
            JSONObject json = new JSONObject(errorJson);

            if (json.has("error")) {
                return json.getString("error");
            }

            if (json.has("message")) {
                return json.getString("message");
            }

            return "Unknown server error. [RH1001]";

        } catch (Exception e) {
            return "Failed to parse server error. [RH1002]";
        }
    }
}