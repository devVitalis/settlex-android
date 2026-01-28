package com.settlex.android.presentation.dashboard

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.settlex.android.R
import com.settlex.android.databinding.ActivityDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {
    lateinit var binding: ActivityDashboardBinding
    private val navControllers = mutableMapOf<Int, NavController>()

    private val navHostIds = listOf(
        R.id.home_nav_host,
        R.id.services_nav_host,
        R.id.rewards_nav_host,
        R.id.account_nav_host
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMultipleBackStacks()
        handleBackStack()
    }

    private fun setupMultipleBackStacks() {
        // Initialize all NavControllers
        navHostIds.forEach { hostId ->
            val navHostFragment = supportFragmentManager.findFragmentById(hostId) as NavHostFragment
            navControllers[hostId] = navHostFragment.navController
        }

        // Handle bottom nav selection
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val hostId = when (item.itemId) {
                R.id.menu_home_fragment -> R.id.home_nav_host
                R.id.menu_services_fragment -> R.id.services_nav_host
                R.id.menu_rewards_fragment -> R.id.rewards_nav_host
                R.id.menu_account_fragment -> R.id.account_nav_host
                else -> return@setOnItemSelectedListener false
            }

            switchTab(hostId)
            true
        }

        binding.bottomNavigationView.itemIconTintList = null

        // Set default tab
        binding.bottomNavigationView.selectedItemId = R.id.menu_home_fragment
    }

    private fun switchTab(selectedHostId: Int) {
        val transaction = supportFragmentManager.beginTransaction()

        navHostIds.forEach { hostId ->
            val fragment = supportFragmentManager.findFragmentById(hostId)!!
            if (hostId == selectedHostId) {
                transaction.show(fragment)
            } else {
                transaction.hide(fragment)
            }
        }

        transaction.commit()
    }

    private fun handleBackStack() {
        onBackPressedDispatcher.addCallback(this) {
            // Get the currently selected host ID from the bottom nav
            val currentHostId = when (binding.bottomNavigationView.selectedItemId) {
                R.id.menu_home_fragment -> R.id.home_nav_host
                R.id.menu_services_fragment -> R.id.services_nav_host
                R.id.menu_rewards_fragment -> R.id.rewards_nav_host
                R.id.menu_account_fragment -> R.id.account_nav_host
                else -> null
            }

            // Find that specific NavController
            val navController = navControllers[currentHostId]

            // Try to go back. If it can't (we are at the start of the tab),
            // we either go to the Home tab or close the app.
            if (navController?.navigateUp() != true) {
                if (currentHostId != R.id.home_nav_host) {
                    binding.bottomNavigationView.selectedItemId = R.id.menu_home_fragment
                } else {
                    finish()
                }
            }
        }
    }
}
