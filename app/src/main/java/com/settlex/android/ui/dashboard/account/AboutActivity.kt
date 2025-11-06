package com.settlex.android.ui.dashboard.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.settlex.android.R
import com.settlex.android.databinding.ActivityAboutBinding
import com.settlex.android.ui.info.legal.PrivacyPolicyActivity
import com.settlex.android.ui.info.legal.TermsAndConditionsActivity
import com.settlex.android.utils.ui.StatusBarUtil


class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUiActions()
    }

    private fun setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white)

        binding.btnTermsAndCondition.setOnClickListener {
            routeToDestination(
                TermsAndConditionsActivity::class.java
            )
        }

        binding.btnPrivacyPolicy.setOnClickListener { routeToDestination(PrivacyPolicyActivity::class.java) }
        binding.btnBackBefore.setOnClickListener { finish() }
    }

    private fun routeToDestination(activityClass: Class<out Activity>) {
        startActivity(Intent(this, activityClass))
    }
}