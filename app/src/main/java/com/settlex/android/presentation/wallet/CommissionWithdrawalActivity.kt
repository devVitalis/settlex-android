package com.settlex.android.presentation.wallet

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.settlex.android.R
import com.settlex.android.databinding.ActivityCommissionWithdrawalBinding
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommissionWithdrawalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommissionWithdrawalBinding
    private val viewModel: WalletViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommissionWithdrawalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUiActions()
    }

    private fun setupUiActions() {
        StatusBar.setColor(this, R.color.white)

        binding.btnWithdraw.setOnClickListener {
            StringFormatter.showNotImplementedToast(
                this
            )
        }
        binding.btnBackBefore.setOnClickListener { v: View? -> finish() }
    }
}