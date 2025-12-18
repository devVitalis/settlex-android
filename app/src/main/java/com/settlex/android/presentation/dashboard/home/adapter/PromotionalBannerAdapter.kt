package com.settlex.android.presentation.dashboard.home.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.settlex.android.databinding.ItemPromotionalBannerBinding
import com.settlex.android.presentation.dashboard.home.adapter.PromotionalBannerAdapter.PromoViewHolder
import com.settlex.android.presentation.dashboard.home.model.PromoBannerUiModel
import com.settlex.android.presentation.dashboard.services.AirtimePurchaseActivity
import com.settlex.android.presentation.dashboard.services.BettingTopUpActivity
import com.settlex.android.presentation.dashboard.services.DataPurchaseActivity

class PromotionalBannerAdapter(private val promoBanners: List<PromoBannerUiModel>) :
    RecyclerView.Adapter<PromoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val binding = ItemPromotionalBannerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PromoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        holder.onBind(promoBanners[position])
    }

    override fun getItemCount(): Int {
        return promoBanners.size
    }

    class PromoViewHolder internal constructor(private val binding: ItemPromotionalBannerBinding) :
        RecyclerView.ViewHolder(
            binding.getRoot()
        ) {
        private val context: Context = binding.root.context

        fun onBind(bannerUiModel: PromoBannerUiModel) {
            // Load from URL
            Glide.with(context)
                .load(bannerUiModel.imageUrl)
                .centerCrop()
                .into(binding.ivPromoBanner)

            val url = bannerUiModel.actionUrl ?: return
            binding.root.setOnClickListener {
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    try {
                        val intentBrowser = Intent(Intent.ACTION_VIEW, url.toUri())
                        intentBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intentBrowser)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to open browser: " + e.message, e)
                        Toast.makeText(
                            context,
                            "Can't open link. Please set a default web browser in your device settings",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Handle deep link
                    if (url.startsWith("app://")) {
                        val deepLink = url.toUri()
                        val host = deepLink.host

                        when (host) {
                            "airtime" -> toDestination(AirtimePurchaseActivity::class.java)
                            "data" -> toDestination(DataPurchaseActivity::class.java)
                            "betting" -> toDestination(BettingTopUpActivity::class.java)
                        }
                    }
                }
            }
        }

        private fun toDestination(activityClass: Class<out Activity>) {
            context.startActivity(Intent(context, activityClass))
        }
    }

    companion object {
        private val TAG: String = PromotionalBannerAdapter::class.java.simpleName
    }
}
