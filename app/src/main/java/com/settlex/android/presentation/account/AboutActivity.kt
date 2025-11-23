package com.settlex.android.presentation.account

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.settlex.android.R
import com.settlex.android.SettleXApp
import com.settlex.android.databinding.ActivityAboutBinding
import com.settlex.android.presentation.legal.PrivacyPolicyActivity
import com.settlex.android.presentation.legal.TermsAndConditionsActivity
import com.settlex.android.util.ui.StatusBar

class AboutActivity : AppCompatActivity() {
    private fun getTAG(): String? = AboutActivity::class.simpleName
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUiActions()
    }

    private fun setupUiActions() {
        StatusBar.setColor(this, R.color.white)
        binding.appVersion.text = getAppVersion()


        binding.btnTermsAndCondition.setOnClickListener {
            routeToDestination(
                TermsAndConditionsActivity::class.java
            )
        }

        binding.btnPrivacyPolicy.setOnClickListener { routeToDestination(PrivacyPolicyActivity::class.java) }
        binding.btnBackBefore.setOnClickListener { finish() }
    }

    private fun getAppVersion(): String {
        val context = SettleXApp.Companion.appContext
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return "v" + packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(getTAG(), "NameNotFoundException: " + e.message, e)
        }
        return "N/A"
    }

    private fun routeToDestination(activityClass: Class<out Activity>) {
        startActivity(Intent(this, activityClass))
    }
}