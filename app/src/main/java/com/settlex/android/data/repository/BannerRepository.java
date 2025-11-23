package com.settlex.android.data.repository;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.settlex.android.ui.dashboard.model.BannerUiModel;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import jakarta.inject.Inject;

public class BannerRepository {
    private final FirebaseRemoteConfig remoteConfig;

    @Inject
    public BannerRepository(FirebaseRemoteConfig remoteConfig) {
        this.remoteConfig = remoteConfig;
        setFirebaseRemoteSettings();
    }

    /**
     * Fetch promotional banners from firebase remote config
     */
    public void fetchPromotionalBanners(PromoBannersCallback callback) {
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onResult(Collections.emptyList());
                        return;
                    }

                    String json = remoteConfig.getString("promo_banners");
                    if (json.isEmpty()) {
                        callback.onResult(Collections.emptyList());
                        return;
                    }

                    try {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<BannerUiModel>>() {}.getType();
                        List<BannerUiModel> promos = gson.fromJson(json, listType);
                        callback.onResult(promos);
                    } catch (Exception e) {
                        callback.onResult(Collections.emptyList());
                    }
                });
    }

    public interface PromoBannersCallback {
        void onResult(List<BannerUiModel> bannerUiModel);
    }

    // Config settings
    private void setFirebaseRemoteSettings() {
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(12 * 60 * 60) // fetch once per 12h
                .build();
        remoteConfig.setConfigSettingsAsync(settings);
    }
}
