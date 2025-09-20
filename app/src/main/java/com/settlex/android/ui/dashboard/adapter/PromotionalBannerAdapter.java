package com.settlex.android.ui.dashboard.adapter;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.settlex.android.databinding.ItemPromotionalBannerBinding;
import com.settlex.android.ui.dashboard.model.PromoBannerUiModel;

import java.util.List;

public class PromotionalBannerAdapter extends RecyclerView.Adapter<PromotionalBannerAdapter.PromoViewHolder> {
    private final List<PromoBannerUiModel> promoBanners;

    public PromotionalBannerAdapter(List<PromoBannerUiModel> promoBanners) {
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

        PromoViewHolder(@NonNull ItemPromotionalBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void onBind(PromoBannerUiModel bannerUiModel) {
            // Load from URL
            Glide.with(binding.promoBanner.getContext())
                    .load(bannerUiModel.getImageUrl())
                    .centerCrop()
//                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.promoBanner);

            //
            String url = bannerUiModel.getActionUrl();
            binding.promoBanner.setOnClickListener(v -> {
                if (url != null && url.startsWith("http")) {
                    Log.d("Url", url);
                    Intent intentBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                    if (intentBrowser.resolveActivity(binding.getRoot().getContext().getPackageManager()) != null) {
                        binding.getRoot().getContext().startActivity(intentBrowser);
                    }

                } else {

                    if (url != null && url.startsWith("app://")) {
                        // TODO: handle app deep link
                        Uri deepLink = Uri.parse(url);
                    }
                }
            });

        }
    }
}
