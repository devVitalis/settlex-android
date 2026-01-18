package com.settlex.android.presentation.transactions

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.RenderMode
import com.settlex.android.R
import com.settlex.android.data.enums.TransactionStatus
import com.settlex.android.databinding.ActivityTransactionStatusBinding
import com.settlex.android.presentation.common.extensions.getParcelableExtraCompat
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toNairaString
import com.settlex.android.presentation.dashboard.DashboardActivity
import com.settlex.android.presentation.transactions.model.TransactionResult
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val result = intent.getParcelableExtraCompat<TransactionResult>("transaction_result")
        initViews(result)
    }

    private fun initViews(result: TransactionResult) {
        StatusBar.setColor(this, R.color.colorSurfaceVariant)
        showTransactionData(result)
        setupDoneButton()
    }

    private fun showTransactionData(result: TransactionResult) = with(binding) {
        tvTxnAmount.text = result.amount.toNairaString()
        tvTxnStatus.text = result.message

        when (result.status) {
            TransactionStatus.SUCCESS -> showSuccessState()
            TransactionStatus.PENDING -> showPendingState()
            TransactionStatus.FAILED -> showFailedState(result.errorMessage)
            else -> Unit
        }
    }

    private fun showPendingState() = with(binding) {
        animTxnPending.apply {
            show()
            renderMode = RenderMode.SOFTWARE
        }
    }

    private fun showSuccessState() = with(binding) {
        animTxnSuccess.apply {
            show()
            renderMode = RenderMode.SOFTWARE
        }
    }

    private fun showFailedState(errorMessage: String?) = with(binding) {
        animTxnFailed.apply {
            show()
            renderMode = RenderMode.SOFTWARE
        }

        if (!errorMessage.isNullOrEmpty()) {
            tvError.text = errorMessage
            tvError.show()
        }
    }

    private fun setupDoneButton() = with(binding) {
        tvDone.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finishAffinity()
    }
}
