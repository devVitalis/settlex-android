package com.settlex.android.presentation.common.util

import android.app.Activity
import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.settlex.android.R
import com.settlex.android.data.remote.profile.ProfileService.loadProfilePhoto
import com.settlex.android.databinding.BottomSheetConfirmPaymentBinding
import com.settlex.android.databinding.BottomSheetPaymentPinAuthBinding
import com.settlex.android.presentation.common.custom.NumericKeypad.OnKeypadInputListener
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toNairaString

/**
 * A helper object for managing payment-related bottom sheet dialogs.
 */
object PaymentBottomSheetHelper {
    fun showConfirmPaymentBottomSheet(
        context: Context,
        recipientPaymentId: String,
        recipientName: String,
        recipientPhotoUrl: String?,
        transferAmount: Long,
        senderWalletBalance: Long,
        senderCommissionBalance: Long,
        onConfirmTransfer: () -> Unit
    ): BottomSheetDialog {
        val binding = BottomSheetConfirmPaymentBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet).apply {
            setContentView(binding.root)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }

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

        // Calculate transaction summary
        val transactionSummary = generateTransactionSummary(
            senderWalletBalance,
            senderCommissionBalance,
            transferAmount
        )

        // Update UI based on transaction summary
        showTransactionSummary(binding, transactionSummary)

        // Set recipient and sender details
        updateRecipientDetails(binding, recipientPaymentId, recipientName, recipientPhotoUrl)
        updateSenderDetails(binding, senderWalletBalance, senderCommissionBalance)

        // Set click listeners
        with(binding) {
            btnConfirmTransfer.setOnClickListener { onConfirmTransfer() }
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
        paymentId: String,
        name: String,
        photoUrl: String?
    ) {
        with(binding) {
            tvRecipientPaymentId.text = paymentId
            tvRecipientName.text = name.uppercase()
            loadProfilePhoto(photoUrl, ivRecipientProfilePhoto)
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

    fun showTransactionSummary(
        binding: BottomSheetConfirmPaymentBinding,
        summary: TransactionSummary
    ) =
        with(binding) {
            listOf(
                tvTransferAmountHeader,
                tvTransferAmount
            ).forEach {
                it.text = summary.transferAmount.toNairaString()
            }

            // Update button state
            btnConfirmTransfer.isEnabled = summary.canProceed
            tvPaymentMethod.text = summary.debitSource

            summary.statusMessage?.let {
                tvFeedback.text = summary.statusMessage
                tvFeedback.show()
            }

            // Show debit summary
            when (summary.walletDebit > 0) {
                true -> {
                    tvDebitFromSenderWalletBalance.show()
                    "-${summary.walletDebit.toNairaString()}".also {
                        tvDebitFromSenderWalletBalance.text = it
                    }
                }

                else -> tvDebitFromSenderWalletBalance.gone()
            }

            when (summary.commissionDebit > 0) {
                true -> {
                    tvDebitFromSenderCommissionBalance.show()
                    "-${summary.commissionDebit.toNairaString()}".also {
                        tvDebitFromSenderCommissionBalance.text = it
                    }
                }

                else -> tvDebitFromSenderCommissionBalance.gone()
            }
        }

    fun generateTransactionSummary(
        senderWalletBalance: Long,
        senderCommissionBalance: Long,
        transferAmount: Long
    ): TransactionSummary {
        val senderTotalAvailableBalance = senderWalletBalance + senderCommissionBalance
        return when {
            senderTotalAvailableBalance < transferAmount -> {
                // Balance and commission are insufficient
                TransactionSummary(
                    transferAmount = transferAmount,
                    canProceed = false,
                    debitSource = "INSUFFICIENT",
                    walletDebit = 0,
                    commissionDebit = 0,
                    statusMessage = "Insufficient balance"
                )
            }

            senderWalletBalance >= transferAmount -> {
                // Wallet alone is sufficient
                TransactionSummary(
                    transferAmount = transferAmount,
                    canProceed = true,
                    debitSource = "WALLET",
                    walletDebit = transferAmount,
                    commissionDebit = 0,
                    statusMessage = null
                )
            }

            else -> {
                // Needs both wallet and commission
                TransactionSummary(
                    transferAmount = transferAmount,
                    canProceed = true,
                    debitSource = "WALLET_AND_COMMISSION",
                    walletDebit = senderWalletBalance,
                    commissionDebit = transferAmount - senderWalletBalance,
                    statusMessage = null
                )
            }
        }
    }

    data class TransactionSummary(
        val transferAmount: Long,
        val canProceed: Boolean,
        val debitSource: String,
        val walletDebit: Long,
        val commissionDebit: Long,
        val statusMessage: String?
    )

    fun showPaymentPinAuthenticationBottomSheet(
        context: Context,
        onPinEntered: (pin: String) -> Unit
    ) {
        val binding = BottomSheetPaymentPinAuthBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet).apply {
            setContentView(binding.root)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }

        with(binding) {
            btnClose.setOnClickListener { dialog.dismiss() }
            tvForgotPaymentPin.setOnClickListener { }

            // Disable system keyboard
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
                dialog.currentFocus?.let { hideSoftInputFromWindow(it.windowToken, 0) }
            }
            pinView.showSoftInputOnFocus = false

            val maxPinLength = pinView.itemCount

            btnNumericKeypad.setOnKeypadInputListener(object : OnKeypadInputListener {
                override fun onNumberPressed(number: String) {
                    if (pinView.length() < maxPinLength) pinView.append(number)

                    if (pinView.length() == maxPinLength) {
                        onPinEntered(pinView.text.toString())
                        dialog.dismiss()
                    }
                }

                override fun onDeletePressed() {
                    val current = pinView.text.toString()
                    if (current.isNotEmpty()) pinView.setText(current.dropLast(1))
                }
            })
            dialog.show()
        }
    }
}