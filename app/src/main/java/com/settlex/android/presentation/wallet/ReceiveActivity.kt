package com.settlex.android.presentation.wallet

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.settlex.android.R
import com.settlex.android.databinding.ActivityReceiveBinding
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceiveActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiveBinding
    private val viewModel: WalletViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        StatusBar.setColor(this, R.color.white)

        binding.btnCopy.setOnClickListener {
            StringFormatter.copyToClipboard(
                this,
                "Payment ID",
                binding.paymentId.text.toString(),
                true
            )
        }

        binding.btnBackBefore.setOnClickListener { finish() }
        binding.btnShareDetails.setOnClickListener {
            Toast.makeText(
                this,
                "This feature is not yet implemented",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}