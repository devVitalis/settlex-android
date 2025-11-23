package com.settlex.android.data.datasource.utils

import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.settlex.android.data.remote.dto.ApiResponse
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

/**
 * A utility class for invoking Firebase Cloud Functions.
 *
 * This class provides a simplified interface for calling HTTPS callable functions
 * and parsing the response into a structured `ApiResponse` object. It handles the
 * asynchronous call and JSON deserialization automatically.
 */
class CloudFunctions @Inject constructor(val functions: FirebaseFunctions) {

    suspend inline fun <reified T> call(name: String, data: Map<String, Any?>): ApiResponse<T> {
        val response = functions.getHttpsCallable(name)
            .call(data)
            .await()

        val gson = Gson()
        val json = gson.toJson(response.data)

        val type = object : TypeToken<ApiResponse<T>>() {}.type
        return gson.fromJson(json, type)
    }
}