package com.settlex.android.ui.dashboard.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        disableItemColorTint();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.dashboardNavHost);

        // Add a null check to prevent the crash
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
        } else {
            // Log an error or handle the case where the fragment isn't found
            Log.e("DashboardActivity", "NavHostFragment not found");
        }
    }

    private void disableItemColorTint() {
        binding.bottomNavigationView.setItemIconTintList(null);
    }
}