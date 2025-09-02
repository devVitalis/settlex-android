package com.settlex.android.ui.dashboard.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.settlex.android.R;
import com.settlex.android.databinding.BottomSheetConfirmPaymentBinding;

public class DashboardUiUtil {

    private DashboardUiUtil() {
    }

    public static void showPayConfirmation(Context context, String recipient,
                                           int profilePic,
                                           String recipientName,
                                           String amountToSendHeader,
                                           String amountToSend,
                                           String senderTotalBalance,
                                           String senderWalletBalance,
                                           String senderCommissionBalance,
                                           final Runnable onPay) {
        BottomSheetConfirmPaymentBinding binding = BottomSheetConfirmPaymentBinding.inflate(LayoutInflater.from(context));
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.Widget_SettleX_BottomSheetDialog);
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Recipient details
        binding.amountToSendHeader.setText(amountToSendHeader);
        binding.recipient.setText(recipient);
        binding.recipientProfilePic.setImageResource(profilePic);
        binding.recipientName.setText(recipientName);
        binding.amountToSend.setText(amountToSend);

        // Sender details
        binding.senderTotalBalance.setText(senderTotalBalance);
        binding.senderWalletBalance.setText(senderWalletBalance);
        binding.senderCommissionBalance.setText(senderCommissionBalance);

        if (amountToSendHeader == null || amountToSendHeader.isEmpty()) {
            binding.amountToSendHeader.setVisibility(View.GONE);
        }

        // Handle buttons
        binding.btnClose.setOnClickListener(v -> dialog.dismiss());
        binding.btnPay.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPay != null) onPay.run();
        });

        dialog.show();
    }
}