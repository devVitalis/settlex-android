package com.settlex.android.presentation.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.settlex.android.R
import com.settlex.android.databinding.ActivityDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val navControllers = mutableMapOf<Int, NavController>()

    private val navHostIds = listOf(
        R.id.home_nav_host,
        R.id.services_nav_host,
        R.id.rewards_nav_host,
        R.id.account_nav_host
    )

    private val menuIds = listOf(
        R.id.home_fragment,
        R.id.services_fragment,
        R.id.rewards_fragment,
        R.id.account_fragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMultipleBackStacks()
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
                R.id.home_fragment -> R.id.home_nav_host
                R.id.services_fragment -> R.id.services_nav_host
                R.id.rewards_fragment -> R.id.rewards_nav_host
                R.id.account_fragment -> R.id.account_nav_host
                else -> return@setOnItemSelectedListener false
            }

            switchTab(hostId)
            true
        }

        // Disable icon tint
        binding.bottomNavigationView.itemIconTintList = null

        // Set default tab
        binding.bottomNavigationView.selectedItemId = R.id.home_fragment
    }

    private fun switchTab(hostId: Int) {
        // Hide all hosts
        navHostIds.forEach { id ->
            supportFragmentManager.findFragmentById(id)?.view?.visibility =
                android.view.View.GONE
        }

        // Show selected host
        supportFragmentManager.findFragmentById(hostId)?.view?.visibility =
            android.view.View.VISIBLE
    }
}