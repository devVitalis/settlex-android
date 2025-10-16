package com.settlex.android.ui.dashboard.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityDashboardBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        disableNavItemColorTint();
        setupNavigationComponent();
    }

    private void setupNavigationComponent() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.dashboard_nav_host);
        if (navHostFragment == null) return;

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);

        // Override selection so Home always pops back
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.homeFragment) {
                // Pop everything above home, leave home on top
                navController.popBackStack(R.id.homeFragment, false);
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    private void disableNavItemColorTint() {
        binding.bottomNavigationView.setItemIconTintList(null);
    }
}