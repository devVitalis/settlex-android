package com.settlex.android.util.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

object ImageConverter {
    private const val TAG = "ImageConverter"
    private const val MAX_SIZE = 512

    /**
     * Converts an image Uri to a compressed Base64 string.
     */
    suspend fun imageUriToBase64(context: Context, imageUri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                // Step 1: Open stream safely
                context.contentResolver.openInputStream(imageUri)?.use { inputStream ->

                    // Step 2: Decode full bitmap
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                        ?: return@withContext null

                    // Step 3: Scale + compress
                    processBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Image conversion failed", e)
                null
            }
        }

    /**
     * Handles scaling, compressing, and Base64 conversion.
     */
    private fun processBitmap(bitmap: Bitmap): String? {
        // Step 1: Compute scale factor
        val needsScaling = bitmap.width > MAX_SIZE || bitmap.height > MAX_SIZE
        val scaled = if (needsScaling) {
            val scale = min(
                MAX_SIZE.toFloat() / bitmap.width,
                MAX_SIZE.toFloat() / bitmap.height
            )
            val newW = (bitmap.width * scale).roundToInt()
            val newH = (bitmap.height * scale).roundToInt()

            // Step 2: Create scaled bitmap
            bitmap.scale(newW, newH, true)
        } else {
            bitmap
        }

        // Step 3: Compress to JPEG
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        // Step 4: Recycle ONLY the scaled version if it's not the same instance
        if (scaled !== bitmap) scaled.recycle()

        // Step 5: Convert to Base64
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
