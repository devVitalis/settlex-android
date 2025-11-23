package com.settlex.android.presentation.wallet.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.settlex.android.databinding.ItemPromotionalBannerBinding;
import com.settlex.android.presentation.home.model.BannerUiModel;
import com.settlex.android.presentation.services.AirtimePurchaseActivity;
import com.settlex.android.presentation.services.BettingTopUpActivity;
import com.settlex.android.presentation.services.DataPurchaseActivity;

import java.util.List;
import java.util.Objects;

public class PromotionalBannerAdapter extends RecyclerView.Adapter<PromotionalBannerAdapter.PromoViewHolder> {
    private static final String TAG = PromotionalBannerAdapter.class.getSimpleName();
    private final List<BannerUiModel> promoBanners;

    public PromotionalBannerAdapter(List<BannerUiModel> promoBanners) {
        this.promoBanners = promoBanners;
    }

    @NonNull
    @Override
    public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPromotionalBannerBinding binding = ItemPromotionalBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PromoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
        holder.onBind(promoBanners.get(position));
    }

    @Override
    public int getItemCount() {
        return promoBanners.size();
    }

    public static class PromoViewHolder extends RecyclerView.ViewHolder {
        private final ItemPromotionalBannerBinding binding;
        private final Context context;

        PromoViewHolder(@NonNull ItemPromotionalBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            context = binding.getRoot().getContext();
        }

        public void onBind(BannerUiModel bannerUiModel) {
            // Load from URL
            Glide.with(context)
                    .load(bannerUiModel.getImageUrl())
                    .centerCrop()
                    .into(binding.promoBanner);

            String url = bannerUiModel.getActionUrl();
            binding.getRoot().setOnClickListener(v -> {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    try {
                        Intent intentBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intentBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentBrowser);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to open browser: " + e.getMessage(), e);
                        Toast.makeText(context, "Can't open link. Please set a default web browser in your device settings", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // Handle deep link
                    if (url != null && url.startsWith("app://")) {
                        Uri deepLink = Uri.parse(url);
                        String host = deepLink.getHost();

                        switch (Objects.requireNonNull(host)) {
                            case "airtime" -> toDestination(AirtimePurchaseActivity.class);
                            case "data" -> toDestination(DataPurchaseActivity.class);
                            case "betting" -> toDestination(BettingTopUpActivity.class);
                        }
                    }
                }
            });
        }

        private void toDestination(Class<? extends Activity> activityClass) {
            context.startActivity(new Intent(context, activityClass));
        }
    }
}
