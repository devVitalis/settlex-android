package com.settlex.android.util.ui;

import android.content.Context;
import android.view.LayoutInflater;

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

    public static void showBottomSheet(@NonNull Context context, @Nullable BiConsumer<BottomSheetDialog, BottomSheetDialogBinding> config) {
        BottomSheetDialogBinding binding = BottomSheetDialogBinding.inflate(LayoutInflater.from(context));

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.Widget_SettleX_BottomSheetDialog);
        dialog.setContentView(binding.getRoot());

        // Default config
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (config != null) config.accept(dialog, binding);
    }

    public static void showAlertDialog(Context context, BiConsumer<AlertDialog, AlertDialogBinding> config) {
        AlertDialogBinding binding = AlertDialogBinding.inflate(LayoutInflater.from(context));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setView(binding.getRoot())
                .setCancelable(false);

        AlertDialog alertDialog = builder.create();

        if (config != null) config.accept(alertDialog, binding);
    }
}