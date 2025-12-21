package com.settlex.android.util.image

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageConverter {
    private const val TAG = "ImageConverter"

    /**
     * Converts an image Uri to a compressed Base64 string.
     */
    suspend fun toBase64(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Base64 conversion failed", e)
                null
            }
        }
    }
}

