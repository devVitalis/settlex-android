package com.settlex.android.presentation.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.settlex.android.R
import com.settlex.android.databinding.ActivityCreatePaymentIdBinding
import com.settlex.android.databinding.BottomSheetSuccessDialogBinding
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.common.util.EditTextFocusBackgroundChanger
import com.settlex.android.presentation.common.util.KeyboardHelper
import com.settlex.android.presentation.dashboard.DashboardActivity
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePaymentIdActivity : AppCompatActivity() {
    private var userPaymentId: String? = null

    // Validation
    private var isFormatValid = false
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

    private fun initViews() {
        StatusBar.setColor(this, R.color.white)
        setupPaymentIdInputWatcher()
        setupInputFocusHandler()
        disableBackButton()

        binding.btnContinue.setOnClickListener {
            viewModel.assignPaymentId(userPaymentId!!)
        }
    }

    private fun initObservers() {
        observePaymentIdAvailabilityStatus()
        observePaymentIdStoreStatus()
    }

    private fun observePaymentIdStoreStatus() {
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

        DialogHelper.showSuccessBottomSheetDialog(
            this
        ) { dialog: BottomSheetDialog, dialogBinding: BottomSheetSuccessDialogBinding ->
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

    private fun onStorePaymentIdFailure(error: String?) {
        binding.paymentIdAvailabilityFeedback.text = error
        progressLoader.hide()
    }

    private fun observePaymentIdAvailabilityStatus() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPaymentIdTakenEvent.collect {
                    when (it) {
                        is UiState.Loading -> onPaymentIdAvailabilityCheckLoading()
                        is UiState.Success -> showPaymentIdAvailability(it.data)
                        is UiState.Failure -> onPaymentIdAvailabilityFailure(it.exception.message)
                    }
                }
            }
        }
    }

    private fun onPaymentIdAvailabilityCheckLoading() {
        binding.paymentIdProgressBar.show()

        binding.paymentIdAvailableCheck.visibility = View.GONE
        binding.paymentIdProgressBar.visibility = View.VISIBLE
    }

    private fun showPaymentIdAvailability(exists: Boolean) {
        this.exists = exists

        // Hide progress bar
        binding.paymentIdProgressBar.hide()
        binding.paymentIdProgressBar.visibility = View.GONE

        binding.paymentIdAvailableCheck.visibility = if (!exists) View.VISIBLE else View.GONE

        val feedback = if (!exists) "Available" else "Not Available"
        val feedbackColor =
            if (!exists) ContextCompat.getColor(this, R.color.green) else ContextCompat.getColor(
                this,
                R.color.red
            )

        binding.paymentIdAvailabilityFeedback.setTextColor(feedbackColor)
        binding.paymentIdAvailabilityFeedback.visibility = View.VISIBLE
        binding.paymentIdAvailabilityFeedback.text = feedback

        // Validate button state
        updateContinueButtonState()
    }

    private fun onPaymentIdAvailabilityFailure(error: String?) {
        with(binding) {
            txtError.text = error
            txtError.gone()

            paymentIdProgressBar.hide()
            paymentIdProgressBar.gone()
            paymentIdAvailableCheck.gone()
            paymentIdAvailabilityFeedback.gone()
        }
    }

    private fun setupInputFocusHandler() = with(binding) {
        val defaultRes = R.drawable.bg_edit_txt_custom_white_not_focused
        val focusedRes = R.drawable.bg_edit_txt_custom_white_focused

        EditTextFocusBackgroundChanger(
            defaultRes,
            focusedRes,
            editTxtPaymentId to editTxtPaymentIdBackground
        )
    }

    private fun setupPaymentIdInputWatcher() {
        binding.editTxtPaymentId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
                setContinueButtonEnabled(false) // disable continue btn

                // don't trim
                validatePaymentIdRuleSet(s.toString())
                userPaymentId = s.toString()

                // Hide feedbacks
                binding.paymentIdAvailableCheck.visibility = View.GONE
                binding.paymentIdAvailabilityFeedback.visibility = View.GONE
                binding.txtError.visibility = View.GONE
            }

            override fun afterTextChanged(e: Editable) {
                // don't trim

                val eString = e.toString()
                val shouldSearch = isFormatValid

                // cancel any previous pending check
                if (pendingCheckRunnable != null) {
                    handler.removeCallbacks(pendingCheckRunnable!!)
                }

                if (shouldSearch) {
                    pendingCheckRunnable = Runnable { viewModel.isPaymentIdTaken(eString) }
                    handler.postDelayed(pendingCheckRunnable!!, 1500)
                }
            }
        })
    }

    private fun validatePaymentIdRuleSet(paymentId: String) {
        // Cache drawables and colors
        val validBg = ContextCompat.getDrawable(this, R.drawable.bg_8dp_green_light)
        val invalidBg = ContextCompat.getDrawable(this, R.drawable.bg_8dp_gray_light)

        val validIcon = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
        val invalidIcon = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray))

        val validText = ContextCompat.getColor(this, R.color.green)
        val invalidText = ContextCompat.getColor(this, R.color.gray)

        // Evaluate rules once
        val startsWith = startsWithLetter(paymentId)
        val hasMinimumLength = hasMinimumLength(paymentId)
        val isValidFormat = isAlphaNumericFormat(paymentId)

        // Starts with letter
        binding.layoutRuleStartWith.background = if (startsWith) validBg else invalidBg
        binding.iconCheckRuleStartWith.imageTintList = if (startsWith) validIcon else invalidIcon
        binding.txtRuleStartWith.setTextColor(if (startsWith) validText else invalidText)

        // Minimum length
        binding.layoutRuleLength.background = if (hasMinimumLength) validBg else invalidBg
        binding.icCheckRuleLength.imageTintList = if (hasMinimumLength) validIcon else invalidIcon
        binding.txtRuleLength.setTextColor(if (hasMinimumLength) validText else invalidText)

        // Alphanumeric format
        binding.layoutRuleContains.background = if (isValidFormat) validBg else invalidBg
        binding.icCheckRuleContains.imageTintList = if (isValidFormat) validIcon else invalidIcon
        binding.txtRuleContains.setTextColor(if (isValidFormat) validText else invalidText)

        // Only update the flag here
        isFormatValid = startsWith && hasMinimumLength && isValidFormat
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
        // Button only enabled when format is valid AND ID does not exist
        setContinueButtonEnabled(isFormatValid && !exists)
    }

    private fun setContinueButtonEnabled(enable: Boolean) {
        binding.btnContinue.isEnabled = enable
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (keyboardHelper.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }

    private fun disableBackButton() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // prevent the user from navigating back
                }
            }
        )
    }
}