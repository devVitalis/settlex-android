package com.settlex.android.ui.dashboard.model;

public class BannerUiModel {
    private final String imageUrl;
    private final String actionUrl;

    public BannerUiModel(String imageUrl, String actionUrl) {
        this.imageUrl = imageUrl;
        this.actionUrl = actionUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getActionUrl() {
        return actionUrl;
    }
}
