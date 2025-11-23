package com.settlex.android.presentation.common.components;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.settlex.android.R;
import com.settlex.android.databinding.ProgressbarOverlayBinding;

public class ProgressDialogFragment extends DialogFragment {
    private AnimatorSet zoomAnimator;
    private int overlayColor;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_SettleX_Dialog_Transparent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Intercept back key explicitly
        dialog.setOnKeyListener((d, keyCode, event) -> {
            return keyCode == android.view.KeyEvent.KEYCODE_BACK; // consume the event, do nothing
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ProgressbarOverlayBinding binding = ProgressbarOverlayBinding.inflate(inflater, container, false);
        binding.getRoot().setBackgroundColor(overlayColor);

        ImageView logo = binding.getRoot().findViewById(R.id.logo);

        // Animate logo
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

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (zoomAnimator != null && zoomAnimator.isRunning()) {
            zoomAnimator.cancel();
        }
    }

    public void setColor(int color) {
        this.overlayColor = color;
    }
}
