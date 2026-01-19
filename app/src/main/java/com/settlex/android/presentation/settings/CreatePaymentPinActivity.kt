package com.settlex.android.presentation.settings

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.chaos.view.PinView
import com.settlex.android.R
import com.settlex.android.data.exception.AppException
import com.settlex.android.databinding.ActivityCreatePaymentPinBinding
import com.settlex.android.presentation.common.extensions.getParcelableExtraCompat
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.common.util.ValidationUtil
import com.settlex.android.presentation.settings.viewmodel.SettingsViewModel
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePaymentPinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePaymentPinBinding
    private lateinit var intentAction: PaymentPinFlow
    private val progressLoader by lazy { ProgressDialogManager(this) }
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePaymentPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intentAction = intent.getParcelableExtraCompat("payment_pin_flow")

        initObservers()
        initViews()
    }

    private fun initObservers() {
        observeSetPaymentPinEvent()
    }

    private fun initViews() = with(binding) {
        StatusBar.setColor(this@CreatePaymentPinActivity, R.color.surface)
        setupPinInputWatcher()
        updateUiFromIntent()

        btnBackBefore.setOnClickListener { finish() }
        btnSetPin.setOnClickListener { settingsViewModel.setPaymentPin(newPinView.text.toString()) }
    }

    private fun updateUiFromIntent() = with(binding) {
        when (intentAction) {
            is PaymentPinFlow.CreatePin -> {
                viewCurrentPinContainer.gone()
                "Set Payment PIN".also { tvTitle.text = it }
            }

            PaymentPinFlow.ChangePin -> {
                viewCurrentPinContainer.show()
            }

            PaymentPinFlow.ResetPin -> {
                viewCurrentPinContainer.gone()
                "Reset Payment PIN".also { tvTitle.text = it }
            }
        }
    }

    //        private void changePaymentPin() {
//            String oldPin = Objects.requireNonNull(binding.currentPinView.getText()).toString();
//            String newPin = Objects.requireNonNull(binding.pinView.getText()).toString();
//
//            profileViewModel.changePaymentPin(oldPin, newPin);
//        }

    // Observers
    private fun observeSetPaymentPinEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.setPaymentPinEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> showSuccessBottomSheetDialog()
                        is UiState.Failure -> showErrorDialog(state.exception)
                    }
                }
            }
        }
    }

    private fun showSuccessBottomSheetDialog() {
        val message = when (intentAction) {
            is PaymentPinFlow.CreatePin -> "Your Payment PIN has been created successfully"
            else -> "Your Payment PIN has been updated successfully"
        }

        DialogHelper.showSuccessBottomSheetDialog(this) { dialog, dialogBinding ->
            with(dialogBinding) {
                "Success".also { tvTitle.text = it }
                message.also { tvMessage.text = it }
                "Continue".also { btnAction.text = it }

                btnAction.setOnClickListener {
                    dialog.dismiss()
                    finish()
                }
            }
        }

        progressLoader.hide()
    }

    private fun showErrorDialog(error: AppException) {
        DialogHelper.showCustomAlertDialog(this) { dialog, dialogBinding ->
            with(dialogBinding) {
                tvMessage.text = error.message
                "Okay".also { btnPrimary.text = it }

                btnSecondary.gone()
                btnPrimary.setOnClickListener { dialog.dismiss() }
            }
        }

        progressLoader.hide()
    }

    private fun setupPinInputWatcher() = with(binding) {
        newPinView.doOnTextChanged { text, _, _, _ ->
            tvNewPinViewError.gone()
            showPinConfirmationStep(text.toString())
        }

        confirmPinView.doOnTextChanged { _, _, _, _ ->
            validatePinConfirmation()
        }

        currentPinView.doOnTextChanged { _, _, _, _ -> }
    }

    private fun beginDelayedLayoutTransition() {
        val transition = AutoTransition().apply { duration = 300 }
        TransitionManager.beginDelayedTransition(binding.root, transition)
    }

    private fun showPinConfirmationStep(newPinValue: String) = with(binding) {
        if (isPinComplete(newPinValue, newPinView)) {
            beginDelayedLayoutTransition()

            viewNewPinContainer.gone()
            viewConfirmPinContainer.show()

            confirmPinView.requestFocus()
        }
    }

    private fun validatePinConfirmation() = with(binding) {
        val pin = newPinView.text.toString()
        val confirmPin = confirmPinView.text.toString()

        val isPinComplete = isPinComplete(confirmPinView.text.toString(), confirmPinView)
        val isPinValidAndMatch = ValidationUtil.isPaymentPinValidAndMatch(pin, confirmPin)

        if (isPinComplete && !isPinValidAndMatch) {

            // Clear text fields
            listOf(newPinView, confirmPinView).forEach { it.setText("") }

            // Show error message
            tvNewPinViewError.apply {
                text = ERROR_PIN_MISMATCH
                show()
            }

            beginDelayedLayoutTransition()

            viewNewPinContainer.show()
            viewConfirmPinContainer.gone()

            newPinView.requestFocus()
        }

        updateSetPinButtonState(isPinComplete && isPinValidAndMatch)
    }

    private fun updateSetPinButtonState(isAllValidationsPassed: Boolean) = with(binding) {
        btnSetPin.isEnabled = isAllValidationsPassed
    }

    private fun isPinComplete(pin: String, view: PinView): Boolean {
        return pin.length == view.itemCount
    }

    companion object {
        private const val ERROR_PIN_MISMATCH = "PIN does not match"
        private const val ERROR_CURRENT_PIN_IS_SAME_AS_NEW =
            "New PIN must not be the same as current"
    }
}