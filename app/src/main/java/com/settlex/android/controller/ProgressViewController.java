
package com.settlex.android.controller;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.settlex.android.R;

public class ProgressViewController {

    private final View progressOverlayView;
    private AnimatorSet zoomAnimator;


    /*-----------------------------------------------
        Constructor: attach to activity layout
    -----------------------------------------------*/
    public ProgressViewController(@NonNull View rootView) {
        ViewGroup root = (ViewGroup) rootView;

        View existingOverlay = root.findViewById(R.id.progressbar_overlay);
        if (existingOverlay != null) {
            progressOverlayView = existingOverlay;
        } else {
            LayoutInflater inflater = LayoutInflater.from(rootView.getContext());
            progressOverlayView = inflater.inflate(R.layout.progressbar_overlay, root, false);
            root.addView(progressOverlayView);
        }

        progressOverlayView.setVisibility(View.GONE);
    }


    /*---------------------------------
        Show the loader overlay
     ----------------------------------*/
    public void show() {
        if (progressOverlayView.getVisibility() != View.VISIBLE) {
            progressOverlayView.setAlpha(1f);
            progressOverlayView.setVisibility(View.VISIBLE);

            ImageView logo = progressOverlayView.findViewById(R.id.logo);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 1f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 1f, 1.1f, 1f);

            scaleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleX.setRepeatMode(ValueAnimator.RESTART);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.setRepeatMode(ValueAnimator.RESTART);

            zoomAnimator = new AnimatorSet();
            zoomAnimator.playTogether(scaleX, scaleY);
            zoomAnimator.setDuration(1000);
            zoomAnimator.start();
        }
    }


    /*---------------------------------
        Hide the loader overlay
     ----------------------------------*/
    public void hide() {
        if (progressOverlayView.getVisibility() == View.VISIBLE) {
            if (zoomAnimator != null && zoomAnimator.isRunning()) {
                zoomAnimator.cancel();
            }

            progressOverlayView.setAlpha(1f);
            progressOverlayView.setVisibility(View.GONE);
        }
    }

    /*---------------------------------
       Expose the view
    ----------------------------------*/
    public View getOverlayView() {
        return progressOverlayView;
    }

}