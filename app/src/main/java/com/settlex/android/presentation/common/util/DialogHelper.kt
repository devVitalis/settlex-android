package com.settlex.android.presentation.common.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.settlex.android.R
import com.settlex.android.data.remote.profile.ProfileService.loadProfilePhoto
import com.settlex.android.databinding.AlertDialogMessageBinding
import com.settlex.android.databinding.AlertDialogWithIconBinding
import com.settlex.android.databinding.BottomSheetConfirmPaymentBinding
import com.settlex.android.databinding.BottomSheetImageSourceBinding
import com.settlex.android.databinding.BottomSheetPaymentPinConfirmBinding
import com.settlex.android.databinding.BottomSheetSuccessDialogBinding
import com.settlex.android.presentation.common.custom.NumericKeypad.OnKeypadInputListener
import com.settlex.android.presentation.common.extensions.addAtPrefix
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toNairaString
import java.util.function.BiConsumer

/**
 * Utility class for common UI operations
 * Provides reusable methods for displaying consistent UI components throughout the application.
 */
object DialogHelper {

    // Alert dialogs
    fun showAlertDialogWithIcon(
        context: Context,
        config: BiConsumer<AlertDialog, AlertDialogWithIconBinding>
    ) {
        val binding = AlertDialogWithIconBinding.inflate(LayoutInflater.from(context))
        val dialog =
            MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert_Rounded16dp)
                .setView(binding.getRoot())
                .setCancelable(false)
                .create()

        config.accept(dialog, binding)
        dialog.show()
    }

    fun showAlertDialogMessage(
        context: Context,
        config: BiConsumer<AlertDialog, AlertDialogMessageBinding>
    ) {
        val binding = AlertDialogMessageBinding.inflate(LayoutInflater.from(context))
        val dialog =
            MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert_Rounded16dp)
                .setView(binding.root)
                .setCancelable(false)
                .create()

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


    // Bottom sheet dialogs
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

    fun showBottomSheetConfirmPayment(
        context: Context,
        recipientUsername: String?,
        recipientName: String,
        recipientProfileUrl: String?,
        transferAmount: Long,
        senderWalletBalance: Long,
        senderCommissionBalance: Long,
        onPay: Runnable?
    ): BottomSheetDialog {
        val binding = BottomSheetConfirmPaymentBinding.inflate(LayoutInflater.from(context))
        with(binding) {
            val dialog = BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet)
            dialog.setContentView(binding.root)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            // Apply blur if Android 12+
            var rootView: View? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                rootView = (context as Activity).window.decorView
                rootView.setRenderEffect(
                    RenderEffect.createBlurEffect(
                        5f,
                        5f,
                        Shader.TileMode.CLAMP
                    )
                )
            }

            // Calculate payment breakdown
            val paymentBreakdown = calculatePaymentBreakdown(
                senderWalletBalance,
                senderCommissionBalance,
                transferAmount
            )

            // Update UI based on breakdown
            updatePaymentUI(binding, paymentBreakdown)

            // Set recipient and sender details
            updateRecipientDetails(binding, recipientUsername, recipientName, recipientProfileUrl)
            updateSenderDetails(binding, senderWalletBalance, senderCommissionBalance)

            // Set click listeners
            btnPay.setOnClickListener { onPay?.run() }
            btnClose.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) rootView?.setRenderEffect(null)
                dialog.dismiss()
            }

            dialog.show()
            return dialog
        }
    }

    fun updateRecipientDetails(
        binding: BottomSheetConfirmPaymentBinding,
        username: String?,
        name: String,
        profileUrl: String?
    ) {
        with(binding) {
            tvRecipientUsername.text = username?.addAtPrefix()
            tvRecipientName.text = name.uppercase()
            loadProfilePhoto(profileUrl, ivRecipientProfilePhoto)
        }
    }

    fun updateSenderDetails(
        binding: BottomSheetConfirmPaymentBinding,
        walletBalance: Long,
        commissionBalance: Long
    ) {
        with(binding) {
            val totalBalance = walletBalance + commissionBalance
            tvSenderTotalBalance.text = totalBalance.toNairaString()
            "(${walletBalance.toNairaString()})".also { tvSenderWalletBalance.text = it }
            "(${commissionBalance.toNairaString()})".also { tvSenderCommissionBalance.text = it }
        }
    }

    fun updatePaymentUI(binding: BottomSheetConfirmPaymentBinding, breakdown: PaymentBreakdown) {
        with(binding) {
            btnPay.isEnabled = breakdown.canProceed
            tvPaymentMethod.text = breakdown.debitSource

            if (breakdown.feedbackMessage != null) {
                tvFeedback.show()
                tvFeedback.text = breakdown.feedbackMessage
            } else {
                tvFeedback.gone()
            }

            // Update debit amounts
            if (breakdown.walletDebit > 0) {
                tvDebitFromSenderWalletBalance.show()
                "-${breakdown.walletDebit.toNairaString()}".also {
                    tvDebitFromSenderWalletBalance.text = it
                }
            } else {
                tvDebitFromSenderWalletBalance.gone()
            }

            if (breakdown.commissionDebit > 0) {
                tvDebitFromSenderCommissionBalance.show()
                "-${breakdown.commissionDebit.toNairaString()}".also {
                    tvDebitFromSenderCommissionBalance.text = it
                }
            } else {
                tvDebitFromSenderCommissionBalance.gone()
            }
        }
    }

    fun calculatePaymentBreakdown(
        walletBalance: Long,
        commissionBalance: Long,
        transferAmount: Long
    ): PaymentBreakdown {
        val totalAvailable = walletBalance + commissionBalance

        return when {
            totalAvailable < transferAmount -> {
                // Can't afford it at all
                PaymentBreakdown(
                    canProceed = false,
                    debitSource = "INSUFFICIENT",
                    walletDebit = 0,
                    commissionDebit = 0,
                    feedbackMessage = "Insufficient balance"
                )
            }

            walletBalance >= transferAmount -> {
                // Wallet covers everything
                PaymentBreakdown(
                    canProceed = true,
                    debitSource = "WALLET",
                    walletDebit = transferAmount,
                    commissionDebit = 0,
                    feedbackMessage = null
                )
            }

            else -> {
                // Need both wallet and commission
                PaymentBreakdown(
                    canProceed = true,
                    debitSource = "WALLET_AND_COMMISSION",
                    walletDebit = walletBalance,
                    commissionDebit = transferAmount - walletBalance,
                    feedbackMessage = null
                )
            }
        }
    }

    data class PaymentBreakdown(
        val canProceed: Boolean,
        val debitSource: String,
        val walletDebit: Long,
        val commissionDebit: Long,
        val feedbackMessage: String?
    )

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
        binding.btnClose.setOnClickListener { dialog.dismiss() }
        binding.btnForgotPaymentPin.setOnClickListener { }

        // disable the system keyboard
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (dialog.currentFocus != null) imm.hideSoftInputFromWindow(
            dialog.currentFocus!!.windowToken, 0
        )
        binding.pinView.showSoftInputOnFocus = false

        val onPinVerified = arrayOfNulls<Runnable>(1)

        // handle keypad input
        binding.numericKeypad.setOnKeypadInputListener(object : OnKeypadInputListener {
            override fun onNumberPressed(number: String?) {
                if (binding.pinView.length() < binding.pinView.itemCount) {
                    binding.pinView.append(number)
                }

                val pin = binding.pinView.getText().toString()

                if (pin.length == binding.pinView.itemCount) {
                    if (onPinVerified[0] != null) {
                        onPinVerified[0]!!.run()
                        dialog.dismiss()
                    }
                }
            }

            override fun onDeletePressed() {
                val current = binding.pinView.getText().toString()

                if (!current.isEmpty()) {
                    binding.pinView.setText(current.subSequence(0, current.length - 1))
                }
            }
        })
        config?.accept(binding, onPinVerified)
        dialog.show()
    }
}