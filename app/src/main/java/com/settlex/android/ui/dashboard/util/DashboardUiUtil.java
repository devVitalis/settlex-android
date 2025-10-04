package com.settlex.android.ui.dashboard.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.settlex.android.R;
import com.settlex.android.data.remote.avater.AvatarService;
import com.settlex.android.databinding.BottomSheetConfirmPaymentBinding;
import com.settlex.android.util.string.StringUtil;

public class DashboardUiUtil {

    private DashboardUiUtil() {
        // prevent instantiation
    }

    public static BottomSheetDialog showPayConfirmation(Context context, String recipientUsername, String recipientName, double amountToSend, double senderWalletBalance, double senderCommissionBalance, final Runnable onPay) {
        BottomSheetConfirmPaymentBinding binding = BottomSheetConfirmPaymentBinding.inflate(LayoutInflater.from(context));
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.Widget_SettleX_BottomSheetDialog);
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Format data for UI display
        String formattedAmountToSend = StringUtil.formatToNaira(amountToSend);
        String formattedUsername = StringUtil.addAtToUsername(recipientUsername);
        String formattedRecipientName = recipientName.toUpperCase();
        double senderTotalBalance = senderWalletBalance + senderCommissionBalance;

        // Feedback texts
        String ERROR_INSUFFICIENT_BALANCE = "Insufficient";


        // Conditions
        boolean isSenderTotalBalanceSufficient = senderTotalBalance < amountToSend;
        boolean isSenderWalletBalanceSufficient = senderWalletBalance >= amountToSend;

        String PAYMENT_METHOD = (isSenderWalletBalanceSufficient) ? "Wallet" : "ALL";

        if (isSenderTotalBalanceSufficient) {
            // Not enough money at all
            binding.txtFeedback.setVisibility(View.VISIBLE);
            binding.paymentMethod.setText(ERROR_INSUFFICIENT_BALANCE);

            // Hide debit breakdowns
            binding.debitFromSenderWalletBalance.setVisibility(View.GONE);
            binding.debitFromSenderCommissionBalance.setVisibility(View.GONE);

        } else if (isSenderWalletBalanceSufficient) {
            // Wallet balance alone is enough
            String DEBIT_FROM_SENDER_WALLET_BALANCE = "-" + formattedAmountToSend;

            binding.paymentMethod.setText(PAYMENT_METHOD);
            binding.debitFromSenderWalletBalance.setVisibility(View.VISIBLE);
            binding.debitFromSenderWalletBalance.setText(DEBIT_FROM_SENDER_WALLET_BALANCE);

            binding.btnPay.setEnabled(true);

            // Hide commission since not used
            binding.debitFromSenderCommissionBalance.setVisibility(View.GONE);

        } else {
            // Wallet not enough, but wallet + commission is sufficient
            double fromWallet;
            fromWallet = senderWalletBalance;
            double fromCommission = amountToSend - senderWalletBalance;

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
        binding.amountToSendHeader.setText(formattedAmountToSend);
        binding.amountToSend.setText(formattedAmountToSend);
        binding.recipientUsername.setText(formattedUsername);
        binding.recipientName.setText(formattedRecipientName);
        AvatarService.loadAvatar(recipientName, binding.recipientProfilePic); // TODO: replace with real profile pic

        // Sender details
        String SENDER_WALLET_BALANCE = "(" + StringUtil.formatToNaira(senderWalletBalance) + ")";
        String SENDER_COMM_BALANCE = "(" + StringUtil.formatToNaira(senderCommissionBalance) + ")";

        binding.senderTotalBalance.setText(StringUtil.formatToNaira(senderTotalBalance));
        binding.senderWalletBalance.setText(SENDER_WALLET_BALANCE);
        binding.senderCommissionBalance.setText(SENDER_COMM_BALANCE);

        // Handle buttons
        binding.btnClose.setOnClickListener(v -> dialog.dismiss());
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