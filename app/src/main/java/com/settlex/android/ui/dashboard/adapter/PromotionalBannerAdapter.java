package com.settlex.android.ui.dashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.settlex.android.R;

import java.util.List;

public class PromotionalBannerAdapter extends RecyclerView.Adapter<PromotionalBannerAdapter.PromoViewHolder> {
    private final List<Integer> promoBanners;

    public PromotionalBannerAdapter(List<Integer> promoBanners) {
        this.promoBanners = promoBanners;
    }

    @NonNull
    @Override
    public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotional_banner, parent, false);
        return new PromoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
        holder.banner.setImageResource(promoBanners.get(position));

      /*  String imageUrl = promoList.get(position).getImageUrl();

        // Load from URL
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .centerCrop()
                .into(holder.promoImage);
       */
    }

    @Override
    public int getItemCount() {
        return promoBanners.size();
    }

    public static class PromoViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView banner;

        PromoViewHolder(@NonNull View itemView) {
            super(itemView);
            banner = itemView.findViewById(R.id.promoBanner);
        }
    }
}
