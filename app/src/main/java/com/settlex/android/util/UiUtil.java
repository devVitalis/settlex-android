package com.settlex.android.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.settlex.android.R;

/**
 * Utility class for common UI operations including dialog and bottom sheet management.
 * Provides reusable methods for displaying consistent UI components throughout the application.
 */
public class UiUtil {

    private UiUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates and displays a non-cancelable Material BottomSheetDialog with custom ViewBinding.
     * The dialog will persist until explicitly dismissed through provided binding callbacks.
     *
     * @param context  The context used for dialog creation and view inflation
     * @param inflater Function to inflate the ViewBinding for the dialog content
     * @param onBind   Callback to configure view bindings and attach dialog event handlers
     * @param <T>      Type parameter extending ViewBinding for type-safe view access
     */
    public static <T extends ViewBinding> void showBottomSheet(@NonNull Context context, @NonNull BindingInflater<T> inflater, @NonNull OnBind<T> onBind) {
        T binding = inflater.inflate(LayoutInflater.from(context));

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(binding.getRoot());

        // Prevent dismissal by back press or outside touch
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        onBind.bind(binding, dialog);
        dialog.show();
    }

    /**
     * Displays a standardized informational dialog with configurable title, message, and callback.
     * Uses Material Design components for consistent appearance and behavior.
     *
     * @param title    Dialog title text
     * @param message  Dialog message content
     * @param context  Context for dialog creation and resource access
     * @param onOkay   Optional callback executed after dialog dismissal
     */
    public static void showInfoDialog(Context context, String title, String message, Runnable onOkay) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_show_info, null, false);

        TextView dialogTitle = view.findViewById(R.id.title);
        TextView dialogMessage = view.findViewById(R.id.message);
        Button btnOkay = view.findViewById(R.id.btnOkay);

        dialogTitle.setText(title);
        dialogMessage.setText(message);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        btnOkay.setOnClickListener(v -> {
            alertDialog.dismiss();
            if (onOkay != null) onOkay.run();
        });
    }

    /**
     * Functional interface for ViewBinding inflation.
     * @param <T> The ViewBinding type to be inflated
     */
    public interface BindingInflater<T extends ViewBinding> {
        T inflate(LayoutInflater inflater);
    }

    /**
     * Functional interface for ViewBinding configuration and dialog event handling.
     * @param <T> The ViewBinding type to be configured
     */
    public interface OnBind<T extends ViewBinding> {
        void bind(T binding, BottomSheetDialog dialog);
    }
}