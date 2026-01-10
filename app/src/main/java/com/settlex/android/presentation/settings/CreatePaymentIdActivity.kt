package com.settlex.android.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.Toast
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
import com.settlex.android.data.exception.AppException
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
    private val validStateBackground by lazy {
        ContextCompat.getDrawable(
            this,
            R.drawable.bg_success_container_rounded8
        )
    }
    private val defaultStateBackground by lazy {
        ContextCompat.getDrawable(
            this,
            R.drawable.bg_surface_rounded8
        )
    }
    private val successStateColor by lazy { ContextCompat.getColor(this, R.color.colorSuccess) }
    private val defaultStateColor by lazy {
        ContextCompat.getColor(
            this,
            R.color.colorOnSurfaceVariant
        )
    }

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

        btnClose.setOnClickListener {
            Toast.makeText(
                this@CreatePaymentIdActivity,
                "Payment ID creation is required to continue",
                Toast.LENGTH_SHORT
            ).show()
        }
        btnContinue.setOnClickListener { viewModel.assignPaymentId(getPaymentId()) }
    }

    private fun initObservers() {
        observePaymentIdAvailability()
        observeAssignPaymentIdEvent()
    }

    private fun observeAssignPaymentIdEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.assignPaymentIdEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> showPaymentIdCreationSuccessDialog()
                        is UiState.Failure -> onAssignPaymentIdError(state.exception)
                    }
                }
            }
        }
    }

    private fun showPaymentIdCreationSuccessDialog() {
        progressLoader.hide()

        DialogHelper.showSuccessBottomSheetDialog(this) { dialog, dialogBinding ->
            with(dialogBinding) {
                "Success".also { tvTitle.text = it }
                "Your Payment ID was successfully created".also { tvMessage.text = it }
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

    private fun onAssignPaymentIdError(error: AppException) = with(binding) {
        progressLoader.hide()

        when (error) {
            is AppException.NetworkException -> showNoInternetDialog(error.message)
            else -> tvError.text = error.message
        }
    }

    private fun showNoInternetDialog(message: String) {
        DialogHelper.showCustomAlertDialog(this) { dialog, dialogBinding ->
            with(dialogBinding) {
                tvMessage.text = message
                "Cancel".also { btnSecondary.text = it }
                "Retry".also { btnPrimary.text = it }

                btnSecondary.setOnClickListener { dialog.dismiss() }

                btnPrimary.setOnClickListener {
                    viewModel.assignPaymentId(getPaymentId())
                    dialog.dismiss()
                }
            }
        }
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
        pbPaymentIdCheck.show()
    }

    private fun setPaymentIdAvailabilityStatus(isPaymentIdTaken: Boolean) = with(binding) {
        pbPaymentIdCheck.gone()

        ivPaymentIdAvailable.also { if (isPaymentIdTaken) it.gone() else it.show() }

        val statusText = if (isPaymentIdTaken) "Not Available" else "Available"
        val statusColor = when (isPaymentIdTaken) {
            true -> ContextCompat.getColor(this@CreatePaymentIdActivity, R.color.colorError)
            false -> ContextCompat.getColor(this@CreatePaymentIdActivity, R.color.colorSuccess)
        }

        tvPaymentIdAvailabilityStatus.also {
            it.text = statusText
            it.setTextColor(statusColor)
            it.show()
        }

        updateContinueButtonState(isPaymentIdTaken)
    }

    private fun onPaymentIdAvailabilityFailure(error: String?) = with(binding) {
        tvError.text = error
        tvError.show()

        listOf(
            pbPaymentIdCheck,
            ivPaymentIdAvailable,
            tvPaymentIdAvailabilityStatus
        ).forEach { it.gone() }
    }

    private fun setupPaymentIdTextWatcher() = with(binding) {
        etPaymentId.doOnTextChanged { paymentId, _, _, _ ->
            // Disable continue button
            btnContinue.isEnabled = false

            validatePaymentIdRulesUI(paymentId.toString())

            listOf(
                ivPaymentIdAvailable,
                tvPaymentIdAvailabilityStatus,
                tvError
            ).forEach { it.gone() }
        }

        etPaymentId.doAfterTextChanged { paymentId ->
            // Cancel any previous pending check
            pendingCheckRunnable?.let { handler.removeCallbacks(it) }

            if (isPaymentIdValid()) {
                pendingCheckRunnable = Runnable { viewModel.isPaymentIdTaken(paymentId.toString()) }
                pendingCheckRunnable?.let { handler.postDelayed(it, 1500) }
            }
        }
    }

    private fun validatePaymentIdRulesUI(paymentId: String) = with(binding) {
        listOf(
            Pair(
                isPaymentIdAlphaPrefixed(paymentId),
                Triple(viewRuleStartWith, ivRuleStartWith, tvRuleStartWith)
            ),
            Pair(
                isPaymentIdLengthInRange(paymentId),
                Triple(viewRuleLength, ivRuleLength, tvRuleLength)
            ),
            Pair(
                isPaymentIdLowercaseAlphanumeric(paymentId),
                Triple(viewRuleContains, ivRuleContains, tvRuleContains)
            )
        ).forEach { (isRequirementMet, triple) ->
            val (view, icon, textView) = triple

            val statusColor = if (isRequirementMet) successStateColor else defaultStateColor
            val statusBackgroundDrawable = when {
                isRequirementMet -> validStateBackground
                else -> defaultStateBackground
            }

            view.background = statusBackgroundDrawable
            icon.setColorFilter(statusColor)
            textView.setTextColor(statusColor)
        }
    }

    private fun getPaymentId(): String = with(binding) {
        return etPaymentId.text.toString()
    }

    private fun isPaymentIdValid(): Boolean = with(binding) {
        val paymentId = etPaymentId.text.toString()
        return isPaymentIdAlphaPrefixed(paymentId)
                && isPaymentIdLengthInRange(paymentId)
                && isPaymentIdLowercaseAlphanumeric(paymentId)
    }

    private fun isPaymentIdAlphaPrefixed(paymentId: String): Boolean {
        return paymentId.matches("^[A-Za-z].*".toRegex())
    }

    private fun isPaymentIdLengthInRange(paymentId: String): Boolean {
        return paymentId.length >= 5 && paymentId.length <= 20
    }

    private fun isPaymentIdLowercaseAlphanumeric(paymentId: String): Boolean {
        return paymentId.matches("^[a-z0-9]+$".toRegex())
    }

    private fun updateContinueButtonState(isPaymentIdTaken: Boolean) = with(binding) {
        btnContinue.isEnabled = isPaymentIdValid() && !isPaymentIdTaken
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (keyboardHelper.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }

    private fun disableBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
    }
}