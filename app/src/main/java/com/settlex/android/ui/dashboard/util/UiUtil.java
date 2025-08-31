package com.settlex.android.ui.dashboard.util;

import android.content.Context;
import android.view.LayoutInflater;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.settlex.android.R;
import com.settlex.android.databinding.BottomSheetConfirmPaymentBinding;

public class UiUtil {

    public interface Callback {
        void onConfirm();
    }

    public static void show(Context context, String amount, String recipient, final Callback callback) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        BottomSheetConfirmPaymentBinding binding = BottomSheetConfirmPaymentBinding.inflate(LayoutInflater.from(context));

        // Set payment details
        binding.recipientTag.setText(amount);
        binding.recipientName.setText(recipient);
//        binding.recipientProfilePic.setImageResource();

        binding.recipientTag.setText(amount);
        binding.recipientName.setText(recipient);


        // Handle buttons
        binding.btnPay.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) callback.onConfirm();
        });

        dialog.setContentView(binding.getRoot());
        dialog.show();
    }
}
