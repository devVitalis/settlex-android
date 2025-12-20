package com.settlex.android.presentation.wallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.R
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.databinding.ActivityReceiveBinding
import com.settlex.android.presentation.common.extensions.addAtPrefix
import com.settlex.android.presentation.wallet.viewmodel.WalletViewModel
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReceiveActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiveBinding
    private val viewModel: WalletViewModel by viewModels()
    private var userPaymentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        observeUserSessionAndGetData()
    }

    private fun observeUserSessionAndGetData() = with(binding) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userSessionState.collect { state ->
                    when (state) {
                        is UserSessionState.Authenticated -> state.user.paymentId?.addAtPrefix()
                            .also {
                                userPaymentId = it
                                tvPaymentId.text = userPaymentId
                            }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun initViews() = with(binding) {
        StatusBar.setColor(this@ReceiveActivity, R.color.white)

        btnCopy.setOnClickListener {
            StringFormatter.copyToClipboard(
                this@ReceiveActivity,
                "Payment ID",
                tvPaymentId.text.toString(),
                true
            )
        }

        btnBackBefore.setOnClickListener { finish() }
        btnShareDetails.setOnClickListener { sharePaymentId() }
    }

    private fun sharePaymentId() {
        val shareIntent = Intent().apply {
            val message = "You can send me money on SettleX using my Payment ID: $userPaymentId"
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
}