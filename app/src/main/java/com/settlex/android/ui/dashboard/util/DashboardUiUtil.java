package com.settlex.android.ui.dashboard.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.settlex.android.R;
import com.settlex.android.data.remote.profile.ProfileService;
import com.settlex.android.databinding.BottomSheetConfirmPaymentBinding;
import com.settlex.android.utils.string.StringUtil;

public class DashboardUiUtil {

    private DashboardUiUtil() {
        // prevent instantiation
    }

    public static BottomSheetDialog showPayConfirmation(Context context, String recipientUsername, String recipientName, String recipientProfileUrl, long amountToSend, long senderWalletBalance, long senderCommissionBalance, final Runnable onPay) {
        BottomSheetConfirmPaymentBinding binding = BottomSheetConfirmPaymentBinding.inflate(LayoutInflater.from(context));
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.MyBottomSheetDialogTheme);
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Set blur background
        View rootView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            rootView = ((Activity) context).getWindow().getDecorView();
            rootView.setRenderEffect(RenderEffect.createBlurEffect(5, 5, Shader.TileMode.CLAMP));
        } else {
            rootView = null;
        }

        // conditions
        long SENDER_TOTAL_BALANCE = senderWalletBalance + senderCommissionBalance;

        boolean IS_SENDER_TOTAL_BALANCE_SUFFICIENT = SENDER_TOTAL_BALANCE < amountToSend;
        boolean IS_SENDER_WALLET_BALANCE_SUFFICIENT = senderWalletBalance >= amountToSend;

        String PAYMENT_METHOD = (IS_SENDER_WALLET_BALANCE_SUFFICIENT) ? "Wallet" : "ALL";

        if (IS_SENDER_TOTAL_BALANCE_SUFFICIENT) {
            // Not enough money at all
            String ERROR_INSUFFICIENT_BALANCE = "Insufficient";

            binding.txtFeedback.setVisibility(View.VISIBLE);
            binding.paymentMethod.setText(ERROR_INSUFFICIENT_BALANCE);

            // Hide debit breakdowns
            binding.debitFromSenderWalletBalance.setVisibility(View.GONE);
            binding.debitFromSenderCommissionBalance.setVisibility(View.GONE);

        } else if (IS_SENDER_WALLET_BALANCE_SUFFICIENT) {
            // Wallet balance alone is enough
            String DEBIT_FROM_SENDER_WALLET_BALANCE = "-" + StringUtil.formatToNaira(amountToSend);

            binding.paymentMethod.setText(PAYMENT_METHOD);
            binding.debitFromSenderWalletBalance.setVisibility(View.VISIBLE);
            binding.debitFromSenderWalletBalance.setText(DEBIT_FROM_SENDER_WALLET_BALANCE);

            binding.btnPay.setEnabled(true);

            // Hide commission since not used
            binding.debitFromSenderCommissionBalance.setVisibility(View.GONE);

        } else {
            // Wallet not enough, but wallet + commission is sufficient
            long fromWallet;
            fromWallet = senderWalletBalance;
            long fromCommission = amountToSend - senderWalletBalance;

            String DEBIT_FROM_SENDER_WALLET_BALANCE = "-" + StringUtil.formatToNaira(fromWallet);
            String DEBIT_FROM_SENDER_COMM_BALANCE = "-" + StringUtil.formatToNaira(fromCommission);

            binding.paymentMethod.setText(PAYMENT_METHOD);

            if (senderWalletBalance != 0) {
                binding.debitFromSenderWalletBalance.setVisibility(View.VISIBLE);
                binding.debitFromSenderWalletBalance.setText(DEBIT_FROM_SENDER_WALLET_BALANCE);
            }

            binding.debitFromSenderCommissionBalance.setVisibility(View.VISIBLE);
            binding.debitFromSenderCommissionBalance.setText(DEBIT_FROM_SENDER_COMM_BALANCE);

            // Enable pay button
            binding.btnPay.setEnabled(true);

            // Hide feedback since covered
            binding.txtFeedback.setVisibility(View.GONE);
        }

        // Recipient details
        binding.amountToSendHeader.setText(StringUtil.formatToNaira(amountToSend));
        binding.amountToSend.setText(StringUtil.formatToNaira(amountToSend));
        binding.recipientUsername.setText(StringUtil.addAtToPaymentId(recipientUsername));
        binding.recipientName.setText(recipientName.toUpperCase());
        ProfileService.loadProfilePic(recipientProfileUrl, binding.recipientProfilePic);

        // Sender details
        String SENDER_WALLET_BALANCE = "(" + StringUtil.formatToNaira(senderWalletBalance) + ")";
        String SENDER_COMM_BALANCE = "(" + StringUtil.formatToNaira(senderCommissionBalance) + ")";

        binding.senderTotalBalance.setText(StringUtil.formatToNaira(SENDER_TOTAL_BALANCE));
        binding.senderWalletBalance.setText(SENDER_WALLET_BALANCE);
        binding.senderCommissionBalance.setText(SENDER_COMM_BALANCE);

        // Handle buttons
        binding.btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) rootView.setRenderEffect(null);
        });
        binding.btnPay.setOnClickListener(v -> {
            binding.btnPay.setEnabled(false);
            if (onPay != null) {
                onPay.run();
            }
        });
        dialog.show();
        return dialog;
    }
}