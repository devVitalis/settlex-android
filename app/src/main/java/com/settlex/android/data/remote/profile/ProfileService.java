package com.settlex.android.data.remote.profile;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.settlex.android.SettleXApp;

public class ProfileService {
    private ProfileService() {
        // prevent instantiation
    }

    /**
     * Load a user profile pic into the given ImageView.
     *
     * @param profilePicUrl The user's profile pic url
     * @param target        The ImageView where the avatar will be loaded
     */
    public static void loadProfilePic(String profilePicUrl, ImageView target) {
        Glide.with(SettleXApp.getAppContext())
                .load(profilePicUrl)
                .apply(new RequestOptions())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(target);
    }
}