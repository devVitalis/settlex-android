package com.settlex.android.presentation.common.util

import android.app.Activity
import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.settlex.android.R
import com.settlex.android.data.remote.profile.ProfileService.loadProfilePhoto
import com.settlex.android.databinding.BottomSheetConfirmPaymentBinding
import com.settlex.android.presentation.common.extensions.addAtPrefix
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toNairaString

/**
 * Utility class for payment-related bottom sheet dialogs and operations.
 * Handles payment confirmation UI, balance calculations, and payment breakdowns.
 */
object PaymentBottomSheetHelper {
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
        senderWalletBalance: Long,
        senderCommissionBalance: Long,
        transferAmount: Long
    ): PaymentBreakdown {
        val senderTotalAvailableBalance = senderWalletBalance + senderCommissionBalance
        return when {
            senderTotalAvailableBalance < transferAmount -> {
                // Balance and commission are insufficient
                PaymentBreakdown(
                    canProceed = false,
                    debitSource = "INSUFFICIENT",
                    walletDebit = 0,
                    commissionDebit = 0,
                    feedbackMessage = "Insufficient balance"
                )
            }

            senderWalletBalance >= transferAmount -> {
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
                    walletDebit = senderWalletBalance,
                    commissionDebit = transferAmount - senderWalletBalance,
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
}