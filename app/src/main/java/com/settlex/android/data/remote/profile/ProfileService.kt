package com.settlex.android.data.remote.profile

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.settlex.android.R
import com.settlex.android.SettleXApp

/**
 * A singleton object responsible for loading and displaying user profile pictures.
 * This service utilizes the Glide library to efficiently load images from a URL
 * into an ImageView, providing caching, error handling, and transition effects.
 */
object ProfileService {

    @JvmStatic
    fun loadProfilePhoto(profilePhotoUrl: String?, target: ImageView) {
        if (profilePhotoUrl == null) return
        Glide.with(SettleXApp.appContext)
            .load(profilePhotoUrl)
            .centerCrop()
            .error(R.drawable.ic_no_profile_photo)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(DrawableTransitionOptions.withCrossFade(100))
            .into(target)
    }
}