package com.settlex.android.domain.repository

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.settlex.android.di.AppPrefs
import com.settlex.android.presentation.dashboard.home.model.PromoBannerUiModel
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Singleton
class PromoBannerRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    @param:AppPrefs private val appPrefs: SharedPreferences
) {
    private val gson: Gson by lazy { GsonBuilder().create() }

    init {
        setFirebaseRemoteSettings()
    }

    /**
     * Get promotional banners from cache first.
     * If cache is empty or outdated, trigger a background fetch.
     * Returns cached data immediately (or empty list if nothing cached yet).
     */
    suspend fun getPromotionalBanners(): List<PromoBannerUiModel> {
        val cached = getCachedBanners()
        Log.d(TAG, "Cached banners: $cached")

        // If cache is empty or older than 12 hours, fetch fresh data in background
        if (cached.isEmpty() || isCacheOutdated()) {
            fetchBannersWithRetry(retryAttempt = 1)
            return getCachedBanners()
        }

        Log.d(TAG, "Returning cached banners")
        return cached
    }


    /**
     * Fetch and activate from Firebase remote config.
     * Automatically retries once if it fails.
     */
    private suspend fun fetchBannersWithRetry(retryAttempt: Int) {
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
            if (retryAttempt < 2) {
                Log.w(TAG, "Fetch failed, retrying... (attempt ${retryAttempt + 1})", e)
                delay(5000)
                fetchBannersWithRetry(retryAttempt + 1)
            } else {
                Log.e(TAG, "Fetch failed after $retryAttempt retries", e)
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

            if (json.isBlank()) {
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
            val json = appPrefs.getString(CACHE_KEY_BANNERS, null) ?: return emptyList()
            val listType = object : TypeToken<List<PromoBannerUiModel>>() {}.type
            gson.fromJson<List<PromoBannerUiModel>>(json, listType) ?: emptyList()
        }.onFailure { throwable ->
            Log.e(TAG, "Failed to parse cached banners", throwable)
        }.getOrDefault(emptyList())
    }

    /**
     * Save banners to cache with a timestamp.
     */
    private fun cacheBanners(banners: List<PromoBannerUiModel>) {
        runCatching {
            val json = gson.toJson(banners)
            appPrefs.edit {
                putString(CACHE_KEY_BANNERS, json)
                putLong(CACHE_KEY_TIMESTAMP, System.currentTimeMillis())
            }
        }.onFailure { throwable ->
            Log.e(TAG, "Failed to cache banners", throwable)
        }
    }

    /**
     * Check if cache is older than 12 hours.
     */
    private fun isCacheOutdated(): Boolean {
        val lastFetch = appPrefs.getLong(CACHE_KEY_TIMESTAMP, 0L)
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