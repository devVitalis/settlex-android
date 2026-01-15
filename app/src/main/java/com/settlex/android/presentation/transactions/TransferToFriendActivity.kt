package com.settlex.android.presentation.transactions

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
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
import com.settlex.android.presentation.common.extensions.addAtPrefix
import com.settlex.android.presentation.common.extensions.fromNairaStringToKobo
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.removeAtPrefix
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toNairaString
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.common.util.FocusManager
import com.settlex.android.presentation.common.util.PaymentBottomSheetHelper
import com.settlex.android.presentation.common.util.ValidationUtil
import com.settlex.android.presentation.settings.CreatePaymentPinActivity
import com.settlex.android.presentation.transactions.adapter.RecipientAdapter
import com.settlex.android.presentation.transactions.model.RecipientUiModel
import com.settlex.android.presentation.transactions.model.TransactionResult
import com.settlex.android.presentation.transactions.model.TransferToFriendUiModel
import com.settlex.android.presentation.transactions.viewmodel.TransactionViewModel
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class TransferToFriendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransferToFriendBinding
    private val viewModel: TransactionViewModel by viewModels()
    private val progressLoader by lazy { ProgressDialogManager(this) }
    private val focusManager by lazy { FocusManager(this) }
    private lateinit var recipientAdapter: RecipientAdapter

    private var paymentConfirmationSheet: BottomSheetDialog? = null
    private var recipientPhotoUrl: String? = null
    private var _currentUser: TransferToFriendUiModel? = null
    val currentUser get() = _currentUser!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferToFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        paymentConfirmationSheet?.dismiss()
        paymentConfirmationSheet = null
    }

    private fun initViews() = with(binding) {
        StatusBar.setColor(this@TransferToFriendActivity, R.color.colorSurfaceVariant)

        initRecipientRecyclerView()
        initInputListeners()
        focusManager.attachDoneAction(etDescription)

        btnBackBefore.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnVerify.setOnClickListener { fetchRecipientData(getRecipientPaymentId()) }
        btnNext.setOnClickListener { showPaymentConfirmation() }
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
        _currentUser = user
    }

    private fun observeTransferToFriendEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transferToFriendEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> showTransferStatus(
                            TransactionStatus.SUCCESS,
                            state.data,
                            null
                        )

                        is UiState.Failure -> showTransferStatus(
                            TransactionStatus.FAILED,
                            null,
                            state.exception
                        )
                    }
                }
            }
        }
    }


    private fun showTransferStatus(
        transactionStatus: TransactionStatus,
        message: String?,
        error: AppException?
    ) {
        val result = TransactionResult(
            status = transactionStatus,
            amount = getAmountInKobo(),
            message = message ?: "Transaction processing",
            errorMessage = error?.message
        )

        val intent = Intent(this, TransactionStatusActivity::class.java).apply {
            putExtra("transaction_result", result)
        }

        startActivity(intent)
        finish()
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
            "No user found with Payment ID ${getRecipientPaymentId().addAtPrefix()}".also {
                tvError.text = it
                tvError.show()
            }

            rvRecipient.gone()
            return
        }

        tvError.gone()
        rvRecipient.show()
        recipientAdapter.submitList(recipientList)
        rvRecipient.adapter = recipientAdapter
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
            showPinAuthFailureDialog(true)
            return
        }

        sendPayment()
        progressLoader.hide()
    }

    private fun sendPayment() = with(binding) {
        viewModel.transferToFriend(
            toRecipientPaymentId = getRecipientPaymentId(),
            transferAmount = getAmountInKobo(),
            description = etDescription.text.toString().trim()
        )
    }

    private fun onPinVerificationError(error: AppException) {
        showPinAuthFailureDialog(false, error)
        progressLoader.hide()
    }

    private fun showPinAuthFailureDialog(isPinIncorrect: Boolean, error: AppException? = null) {
        DialogHelper.showCustomAlertDialog(this) { dialog, dialogBinding ->
            with(dialogBinding) {
                when (isPinIncorrect) {
                    true -> {
                        "Incorrect PIN".also { tvMessage.text = it }
                        "Forgot PIN".also { btnSecondary.text = it }
                        "Retry".also { btnPrimary.text = it }

                        btnPrimary.setOnClickListener { dialog.dismiss() }
                        btnSecondary.setOnClickListener { dialog.dismiss() }
                    }

                    else -> {
                        btnSecondary.gone()
                        tvMessage.text = error?.message
                        "Okay".also { btnPrimary.text = it }
                        btnPrimary.setOnClickListener { dialog.dismiss() }
                    }
                }
            }
        }
    }

    private fun initRecipientRecyclerView() = with(binding) {
        val layoutManager = LinearLayoutManager(this@TransferToFriendActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rvRecipient.layoutManager = layoutManager

        // Initialize adapter and set click listener
        recipientAdapter = RecipientAdapter(object : RecipientAdapter.OnItemClickListener {
            override fun onClick(selectedRecipient: RecipientUiModel) {
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

    private fun showPaymentConfirmation() = with(binding) {
        // Validate current selections; selected recipient text contains formatted payment id
        val recipientPaymentIdRaw = tvSelectedRecipientPaymentId.text.toString()
        val recipientName = tvSelectedRecipientName.text.toString()

        paymentConfirmationSheet = PaymentBottomSheetHelper.showConfirmPaymentBottomSheet(
            this@TransferToFriendActivity,
            recipientPaymentIdRaw,
            recipientName,
            recipientPhotoUrl,
            getAmountInKobo(),
            currentUser.balance,
            currentUser.commissionBalance
        ) {
            // on confirm callback
            if (!currentUser.hasPin) {
                showPaymentPinCreationDialog()
                return@showConfirmPaymentBottomSheet
            }

            PaymentBottomSheetHelper.showPaymentPinAuthenticationBottomSheet(
                this@TransferToFriendActivity
            ) { pin ->
                viewModel.authPaymentPin(pin)
            }
        }
    }

    private fun showPaymentPinCreationDialog() {
        val message = "Set up your Payment PIN to complete this transaction securely"

        DialogHelper.showCustomAlertDialogWithIcon(this) { dialog, dialogBinding ->
            with(dialogBinding) {
                "Payment PIN Required".also { tvTitle.text = it }
                "Cancel".also { btnSecondary.text = it }
                "Create PIN".also { btnPrimary.text = it }
                tvMessage.text = message
                ivIcon.setImageResource(R.drawable.ic_lock_filled)

                btnSecondary.setOnClickListener {
                    sendPayment()
                    dialog.dismiss()
                }
                btnPrimary.setOnClickListener {
                    startActivity(
                        Intent(
                            this@TransferToFriendActivity,
                            CreatePaymentPinActivity::class.java
                        )
                    )
                    dialog.dismiss()
                }
            }
        }
    }

    private fun fetchRecipientData(paymentId: String) = with(binding) {
        if (paymentId == currentUser.paymentId) {
            tvError.text = ERROR_CANNOT_SEND_TO_SELF
            tvError.show()
            return
        }

        viewModel.getRecipientByPaymentId(paymentId)
    }

    private fun initInputListeners() = with(binding) {
        etPaymentId.doOnTextChanged { text, _, _, _ ->

            tvError.gone()
            viewSelectedRecipient.gone()
            btnVerify.isVisible = ValidationUtil.isPaymentIdValid(getRecipientPaymentId())

            updateNextButtonState()
        }

        etAmount.addTextChangedListener(
            CurrencyInputWatcher(
                etAmount,
                "₦",
                Locale.forLanguageTag("en-NG"),
                2
            )
        )

        etAmount.doOnTextChanged { text, _, _, _ ->
            val amount = text.toString()

            val isAmountOutOfRange = amount.isNotEmpty() && !isAmountInRange(getAmountInKobo())
            when (isAmountOutOfRange) {
                true -> {
                    tvAmountFeedback.text = ERROR_AMOUNT_OUT_OF_RANGE
                    tvAmountFeedback.show()
                }

                else -> tvAmountFeedback.gone()
            }

            updateNextButtonState()
        }
    }

    private fun updateNextButtonState() = with(binding) {
        val isRecipientSelected = viewSelectedRecipient.isVisible
        val isPaymentIdValid = isPaymentIdValid(getRecipientPaymentId())
        val isAmountInRange = isAmountInRange(getAmountInKobo())

        btnNext.isEnabled = isPaymentIdValid && isAmountInRange && isRecipientSelected
    }

    private fun getAmountInKobo(): Long = with(binding) {
        return etAmount.text.toString().fromNairaStringToKobo()
    }

    private fun getRecipientPaymentId(): String = with(binding) {
        return etPaymentId.text.toString().trim().removeAtPrefix()
    }

    private fun isPaymentIdValid(paymentId: String): Boolean {
        return ValidationUtil.isPaymentIdValid(paymentId)
    }

    private fun isAmountInRange(amount: Long): Boolean {
        // return amount >= 10_000L && amount <= 100_000_000L
        return amount >= 10_000L && amount <= 100_000_000_000_000L
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (focusManager.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }

    companion object {
        private const val ERROR_AMOUNT_OUT_OF_RANGE = "Min ₦100, Max ₦1,000,000"
        private const val ERROR_CANNOT_SEND_TO_SELF = "Self-payments are not allowed"
    }
}
