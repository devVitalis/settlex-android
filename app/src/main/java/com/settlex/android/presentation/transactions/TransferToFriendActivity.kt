package com.settlex.android.presentation.transactions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cottacush.android.currencyedittext.CurrencyInputWatcher
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.settlex.android.R
import com.settlex.android.data.enums.TransactionStatus
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.remote.profile.ProfileService.loadProfilePhoto
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.databinding.ActivityTransferToFriendBinding
import com.settlex.android.domain.TransactionIdGenerator
import com.settlex.android.presentation.common.extensions.addAtPrefix
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.removeAtPrefix
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toNairaString
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.common.util.KeyboardHelper
import com.settlex.android.presentation.common.util.PaymentBottomSheetHelper
import com.settlex.android.presentation.settings.CreatePaymentPinActivity
import com.settlex.android.presentation.transactions.adapter.RecipientAdapter
import com.settlex.android.presentation.transactions.model.RecipientUiModel
import com.settlex.android.presentation.transactions.model.TransferToFriendUiModel
import com.settlex.android.presentation.transactions.viewmodel.TransactionViewModel
import com.settlex.android.util.string.CurrencyFormatter
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Locale

@AndroidEntryPoint
class TransferToFriendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransferToFriendBinding
    private val progressLoader by lazy { ProgressDialogManager(this) }
    private val recipientAdapter by lazy { RecipientAdapter() }
    private val viewModel: TransactionViewModel by viewModels()
    private val keyboardHelper by lazy { KeyboardHelper(this) }

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var recipientPhotoUrl: String? = null
    private var recipientPaymentId: String? = null
    private var transferAmount: Long = 0L
    private var currentUser: TransferToFriendUiModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferToFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
    }

    private fun initViews() = with(binding) {
        StatusBar.setColor(this@TransferToFriendActivity, R.color.colorSurfaceVariant)

        initRecipientRecyclerView()
        setupTextWatchers()
        keyboardHelper.attachDoneAction(etDescription)

        btnBackBefore.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnVerify.setOnClickListener { searchRecipient(recipientPaymentId.orEmpty()) }
        btnNext.setOnClickListener { startPaymentProcess() }
    }

    private fun initObservers() {
        observeUserSession()
        observeTransferToFriendEvent()
        observeGetRecipient()
        observePaymentPinAuth()
    }

    private fun observeUserSession() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userSessionState.collect { state ->
                    when (state) {
                        is UserSessionState.Authenticated -> setUser(state.user)
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setUser(user: TransferToFriendUiModel) = with(binding) {
        tvAvailableBalance.text = user.totalBalance.toNairaString()
        currentUser = user
    }

    private fun observeTransferToFriendEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transferToFriendEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> showTransferStatus(TransactionStatus.SUCCESS, null)
                        is UiState.Failure -> showTransferStatus(
                            TransactionStatus.FAILED,
                            state.exception
                        )
                    }
                }
            }
        }
    }

    private fun showTransferStatus(transactionStatus: TransactionStatus, error: AppException?) {
        val formattedAmount = transferAmount.toNairaString()
        val intent = Intent(this, TransactionStatusActivity::class.java).apply {
            putExtra("amount", formattedAmount)
            putExtra("status", transactionStatus.name)
            putExtra("message", error?.message)
        }
        startActivity(intent)
        finish()

        bottomSheetDialog?.dismiss()
        progressLoader.hide()
    }

    private fun observeGetRecipient() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getRecipientEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> startRecipientShimmerLoading()
                        is UiState.Success -> setRecipientData(state.data)
                        is UiState.Failure -> onGetRecipientError(state.exception)
                    }
                }
            }
        }
    }

    private fun startRecipientShimmerLoading() = with(binding) {
        recipientAdapter.submitList(emptyList())

        listOf(
            rvRecipient,
            viewSelectedRecipient,
            tvError
        ).forEach { it.gone() }

        shimmerEffect.show()
        updateNextButtonState()
    }

    private fun setRecipientData(recipientList: List<RecipientUiModel>) = with(binding) {
        shimmerEffect.gone()

        if (recipientList.isEmpty()) {
            recipientAdapter.submitList(emptyList())
            "No user found with Payment ID ${recipientPaymentId?.addAtPrefix()}".also {
                tvError.text = it
                tvError.show()
            }

            rvRecipient.gone()
            return
        }

        tvError.gone()
        rvRecipient.show()
        recipientAdapter.submitList(recipientList)
    }

    private fun onGetRecipientError(error: AppException) = with(binding) {
        shimmerEffect.gone()

        tvError.show()
        tvError.text = error.message
    }

    private fun observePaymentPinAuth() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authPaymentPinEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onPinVerificationSuccess(state.data)
                        is UiState.Failure -> onPinVerificationError(state.exception)
                    }
                }
            }
        }
    }

    private fun onPinVerificationSuccess(isVerified: Boolean) = with(binding) {
        if (!isVerified) {
            showPinErrorDialog()
            return
        }

        startPayFriendTransaction(
            currentUser?.uid!!,
            recipientPaymentId!!,
            transferAmount,
            etAmount.text.toString().trim()
        )

        progressLoader.hide()
    }

    private fun onPinVerificationError(error: AppException) {
        showSimpleAlertDialog(error.message)
        progressLoader.hide()
    }

    private fun showPinErrorDialog() {
        DialogHelper.showCustomAlertDialog(this) { dialog, dialogBinding ->
            with(dialogBinding) {
                "Incorrect PIN. Try again or reset your PIN".also { tvMessage.text = it }
                "Forgot PIN".also { btnSecondary.text = it }
                "Retry".also { btnPrimary.text = it }

                btnPrimary.setOnClickListener { dialog.dismiss() }
                btnSecondary.setOnClickListener {
                    // TODO pin Reset
                    dialog.dismiss()
                }
            }
        }
    }

    private fun showSimpleAlertDialog(message: String?) {
        DialogHelper.showSimpleAlertDialog(this, "Error", message)
    }

    private fun initRecipientRecyclerView() = with(binding) {
        rvRecipient.layoutManager = LinearLayoutManager(this@TransferToFriendActivity)
        rvRecipient.adapter = recipientAdapter

        onRecipientSelected()
    }

    private fun onRecipientSelected() = with(binding) {
        recipientAdapter.setOnRecipientClickListener(object : RecipientAdapter.OnItemClickListener {
            override fun onItemClick(selectedRecipient: RecipientUiModel) {
                etPaymentId.setText(selectedRecipient.paymentId)
                etPaymentId.setSelection(etPaymentId.length())
                btnVerify.gone()

                // Clear search results
                recipientAdapter.submitList(emptyList())
                rvRecipient.gone()

                // Set selected recipient
                recipientPhotoUrl = selectedRecipient.photoUrl
                loadProfilePhoto(selectedRecipient.photoUrl, ivSelectedRecipientProfilePhoto)
                tvSelectedRecipientName.text = selectedRecipient.fullName
                tvSelectedRecipientPaymentId.text = selectedRecipient.paymentId.addAtPrefix()
                viewSelectedRecipient.show()

                updateNextButtonState()
            }
        })
    }


    private fun startPaymentProcess() = with(binding) {
        // Validate current selections; selected recipient text contains formatted payment id
        val recipientPaymentIdRaw = tvSelectedRecipientPaymentId.text.toString().removeAtPrefix()
        val recipientName = tvSelectedRecipientName.text.toString()

        bottomSheetDialog = PaymentBottomSheetHelper.showBottomSheetConfirmPayment(
            this@TransferToFriendActivity,
            recipientPaymentIdRaw,
            recipientName,
            recipientPhotoUrl,
            transferAmount,
            currentUser?.balance ?: 0L,
            currentUser?.commissionBalance ?: 0L
        ) {
            // on confirm callback
            currentUser?.hasPin?.let { hasPin ->
                if (!hasPin) {
                    promptTransactionPinCreation()
                    return@showBottomSheetConfirmPayment
                }
            }

            DialogHelper.showBottomSheetPaymentPinConfirmation(this@TransferToFriendActivity) { binding, runnable ->
                runnable?.set(0) {
                    viewModel.authPaymentPin(binding?.pinView?.text.toString())
                }
            }
        }
    }

    private fun startPayFriendTransaction(
        fromSenderUid: String,
        toRecipient: String,
        amount: Long,
        description: String?
    ) {
        viewModel.transferToFriend(
            fromSenderUid,
            toRecipient,
            TransactionIdGenerator.generate(fromSenderUid),
            amount,
            description
        )
    }

    private fun promptTransactionPinCreation() {
        val title = "Payment PIN Required"
        val message = "Please set up your Payment PIN to complete this transaction securely"
        val btnPriText = "Create PIN"
        val btnSecText = "Cancel"

        DialogHelper.showCustomAlertDialogWithIcon(
            this,
            { dialog, dialogBinding ->
                dialogBinding.title.text = title
                dialogBinding.message.text = message
                dialogBinding.btnPrimary.text = btnPriText
                dialogBinding.btnSecondary.text = btnSecText
                dialogBinding.icon.setImageResource(R.drawable.ic_lock_filled)

                dialogBinding.btnSecondary.setOnClickListener { dialog.dismiss() }
                dialogBinding.btnPrimary.setOnClickListener {
                    startActivity(Intent(this, CreatePaymentPinActivity::class.java))
                    dialog.dismiss()
                }
            }
        )
    }

    private fun searchRecipient(paymentId: String) {
        if (paymentId.isBlank()) return

        // Prevent sending to self
//        val stripped = StringFormatter.removeAtInPaymentId(paymentId)
//        if (stripped == currentUser?.paymentId) {
//            binding.txtError.text = ERROR_CANNOT_SEND_TO_SELF
//            binding.txtError.show()
//            return
//        }

        viewModel.getRecipientByPaymentId(paymentId)
    }

    // Text watchers + focus handlers
    private fun setupTextWatchers() = with(binding) {
        etPaymentId.doOnTextChanged { text, _, _, _ ->
            val raw = text.toString().trim()
            recipientPaymentId = if (raw.isNotEmpty()) raw.lowercase().removeAtPrefix() else null

            tvError.gone()
            viewSelectedRecipient.gone()
            btnVerify.isVisible = raw.length >= 5

            updateNextButtonState()
        }

        etAmount.addTextChangedListener(CurrencyInputWatcher(etAmount, "₦", Locale.getDefault(), 2))
        etAmount.doOnTextChanged { amount, _, _, _ ->
            Log.d("Transfer", "Raw Amount: ${amount.toString()}")
            val rawStr = amount.toString().replace(",", "")
            Log.d("Transfer", "Formatted Amount: $rawStr")

            transferAmount =
                if (rawStr.isBlank()) 0L else CurrencyFormatter.convertNairaStringToKobo(rawStr)
            val isAmountEmpty = rawStr.isBlank()
            val shouldShowError = !isAmountInRange(transferAmount) && !isAmountEmpty

            tvAmountFeedback.text = if (shouldShowError) ERROR_INVALID_AMOUNT else ""
            tvAmountFeedback.isVisible = shouldShowError

            updateNextButtonState()
        }

    }

    private fun setupFocusHandlers() = with(binding) {
        val rawInput = etAmount.text.toString().trim()
        if (rawInput.isEmpty()) return

        val numericValue = etAmount.toBigDecimalSafe()
        Log.d("Transfer", "Numeric value: $numericValue")

        etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val amountString = numericValue.toPlainString()
                etAmount.setText(amountString)
                etAmount.setSelection(amountString.length)
                return@setOnFocusChangeListener
            }
        }

        val currencyFormat = CurrencyFormatter.formatToCurrency(numericValue)
        Log.d("Transfer", "Currency format: $currencyFormat")
        etAmount.setText(currencyFormat)
        etAmount.setSelection(currencyFormat.length)
    }

    private fun EditText.toBigDecimalSafe(): BigDecimal {
        val cleanedNumberString = this.text.toString().trim().replace(",", "")

        return if (cleanedNumberString.isBlank()) BigDecimal.ZERO else try {
            BigDecimal(cleanedNumberString)
        } catch (_: NumberFormatException) {
            BigDecimal.ZERO
        }
    }

    private fun updateNextButtonState() {
        val recipientSelected = binding.viewSelectedRecipient.isVisible
        binding.btnNext.isEnabled =
            isPaymentIdValid(recipientPaymentId) && isAmountInRange(transferAmount) && recipientSelected
    }

    private fun isPaymentIdValid(paymentId: String?): Boolean {
        return paymentId != null && PAYMENT_ID_REGEX.matches(paymentId)
    }

    private fun isAmountInRange(amount: Long): Boolean {
        return amount >= 10_000L && amount <= 100_000_000L
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (keyboardHelper.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }

    companion object {
        private const val ERROR_INVALID_AMOUNT = "Amount must be in range of ₦100 - ₦1,000,000.00"
        private const val ERROR_CANNOT_SEND_TO_SELF =
            "You cannot send a payment to your own account. Please choose a different recipient"
        private val PAYMENT_ID_REGEX = "^@?[A-Za-z][A-Za-z0-9]{4,19}$".toRegex()
    }
}
