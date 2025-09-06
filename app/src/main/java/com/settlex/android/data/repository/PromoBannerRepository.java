package com.settlex.android.data.repository;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.settlex.android.ui.dashboard.model.PromoBannerUiModel;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class PromoBannerRepository {
    private final FirebaseRemoteConfig remoteConfig;

    //Constructor
    public PromoBannerRepository() {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        setFirebaseRemoteSettings();
    }

    // Callback interfaces
    public interface PromoBannersCallback {
        void onResult(List<PromoBannerUiModel> bannerUiModel);
    }

    // Firebase Config settings
    private void setFirebaseRemoteSettings() {
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(12 * 60 * 60) // fetch once per 12h
                .build();
        remoteConfig.setConfigSettingsAsync(settings);
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
                        Type listType = new TypeToken<List<PromoBannerUiModel>>() {
                        }.getType();
                        List<PromoBannerUiModel> promos = gson.fromJson(json, listType);
                        callback.onResult(promos);
                    } catch (Exception e) {
                        callback.onResult(Collections.emptyList());
                    }
                });
    }
}
