package com.settlex.android.presentation.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.R
import com.settlex.android.databinding.ActivityCreatePaymentIdBinding
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.common.util.KeyboardHelper
import com.settlex.android.presentation.dashboard.DashboardActivity
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePaymentIdActivity : AppCompatActivity() {
    private var userPaymentId: String? = null
    private var exists = false

    // Dependencies
    private lateinit var binding: ActivityCreatePaymentIdBinding
    private val viewModel: SettingsViewModel by viewModels()
    private val progressLoader by lazy { ProgressDialogManager(this) }
    private val keyboardHelper by lazy { KeyboardHelper(this) }
    private val handler = Handler(Looper.getMainLooper())
    private var pendingCheckRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePaymentIdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
    }

    private fun initViews() = with(binding) {
        StatusBar.setColor(this@CreatePaymentIdActivity, R.color.colorBackground)
        setupPaymentIdTextWatcher()
        disableBackButton()

        btnContinue.setOnClickListener { viewModel.assignPaymentId(userPaymentId!!) }
    }

    private fun initObservers() {
        observePaymentIdAvailability()
        observeAssignPaymentIdEvent()
    }

    private fun observeAssignPaymentIdEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.assignPaymentIdEvent.collect {
                    when (it) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> showPaymentIdCreationSuccessDialog()
                        is UiState.Failure -> onStorePaymentIdFailure(it.exception.message)
                    }
                }
            }
        }
    }

    private fun showPaymentIdCreationSuccessDialog() {
        progressLoader.hide()

        DialogHelper.showSuccessBottomSheetDialog(this) { dialog, dialogBinding ->
            with(dialogBinding) {
                val title = "Success"
                val message = "Your Payment ID was successfully created."

                tvTitle.text = title
                tvMessage.text = message
                "Okay".also { btnAction.text = it }

                btnAction.setOnClickListener {
                    startActivity(
                        Intent(
                            this@CreatePaymentIdActivity,
                            DashboardActivity::class.java
                        )
                    )
                    finish()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun onStorePaymentIdFailure(error: String?) = with(binding) {
        paymentIdAvailabilityFeedback.text = error
        progressLoader.hide()
    }

    private fun observePaymentIdAvailability() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPaymentIdTakenEvent.collect {
                    when (it) {
                        is UiState.Loading -> onPaymentIdAvailabilityCheckLoading()
                        is UiState.Success -> setPaymentIdAvailabilityStatus(it.data)
                        is UiState.Failure -> onPaymentIdAvailabilityFailure(it.exception.message)
                    }
                }
            }
        }
    }

    private fun onPaymentIdAvailabilityCheckLoading() = with(binding) {
        paymentIdProgressBar.show()
        ivPaymentIdAvailable.gone()
    }

    private fun setPaymentIdAvailabilityStatus(exists: Boolean) = with(binding) {
        this@CreatePaymentIdActivity.exists = exists

        // Hide progress bar
        paymentIdProgressBar.gone()

        ivPaymentIdAvailable.visibility = if (!exists) View.VISIBLE else View.GONE

        val feedback = if (!exists) "Available" else "Not Available"
        val feedbackColor =
            if (!exists) ContextCompat.getColor(
                this@CreatePaymentIdActivity,
                R.color.colorSuccess
            ) else ContextCompat.getColor(
                this@CreatePaymentIdActivity,
                R.color.colorError
            )

        paymentIdAvailabilityFeedback.setTextColor(feedbackColor)
        paymentIdAvailabilityFeedback.show()
        paymentIdAvailabilityFeedback.text = feedback

        // Validate button state
        updateContinueButtonState()
    }

    private fun onPaymentIdAvailabilityFailure(error: String?) = with(binding) {
        tvError.text = error
        tvError.show()

        listOf(
            paymentIdProgressBar,
            ivPaymentIdAvailable,
            paymentIdAvailabilityFeedback
        ).forEach { it.gone() }
    }

    private fun setupPaymentIdTextWatcher() = with(binding) {
        editTxtPaymentId.doOnTextChanged { paymentId, _, _, _ ->
            // Disable continue button
            setContinueButtonEnabled(false)

            validatePaymentIdRuleSet(paymentId.toString())
            userPaymentId = paymentId.toString()

            // Hide feedbacks
            listOf(
                ivPaymentIdAvailable,
                paymentIdAvailabilityFeedback,
                tvError
            ).forEach { it.gone() }
        }

        editTxtPaymentId.doAfterTextChanged { paymentId ->
            // Cancel any previous pending check
            pendingCheckRunnable?.let { handler.removeCallbacks(it) }

            if (isPaymentIdValid()) {
                pendingCheckRunnable = Runnable { viewModel.isPaymentIdTaken(paymentId.toString()) }
                pendingCheckRunnable?.let { handler.postDelayed(it, 1500) }
            }
        }
    }

    private fun validatePaymentIdRuleSet(paymentId: String) {
        // Cache drawables and colors
        val validBg = ContextCompat.getDrawable(this, R.drawable.bg_8dp_green_light)
        val invalidBg = ContextCompat.getDrawable(this, R.drawable.bg_surface_rounded8)

        val validIcon = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorSuccess))
        val invalidIcon = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorOnSurfaceVariant))

        val validText = ContextCompat.getColor(this, R.color.colorSuccess)
        val invalidText = ContextCompat.getColor(this, R.color.colorOnSurfaceVariant)

        // Evaluate rules once
        val startsWith = startsWithLetter(paymentId)
        val hasMinimumLength = hasMinimumLength(paymentId)
        val isValidFormat = isAlphaNumericFormat(paymentId)

        with(binding) {
            // Starts with letter
            viewRuleStartWith.background = if (startsWith) validBg else invalidBg
            ivCheckRuleStartWith.imageTintList = if (startsWith) validIcon else invalidIcon
            tvRuleStartWith.setTextColor(if (startsWith) validText else invalidText)

            // Minimum length
            viewRuleLength.background = if (hasMinimumLength) validBg else invalidBg
            ivCheckRuleLength.imageTintList = if (hasMinimumLength) validIcon else invalidIcon
            tvRuleLength.setTextColor(if (hasMinimumLength) validText else invalidText)

            // Alphanumeric format
            viewRuleContains.background = if (isValidFormat) validBg else invalidBg
            ivCheckRuleContains.imageTintList = if (isValidFormat) validIcon else invalidIcon
            tvRuleContains.setTextColor(if (isValidFormat) validText else invalidText)
        }
    }

    private fun isPaymentIdValid(): Boolean = with(binding) {
        val paymentId = editTxtPaymentId.text.toString()
        return startsWithLetter(paymentId) && hasMinimumLength(paymentId) && isAlphaNumericFormat(
            paymentId
        )
    }

    private fun startsWithLetter(paymentId: String): Boolean {
        return paymentId.matches("^[A-Za-z].*".toRegex())
    }

    private fun hasMinimumLength(paymentId: String): Boolean {
        return paymentId.length >= 5 && paymentId.length <= 20
    }

    private fun isAlphaNumericFormat(paymentId: String): Boolean {
        return paymentId.matches("^[a-z0-9]+$".toRegex())
    }

    private fun updateContinueButtonState() {
        setContinueButtonEnabled(isPaymentIdValid() && !exists)
    }

    private fun setContinueButtonEnabled(enable: Boolean) = with(binding) {
        btnContinue.isEnabled = enable
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (keyboardHelper.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }

    private fun disableBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing to disable back button
            }
        })
    }
}