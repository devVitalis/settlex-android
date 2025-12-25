package com.settlex.android.presentation.common.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.settlex.android.R
import com.settlex.android.data.remote.profile.ProfileService.loadProfilePic
import com.settlex.android.databinding.AlertDialogMessageBinding
import com.settlex.android.databinding.AlertDialogWithIconBinding
import com.settlex.android.databinding.BottomSheetConfirmPaymentBinding
import com.settlex.android.databinding.BottomSheetImageSourceBinding
import com.settlex.android.databinding.BottomSheetPaymentPinConfirmBinding
import com.settlex.android.databinding.BottomSheetSuccessDialogBinding
import com.settlex.android.presentation.common.custom.NumericKeypad.OnKeypadInputListener
import com.settlex.android.util.string.CurrencyFormatter
import com.settlex.android.util.string.StringFormatter
import java.util.Locale
import java.util.Objects
import java.util.function.BiConsumer

/**
 * Utility class for common UI operations
 * Provides reusable methods for displaying consistent UI components throughout the application.
 */
object DialogHelper {
    fun showSuccessBottomSheetDialog(
        context: Context,
        config: BiConsumer<BottomSheetDialog, BottomSheetSuccessDialogBinding>
    ) {
        val binding = BottomSheetSuccessDialogBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet)
        dialog.setContentView(binding.root)

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        // Blur background on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val rootView = (context as Activity).window.decorView
            rootView.setRenderEffect(RenderEffect.createBlurEffect(5f, 5f, Shader.TileMode.CLAMP))
        }

        binding.anim.playAnimation()

        config.accept(dialog, binding)
        dialog.show()
    }

    fun showSimpleAlertDialog(context: Context, title: String?, message: String?) {
        MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert)
            .setCancelable(true)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }


    fun showNoInternetAlertDialog(context: Context) {
        MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert)
            .setCancelable(true)
            .setTitle("Network Unavailable")
            .setMessage("Please check your Wi-Fi or cellular data and try again")
            .setPositiveButton(
                "OK"
            ) { dialog: DialogInterface?, i: Int -> dialog!!.dismiss() }
            .show()
    }


    fun showPayConfirmation(
        context: Context,
        recipientUsername: String?,
        recipientName: String,
        recipientProfileUrl: String?,
        amountToSend: Long,
        senderWalletBalance: Long,
        senderCommissionBalance: Long,
        onPay: Runnable?
    ): BottomSheetDialog {
        val binding = BottomSheetConfirmPaymentBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet)
        dialog.setContentView(binding.getRoot())

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        // Set blur background
        val rootView: View?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            rootView = (context as Activity).getWindow().getDecorView()
            rootView.setRenderEffect(RenderEffect.createBlurEffect(5f, 5f, Shader.TileMode.CLAMP))
        } else {
            rootView = null
        }

        // Conditions
        val SENDER_TOTAL_BALANCE = senderWalletBalance + senderCommissionBalance
        val IS_SENDER_TOTAL_BALANCE_SUFFICIENT = SENDER_TOTAL_BALANCE < amountToSend
        val IS_SENDER_WALLET_BALANCE_SUFFICIENT = senderWalletBalance >= amountToSend

        val PAYMENT_METHOD = if (IS_SENDER_WALLET_BALANCE_SUFFICIENT) "Wallet" else "ALL"

        if (IS_SENDER_TOTAL_BALANCE_SUFFICIENT) {
            // Not enough money at all
            val ERROR_INSUFFICIENT_BALANCE = "Insufficient"

            binding.txtFeedback.setVisibility(View.VISIBLE)
            binding.paymentMethod.setText(ERROR_INSUFFICIENT_BALANCE)

            // Hide debit breakdowns
            binding.debitFromSenderWalletBalance.setVisibility(View.GONE)
            binding.debitFromSenderCommissionBalance.setVisibility(View.GONE)
        } else if (IS_SENDER_WALLET_BALANCE_SUFFICIENT) {
            // Wallet balance alone is enough
            val DEBIT_FROM_SENDER_WALLET_BALANCE =
                "-" + CurrencyFormatter.formatToNaira(amountToSend)

            binding.paymentMethod.setText(PAYMENT_METHOD)
            binding.debitFromSenderWalletBalance.setVisibility(View.VISIBLE)
            binding.debitFromSenderWalletBalance.setText(DEBIT_FROM_SENDER_WALLET_BALANCE)

            binding.btnPay.setEnabled(true)

            // Hide commission since not used
            binding.debitFromSenderCommissionBalance.setVisibility(View.GONE)
        } else {
            // Wallet not enough, but wallet + commission is sufficient
            val fromWallet: Long
            fromWallet = senderWalletBalance
            val fromCommission = amountToSend - senderWalletBalance

            val DEBIT_FROM_SENDER_WALLET_BALANCE = "-" + CurrencyFormatter.formatToNaira(fromWallet)
            val DEBIT_FROM_SENDER_COMM_BALANCE =
                "-" + CurrencyFormatter.formatToNaira(fromCommission)

            binding.paymentMethod.setText(PAYMENT_METHOD)

            if (senderWalletBalance != 0L) {
                binding.debitFromSenderWalletBalance.setVisibility(View.VISIBLE)
                binding.debitFromSenderWalletBalance.setText(DEBIT_FROM_SENDER_WALLET_BALANCE)
            }

            binding.debitFromSenderCommissionBalance.setVisibility(View.VISIBLE)
            binding.debitFromSenderCommissionBalance.setText(DEBIT_FROM_SENDER_COMM_BALANCE)

            // Enable pay button
            binding.btnPay.setEnabled(true)

            // Hide feedback since covered
            binding.txtFeedback.setVisibility(View.GONE)
        }

        // Recipient details
        binding.amountToSendHeader.setText(CurrencyFormatter.formatToNaira(amountToSend))
        binding.amountToSend.setText(CurrencyFormatter.formatToNaira(amountToSend))
        binding.recipientUsername.setText(StringFormatter.addAtToPaymentId(recipientUsername))
        binding.recipientName.setText(recipientName.uppercase(Locale.getDefault()))
        loadProfilePic(recipientProfileUrl, binding.recipientProfilePic)

        // Sender details
        val SENDER_WALLET_BALANCE = "(" + CurrencyFormatter.formatToNaira(senderWalletBalance) + ")"
        val SENDER_COMM_BALANCE =
            "(" + CurrencyFormatter.formatToNaira(senderCommissionBalance) + ")"

        binding.senderTotalBalance.setText(CurrencyFormatter.formatToNaira(SENDER_TOTAL_BALANCE))
        binding.senderWalletBalance.setText(SENDER_WALLET_BALANCE)
        binding.senderCommissionBalance.setText(SENDER_COMM_BALANCE)

        // Handle buttons
        binding.btnClose.setOnClickListener(View.OnClickListener { v: View? ->
            dialog.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) rootView!!.setRenderEffect(null)
        })
        binding.btnPay.setOnClickListener(View.OnClickListener { v: View? ->
            if (onPay != null) {
                onPay.run()
            }
        })
        dialog.show()
        return dialog
    }

    fun showBottomSheetPaymentPinConfirmation(
        context: Context,
        config: BiConsumer<BottomSheetPaymentPinConfirmBinding?, Array<Runnable?>?>?
    ) {
        val binding = BottomSheetPaymentPinConfirmBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet)
        dialog.setContentView(binding.getRoot())

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        // setup click listener
        binding.btnClose.setOnClickListener(View.OnClickListener { view: View? -> dialog.dismiss() })
        binding.btnForgotPaymentPin.setOnClickListener(View.OnClickListener { view: View? -> })

        // disable the system keyboard
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (dialog.getCurrentFocus() != null) imm.hideSoftInputFromWindow(
            dialog.getCurrentFocus()!!.getWindowToken(), 0
        )
        binding.pinView.setShowSoftInputOnFocus(false)

        val onPinVerified = arrayOfNulls<Runnable>(1)

        // handle keypad input
        binding.numericKeypad.setOnKeypadInputListener(object : OnKeypadInputListener {
            override fun onNumberPressed(number: String?) {
                if (binding.pinView.length() < binding.pinView.getItemCount()) {
                    binding.pinView.append(number)
                }

                val pin =binding.pinView.getText().toString()

                if (pin.length == binding.pinView.getItemCount()) {
                    if (onPinVerified[0] != null) {
                        onPinVerified[0]!!.run()
                        dialog.dismiss()
                    }
                }
            }

            override fun onDeletePressed() {
                val current =binding.pinView.getText().toString()

                if (!current.isEmpty()) {
                    binding.pinView.setText(current.subSequence(0, current.length - 1))
                }
            }
        })
        if (config != null) config.accept(binding, onPinVerified)
        dialog.show()
    }

    fun showAlertDialogWithIcon(
        context: Context,
        config: BiConsumer<AlertDialog, AlertDialogWithIconBinding>
    ) {
        val binding = AlertDialogWithIconBinding.inflate(LayoutInflater.from(context))

        val builder =
            MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert_Rounded16dp)
                .setView(binding.getRoot())
                .setCancelable(false)

        val alertDialog = builder.create()

        config.accept(alertDialog, binding)
        alertDialog.show()
    }

    fun showAlertDialogMessage(
        context: Context,
        config: BiConsumer<AlertDialog, AlertDialogMessageBinding>
    ) {
        val binding = AlertDialogMessageBinding.inflate(LayoutInflater.from(context))

        val builder =
            MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert_Rounded16dp)
                .setView(binding.root)
                .setCancelable(false)

        val alertDialog = builder.create()

        config.accept(alertDialog, binding)
        alertDialog.show()
    }

    fun showBottomSheetImageSource(
        context: Context,
        config: BiConsumer<BottomSheetDialog, BottomSheetImageSourceBinding>
    ) {
        val binding = BottomSheetImageSourceBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet)
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        config.accept(dialog, binding)
        dialog.show()
    }
}