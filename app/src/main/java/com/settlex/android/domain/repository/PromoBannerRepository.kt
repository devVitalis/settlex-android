package com.settlex.android.domain.repository

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.settlex.android.di.PromoBannerPrefs
import com.settlex.android.presentation.dashboard.home.model.PromoBannerUiModel
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PromoBannerRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    @param:PromoBannerPrefs private val sharedPreferences: SharedPreferences
) {
    private val gson: Gson by lazy { GsonBuilder().create() }
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        setFirebaseRemoteSettings()
    }

    /**
     * Get promotional banners from cache first.
     * If cache is empty or stale, trigger a background fetch.
     * Returns cached data immediately (or empty list if nothing cached yet).
     */
    fun getPromotionalBanners(): List<PromoBannerUiModel> {
        val cached = getCachedBanners()

        // If cache is empty or older than 12 hours, fetch fresh data in background
        if (cached.isEmpty() || isCacheStale()) {
            fetchBannersInBackground()
        }

        return cached
    }

    /**
     * Fetch banners from Firebase in the background without blocking the caller.
     * Automatically retries once if it fails.
     */
    private fun fetchBannersInBackground() {
        scope.launch {
            fetchBannersWithRetry(retryAttempt = 0)
        }
    }

    /**
     * Fetch and activate from Firebase. Converts callback to coroutine.
     */
    private suspend fun fetchBannersWithRetry(retryAttempt: Int) {
        return withContext(Dispatchers.IO) {
            try {
                val success = remoteConfig.fetchAndActivate().await()
                if (success) {
                    parseBannersFromRemoteConfig()?.let { banners ->
                        cacheBanners(banners)
                        Log.d(TAG, "Successfully fetched and cached ${banners.size} banners")
                    }
                } else {
                    throw Exception("Fetch activation returned false")
                }
            } catch (e: Exception) {
                if (retryAttempt < 1) {
                    Log.w(TAG, "Fetch failed, retrying... (attempt ${retryAttempt + 1})", e)
                    kotlinx.coroutines.delay(1000)
                    fetchBannersWithRetry(retryAttempt + 1)
                } else {
                    Log.e(TAG, "Fetch failed after retries", e)
                }
            }
        }
    }

    /**
     * Parse banners from Remote Config. Returns null if key doesn't exist or parse fails.
     */
    private fun parseBannersFromRemoteConfig(): List<PromoBannerUiModel>? {
        return runCatching {
            // Explicitly check if the key exists with a default value
            val json = remoteConfig.getString("promotional_banners")

            if (json.isEmpty()) {
                Log.d(TAG, "Remote Config key is empty or doesn't exist")
                return@runCatching null
            }

            val listType = object : TypeToken<List<PromoBannerUiModel>>() {}.type
            gson.fromJson<List<PromoBannerUiModel>>(json, listType) ?: emptyList()
        }.onFailure { throwable ->
            Log.e(TAG, "Failed to parse promotional banners from Remote Config", throwable)
        }.getOrNull()
    }

    /**
     * Get banners from SharedPreferences cache.
     */
    private fun getCachedBanners(): List<PromoBannerUiModel> {
        return runCatching {
            val json = sharedPreferences.getString(CACHE_KEY_BANNERS, null) ?: return emptyList()
            val listType = object : TypeToken<List<PromoBannerUiModel>>() {}.type
            gson.fromJson<List<PromoBannerUiModel>>(json, listType) ?: emptyList()
        }.onFailure { throwable ->
            Log.e(TAG, "Failed to parse cached banners", throwable)
        }.getOrNull() ?: emptyList()
    }

    /**
     * Save banners to cache with a timestamp.
     */
    private fun cacheBanners(banners: List<PromoBannerUiModel>) {
        runCatching {
            val json = gson.toJson(banners)
            sharedPreferences.edit().apply {
                putString(CACHE_KEY_BANNERS, json)
                putLong(CACHE_KEY_TIMESTAMP, System.currentTimeMillis())
                apply()
            }
        }.onFailure { throwable ->
            Log.e(TAG, "Failed to cache banners", throwable)
        }
    }

    /**
     * Check if cache is older than 12 hours.
     */
    private fun isCacheStale(): Boolean {
        val lastFetch = sharedPreferences.getLong(CACHE_KEY_TIMESTAMP, 0L)
        val timeSinceCacheUpdate = System.currentTimeMillis() - lastFetch
        val twelveHoursMs = 12 * 60 * 60 * 1000
        return timeSinceCacheUpdate > twelveHoursMs
    }

    private fun setFirebaseRemoteSettings() {
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds((12 * 60 * 60).toLong()) // 12 hours
            .build()
        remoteConfig.setConfigSettingsAsync(settings)
    }

    companion object {
        private val TAG = PromoBannerRepository::class.java.simpleName
        private const val CACHE_KEY_BANNERS = "cached_promotional_banners"
        private const val CACHE_KEY_TIMESTAMP = "cached_promotional_banners_timestamp"
    }
}