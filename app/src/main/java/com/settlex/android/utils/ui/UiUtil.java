package com.settlex.android.utils.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.settlex.android.R;
import com.settlex.android.databinding.BottomSheetSuccessDialogBinding;

import java.util.function.BiConsumer;

/**
 * Utility class for common UI operations
 * Provides reusable methods for displaying consistent UI components throughout the application.
 */
public class UiUtil {

    private UiUtil() {
        // Utility class - prevent instantiation
    }

    public static void showSuccessBottomSheetDialog(@NonNull Context context, @Nullable BiConsumer<BottomSheetDialog, BottomSheetSuccessDialogBinding> config) {
        BottomSheetSuccessDialogBinding binding = BottomSheetSuccessDialogBinding.inflate(LayoutInflater.from(context));
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet);
        dialog.setContentView(binding.getRoot());

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // blur background on Android 12+
            View rootView = ((Activity) context).getWindow().getDecorView();
            rootView.setRenderEffect(RenderEffect.createBlurEffect(5, 5, Shader.TileMode.CLAMP));
        }

        if (config != null) config.accept(dialog, binding);
        dialog.show();
    }

    public static void showSimpleAlertDialog(Context context, String title, String message) {
        new MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, i) -> dialog.dismiss())
                .show();
    }


    public static void showNoInternetAlertDialog(Context context) {
        new MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert)
                .setCancelable(true)
                .setTitle("Network Unavailable")
                .setMessage("Please check your Wi-Fi or cellular data and try again")
                .setPositiveButton("OK", (dialog, i) -> dialog.dismiss())
                .show();
    }
}