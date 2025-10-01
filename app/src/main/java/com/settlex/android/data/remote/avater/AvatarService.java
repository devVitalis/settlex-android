package com.settlex.android.data.remote.avater;

import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.settlex.android.SettleXApp;

public class AvatarService {
    private AvatarService() {
        // prevent instantiation
    }

    private static final String BASE_URL_INITIALS = "https://ui-avatars.com/api/"; // Initials Avatar
    private static final String BASE_URL_DICE_BEAR = "https://api.dicebear.com/7.x/adventurer/png?seed="; // Dice bear Avatar

    /**
     * Generate and load a user avatar into the given ImageView.
     *
     * @param displayName The user's first name & last name
     * @param target   The ImageView where the avatar will be loaded
     */
    public static void loadAvatar(String displayName, ImageView target) {

        // Build the request URL
        String name = Uri.encode(displayName);
        String uri_initials_avatar = BASE_URL_INITIALS + "?name=" + name + "&background=0044CC&color=fff&size=40";
        String uri_dice_bear = BASE_URL_DICE_BEAR + name;

        Glide.with(SettleXApp.getAppContext())
                .load(uri_dice_bear)
                .apply(new RequestOptions())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(target);
    }
}