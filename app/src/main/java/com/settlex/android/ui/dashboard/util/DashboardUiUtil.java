package com.settlex.android.ui.dashboard.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.settlex.android.R;
import com.settlex.android.databinding.BottomSheetConfirmPaymentBinding;
import com.settlex.android.util.string.StringUtil;

public class DashboardUiUtil {

    private DashboardUiUtil() {
    }

    public static void showPayConfirmation(Context context, String recipientUsername, int profilePic, String recipientName, double amountToSend, double senderWalletBalance, double senderCommissionBalance, final Runnable onPay) {
        BottomSheetConfirmPaymentBinding binding = BottomSheetConfirmPaymentBinding.inflate(LayoutInflater.from(context));
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.Widget_SettleX_BottomSheetDialog);
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        //Format data for UI display
        String formattedAmountToSend = StringUtil.formatToNaira(amountToSend);
        String formattedUsername = StringUtil.addAtToUsername(recipientUsername);
        String formattedRecipientName = recipientName.toUpperCase();
        double senderTotalBalance = senderWalletBalance + senderCommissionBalance;

        // Conditions
        if (senderWalletBalance >= amountToSend){
            binding.btnPay.setEnabled(true);
        } else {
            binding.txtFeedback.setVisibility(View.VISIBLE);
        }

        // Recipient details
        binding.amountToSendHeader.setText(formattedAmountToSend);
        binding.amountToSend.setText(formattedAmountToSend);
        binding.recipientUsername.setText(formattedUsername);
        binding.recipientName.setText(formattedRecipientName);
        binding.recipientProfilePic.setImageResource(profilePic);

        // Sender details
        binding.senderTotalBalance.setText(StringUtil.formatToNaira(senderTotalBalance));
        binding.senderWalletBalance.setText(StringUtil.formatToNaira(senderWalletBalance));
        binding.senderCommissionBalance.setText(StringUtil.formatToNaira(senderCommissionBalance));

        // Handle buttons
        binding.btnClose.setOnClickListener(v -> dialog.dismiss());
        binding.btnPay.setOnClickListener(v -> {
            binding.btnPay.setEnabled(false);
            dialog.dismiss();
            if (onPay != null) onPay.run();
        });

        dialog.show();
    }
}