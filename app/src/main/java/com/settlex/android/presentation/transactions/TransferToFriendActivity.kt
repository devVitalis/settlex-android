package com.settlex.android.presentation.transactions

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.settlex.android.R
import com.settlex.android.data.enums.TransactionStatus
import com.settlex.android.data.remote.profile.ProfileService.loadProfilePhoto
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.databinding.ActivityTransferToFriendBinding
import com.settlex.android.domain.TransactionIdGenerator
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toNairaString
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.common.util.PaymentBottomSheetHelper
import com.settlex.android.presentation.settings.CreatePaymentPinActivity
import com.settlex.android.presentation.transactions.adapter.RecipientAdapter
import com.settlex.android.presentation.transactions.model.RecipientUiModel
import com.settlex.android.presentation.transactions.model.TransferToFriendUiModel
import com.settlex.android.presentation.transactions.viewmodel.TransactionViewModel
import com.settlex.android.util.string.CurrencyFormatter
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal

@AndroidEntryPoint
class TransferToFriendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransferToFriendBinding
    private val progressLoader by lazy { ProgressDialogManager(this) }
    private val recipientAdapter by lazy { RecipientAdapter() }
    private val viewModel: TransactionViewModel by viewModels()

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
        binding.shimmerEffect.stopShimmer()
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
    }


    private fun initViews() = with(binding) {
        StatusBar.setColor(this@TransferToFriendActivity, R.color.white)

        setupRecipientRecyclerView()
        setupTextWatchers()
        setupFocusHandlers()
        setupDescriptionDoneAction()

        btnBackBefore.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnVerify.setOnClickListener { searchRecipient(recipientPaymentId.orEmpty()) }
        btnNext.setOnClickListener { startPaymentProcess() }
    }

    private fun initObservers() {
        observeUserState()
        observeTransferToFriendEvent()
        observeGetRecipient()
        observePaymentPinAuth()
    }

    private fun observeUserState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userSessionState.collect { state ->
                    when (state) {
                        is UserSessionState.Authenticated -> displayUserInfo(state.user)
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun displayUserInfo(user: TransferToFriendUiModel) = with(binding) {
        currentUser = user
        availableBalance.text = user.totalBalance.toNairaString()
    }

    private fun observeTransferToFriendEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transferToFriendEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> goToStatus(TransactionStatus.SUCCESS, null)
                        is UiState.Failure -> goToStatus(
                            TransactionStatus.FAILED,
                            state.exception.message
                        )
                    }
                }
            }
        }
    }

    private fun observeGetRecipient() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getRecipientEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> showRecipientLoading()
                        is UiState.Success -> displayRecipientData(state.data)
                        is UiState.Failure -> handleRecipientFailure()
                    }
                }
            }
        }
    }

    private fun observePaymentPinAuth() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authPaymentPinEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onPinVerificationSuccess(state.data)
                        is UiState.Failure -> onPinVerificationError(state.exception.message)
                    }
                }
            }
        }
    }

    // Recipient UI handling
    private fun setupRecipientRecyclerView() = with(binding) {
        recipientRecyclerView.layoutManager = LinearLayoutManager(this@TransferToFriendActivity)
        recipientRecyclerView.adapter = recipientAdapter
        setupRecipientAdapterClick()
    }

    private fun setupRecipientAdapterClick() =with(binding) {
        recipientAdapter.setOnItemClickListener { recipient ->
            recipient ?: return@setOnItemClickListener
            editTxtPaymentId.setText(recipient.paymentId)
            editTxtPaymentId.setSelection(binding.editTxtPaymentId.text.length)
            btnVerify.isVisible = false

            // clear search results
            recipientAdapter.submitList(emptyList())
            recipientRecyclerView.gone()

            // show selected recipient
            recipientPhotoUrl = recipient.photoUrl
            loadProfilePhoto(recipientPhotoUrl, binding.selectedRecipientProfilePic)
            selectedRecipientName.text = recipient.fullName
            selectedRecipientPaymentId.text = recipient.paymentId
            selectedRecipient.show()

            updateNextButtonState()
        }
    }

    private fun showRecipientLoading() = with(binding) {
        // reset adapter and hide list
        recipientAdapter.submitList(emptyList())
        recipientRecyclerView.gone()

        // hide selected recipient and errors
        selectedRecipient.gone()
        txtError.gone()
        updateNextButtonState()

        shimmerEffect.show()
        shimmerEffect.startShimmer()
    }

    private fun displayRecipientData(recipientList: List<RecipientUiModel>) = with(binding) {
        shimmerEffect.stopShimmer()
        shimmerEffect.gone()

        if (recipientList.isEmpty()) {
            recipientAdapter.submitList(emptyList())
            val paymentId = StringFormatter.addAtToPaymentId(recipientPaymentId)
            txtError.text = "No user found with Payment ID $paymentId"
            txtError.show()
            recipientRecyclerView.gone()
            return
        }

        txtError.gone()
        recipientAdapter.submitList(recipientList.toMutableList())
        recipientRecyclerView.show()
    }

    private fun handleRecipientFailure() = with(binding) {
        shimmerEffect.stopShimmer()
        shimmerEffect.gone()
        // Optionally show a message or retry UI here
    }

    // Payment PIN / Transfer handling
    private fun onPinVerificationSuccess(isVerified: Boolean) {
        if (!isVerified) {
            showIncorrectPinDialog()
            return
        }

        // Start transaction
        val fromUid = currentUser?.uid ?: return
        startPayFriendTransaction(
            fromUid,
            recipientPaymentId ?: return,
            transferAmount,
            binding.editTxtDescription.text.toString().trim()
        )

        progressLoader.hide()
    }

    private fun onPinVerificationError(message: String?) {
        showSimpleAlertDialog(message)
        progressLoader.hide()
    }

    private fun showIncorrectPinDialog() {
        val message =
            "Incorrect PIN. Please try again, or click on the Forgot PIN to reset your PIN"
        val priButton = "Forgot Pin"
        val secButton = "Retry"

        DialogHelper.showAlertDialogMessage(
            this
        ) { dialog, dialogBinding ->
            dialogBinding.tvMessage.text = message
            dialogBinding.btnPrimary.text = priButton
            dialogBinding.btnSecondary.text = secButton

            dialogBinding.btnSecondary.setOnClickListener { dialog.dismiss() }
            dialogBinding.btnPrimary.setOnClickListener {
                // TODO pin Reset
            }
        }
    }

    private fun showSimpleAlertDialog(message: String?) {
        DialogHelper.showSimpleAlertDialog(this, "Error", message)
    }

    private fun startPaymentProcess() {
        // Validate current selections; selected recipient text contains formatted payment id
        val recipientPaymentIdRaw =
            StringFormatter.removeAtInPaymentId(binding.selectedRecipientPaymentId.text.toString())
        val recipientName = binding.selectedRecipientName.text.toString()

        bottomSheetDialog = PaymentBottomSheetHelper.showBottomSheetConfirmPayment(
            this,
            recipientPaymentIdRaw,
            recipientName,
            recipientPhotoUrl,
            transferAmount,
            currentUser?.balance ?: 0L,
            currentUser?.commissionBalance ?: 0L
        ) {
            // on confirm callback (keeps original behavior)
            if (currentUser?.hasPin != true) {
                promptTransactionPinCreation()
                return@showBottomSheetConfirmPayment
            }

//            DialogHelper.showBottomSheetPaymentPinConfirmation(this) { binding, runnable ->
//                runnable[0] = {
//                    // call viewModel to authenticate pin (keeps existing flow)
//                    viewModel.authPaymentPin(binding.pinView.text.toString())
//                }
//            }
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

        DialogHelper.showAlertDialogWithIcon(
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
        editTxtPaymentId.doOnTextChanged { text, _, _, _ ->
            val raw = text?.toString().orEmpty().trim()
            recipientPaymentId = if (raw.isNotEmpty()) {
                StringFormatter.removeAtInPaymentId(raw.lowercase())
            } else {
                null
            }

            txtError.gone()
            selectedRecipient.gone()
            btnVerify.isVisible = raw.length >= 5
            updateNextButtonState()
        }

        editTxtAmount.doOnTextChanged { raw, _, _, _ ->
            val rawStr = raw?.toString().orEmpty().replace(",", "")
            transferAmount =
                if (rawStr.isBlank()) 0L else CurrencyFormatter.convertNairaStringToKobo(rawStr)
            val isAmountEmpty = rawStr.isBlank()
            val shouldShowError = !isAmountInRange(transferAmount) && !isAmountEmpty

            txtAmountFeedback.text = if (shouldShowError) ERROR_INVALID_AMOUNT else ""
            txtAmountFeedback.isVisible = shouldShowError

            updateNextButtonState()
        }
    }

    private fun setupFocusHandlers() = with(binding) {
//        editTxtPaymentId.setOnFocusChangeListener { _, hasFocus ->
//            editTxtPaymentIdBackground.setBackgroundResource(
//                if (hasFocus) R.drawable.bg_edit_txt_custom_white_focused else R.drawable.`bg_input_field_filled.xml`
//            )
//        }
//
//        editTxtDescription.setOnFocusChangeListener { _, hasFocus ->
//            editTxtDescriptionBackground.setBackgroundResource(
//                if (hasFocus) R.drawable.bg_edit_txt_custom_white_focused else R.drawable.`bg_input_field_filled.xml`
//            )
//        }
//
//        editTxtAmount.setOnFocusChangeListener { _, hasFocus ->
//            editTxtAmountBackground.setBackgroundResource(
//                if (hasFocus) R.drawable.bg_edit_txt_custom_white_focused else R.drawable.`bg_input_field_filled.xml`
//            )

            val rawInput = editTxtAmount.text.toString().trim()
            if (rawInput.isEmpty()) return

            val numericValue = editTxtAmount.toBigDecimalSafe()

//            if (hasFocus) {
//                val cleanNumber = numericValue.toPlainString()
//                editTxtAmount.setText(cleanNumber)
//                editTxtAmount.setSelection(cleanNumber.length)
//                return@setOnFocusChangeListener
//            }

            val currencyFormat = CurrencyFormatter.formatToCurrency(numericValue)
            editTxtAmount.setText(currencyFormat)
            editTxtAmount.setSelection(currencyFormat.length)
//        }
    }

    private fun setupDescriptionDoneAction() {
        binding.editTxtDescription.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(v)
                v.clearFocus()
                true
            } else false
        }
    }

    private fun updateNextButtonState() {
        val recipientSelected = binding.selectedRecipient.isVisible
        binding.btnNext.isEnabled =
            isPaymentIdValid(recipientPaymentId) && isAmountInRange(transferAmount) && recipientSelected
    }

    private fun isPaymentIdValid(paymentId: String?): Boolean {
        return paymentId != null && PAYMENT_ID_REGEX.matches(paymentId)
    }

    private fun isAmountInRange(amount: Long): Boolean {
        // amount in kobo (smallest unit)
        return amount >= 10_000L && amount <= 100_000_000L
    }

    private fun goToStatus(transactionStatus: TransactionStatus, feedback: String?) {
        bottomSheetDialog?.dismiss()
        val formattedAmount = transferAmount.toNairaString()
        val intent = Intent(this, TransactionStatusActivity::class.java).apply {
            putExtra("amount", formattedAmount)
            putExtra("status", transactionStatus.name)
            putExtra("message", feedback)
        }
        startActivity(intent)
        finish()

        progressLoader.hide()
    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun EditText.toBigDecimalSafe(): BigDecimal {
        val s = this.text.toString().trim().replace(",", "")
        return if (s.isBlank()) BigDecimal.ZERO else try {
            BigDecimal(s)
        } catch (_: Throwable) {
            BigDecimal.ZERO
        }
    }

    companion object {
        private const val ERROR_INVALID_AMOUNT = "Amount must be in range of ₦100 - ₦1,000,000.00"
        private const val ERROR_CANNOT_SEND_TO_SELF =
            "You cannot send a payment to your own account. Please choose a different recipient"
        private val PAYMENT_ID_REGEX = "^@?[A-Za-z][A-Za-z0-9]{4,19}$".toRegex()
    }
}
