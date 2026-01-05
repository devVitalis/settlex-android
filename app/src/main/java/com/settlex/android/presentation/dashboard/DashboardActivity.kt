package com.settlex.android.presentation.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.settlex.android.R
import com.settlex.android.databinding.ActivityDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        disableNavItemColorTint()
        setupNavigationComponent()
    }

    private fun setupNavigationComponent() = with(binding) {
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.dashboard_nav_host
        ) as NavHostFragment

        val navController = navHostFragment.navController
        setupWithNavController(bottomNavigationView, navController)

//        // Override selection so Home always pops back
//        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
//            if (item.itemId == R.id.home_fragment) {
//                // Pop everything above home, leave home on top
//                navController.popBackStack(R.id.home_fragment, false)
//                return@setOnItemSelectedListener true
//            }
//            onNavDestinationSelected(item, navController)
//        }
    }

    private fun disableNavItemColorTint() = with(binding) {
        bottomNavigationView.itemIconTintList = null
    }
}