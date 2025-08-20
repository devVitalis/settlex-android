package com.settlex.android.util;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class UiUtil {

    private UiUtil() {
        // Prevent instantiation
    }

    /**
     * Creates and shows a Material 3 BottomSheetDialog with a custom ViewBinding.
     *
     * @param context  The context (usually an Activity).
     * @param inflater A function that inflates the ViewBinding for the sheet.
     * @param onBind   A callback to configure views (listeners, texts, etc).
     * @param <T>      The type of the ViewBinding.
     */
    public static <T extends ViewBinding> void showBottomSheet(@NonNull Context context, @NonNull BindingInflater<T> inflater, @NonNull OnBind<T> onBind) {
        // Inflate the binding
        T binding = inflater.inflate(LayoutInflater.from(context));

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(binding.getRoot());

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        onBind.bind(binding, dialog);
        dialog.show();
    }

    // Functional interfaces for inflating + binding
    public interface BindingInflater<T extends ViewBinding> {
        T inflate(android.view.LayoutInflater inflater);
    }

    public interface OnBind<T extends ViewBinding> {
        void bind(T binding, BottomSheetDialog dialog);
    }
}
