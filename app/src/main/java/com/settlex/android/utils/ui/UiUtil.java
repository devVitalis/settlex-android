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
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.settlex.android.R;
import com.settlex.android.databinding.AlertDialogBinding;
import com.settlex.android.databinding.BottomSheetDialogBinding;

import java.util.function.BiConsumer;

/**
 * Utility class for common UI operations
 * Provides reusable methods for displaying consistent UI components throughout the application.
 */
public class UiUtil {

    private UiUtil() {
        // Utility class - prevent instantiation
    }

    public static void showBottomSheetDialog(@NonNull Context context, @Nullable BiConsumer<BottomSheetDialog, BottomSheetDialogBinding> config) {
        BottomSheetDialogBinding binding = BottomSheetDialogBinding.inflate(LayoutInflater.from(context));

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.MyBottomSheetDialogTheme);
        dialog.setContentView(binding.getRoot());

        // Default config
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // blur background on Android 12+
            View rootView = ((Activity) context).getWindow().getDecorView();
            rootView.setRenderEffect(RenderEffect.createBlurEffect(5, 5, Shader.TileMode.CLAMP));
        }

        if (config != null) config.accept(dialog, binding);
    }

    public static void showCustomAlertDialog(Context context, BiConsumer<AlertDialog, AlertDialogBinding> config) {
        AlertDialogBinding binding = AlertDialogBinding.inflate(LayoutInflater.from(context));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setView(binding.getRoot())
                .setCancelable(false);

        AlertDialog alertDialog = builder.create();

        if (config != null) config.accept(alertDialog, binding);
    }

    public static void showSimpleAlertDialog(Context context, String title, String message) {
        new MaterialAlertDialogBuilder(context, R.style.MyAlertDialogTheme)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, i) -> dialog.dismiss())
                .show();
    }
}