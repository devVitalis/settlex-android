package com.settlex.android.ui.common.util;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class ProgressLoaderController {
    private final FragmentManager fragmentManager;
    private ProgressDialogFragment progressDialog;
    private int overlayColor = Color.TRANSPARENT;

    public ProgressLoaderController(@NonNull FragmentActivity activity) {
        this.fragmentManager = activity.getSupportFragmentManager();
    }

    public void show() {
        if (progressDialog == null || !progressDialog.isVisible()) {
            progressDialog = new ProgressDialogFragment();
            progressDialog.show(fragmentManager, "progress");
            progressDialog.setColor(overlayColor);
        }
    }

    public void hide() {
        if (progressDialog != null) {
            progressDialog.dismissAllowingStateLoss();
            progressDialog = null;
        }
    }

    // Set progress overlay color
    public void setOverlayColor(int color) {
        this.overlayColor = color;
    }
}
